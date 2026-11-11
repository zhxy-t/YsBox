package com.fongmi.android.tv.utils.python;

import com.fongmi.chaquo.Packages;
import com.github.catvod.net.OkHttp;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 运行时安装纯 Python 包。
 * Chaquopy 不支持 pip，这里从 PyPI 下载 py3-none-any 的 wheel，解压到
 * files/py_packages 并挂载到 sys.path。
 */
public class PythonPackageManager {

    private static final String INDEX = "https://pypi.org/pypi/";
    private static final Pattern DEP_NAME = Pattern.compile("^[A-Za-z0-9._-]+");
    private static final Object LOCK = new Object();

    public interface Progress {
        void onStep(String message);
    }

    public static List<PythonPackage> getInstalled() {
        List<PythonPackage> list = new ArrayList<>();
        File[] dirs = Packages.dir().listFiles(File::isDirectory);
        if (dirs == null) return list;
        for (File dir : dirs) {
            if (dir.getName().startsWith(".")) continue;
            PythonPackage item = read(dir);
            if (item != null) list.add(item);
        }
        Collections.sort(list, Comparator.comparing(PythonPackage::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public static boolean applyRuntime() {
        if (!com.chaquo.python.Python.isStarted()) return false;
        Packages.apply();
        return true;
    }

    public static void install(String spec, Progress progress) throws Exception {
        synchronized (LOCK) {
            Set<String> visited = new HashSet<>();
            installInternal(spec, progress, visited, 0);
            applyRuntime();
        }
    }

    public static void uninstall(PythonPackage item) throws IOException {
        synchronized (LOCK) {
            delete(item.getDir());
            applyRuntime();
        }
    }

    private static void installInternal(String spec, Progress progress, Set<String> visited, int depth) throws Exception {
        if (depth > 10) return;
        String raw = spec == null ? "" : spec.trim();
        if (raw.isEmpty()) throw new IOException("empty package name");
        String name = raw;
        String version = null;
        int eq = raw.indexOf("==");
        if (eq > 0) {
            name = raw.substring(0, eq).trim();
            version = raw.substring(eq + 2).trim();
        }
        name = normalize(name);
        if (name.isEmpty()) throw new IOException("empty package name");
        if (!visited.add(name)) return;
        if (isInstalled(name)) {
            progress.onStep(name + " already installed");
            return;
        }
        String url = INDEX + name + (version == null || version.isEmpty() ? "" : "/" + version) + "/json";
        progress.onStep("fetching " + name);
        JsonObject root = fetch(url);
        JsonObject info = root.getAsJsonObject("info");
        String resolved = version == null || version.isEmpty() ? str(info, "version") : version;
        JsonObject wheel = findWheel(root.getAsJsonArray("urls"));
        if (wheel == null) throw new IOException("no pure-python wheel for " + name);
        String filename = str(wheel, "filename");
        File target = new File(Packages.dir(), safeName(name) + "-" + resolved);
        if (target.exists()) {
            progress.onStep(name + " already installed");
            return;
        }
        File tmp = new File(Packages.dir(), ".tmp-" + safeName(name));
        File archive = new File(Packages.dir(), ".tmp-" + safeName(name) + ".whl");
        try {
            progress.onStep("downloading " + filename);
            download(str(wheel, "url"), archive);
            verify(archive, wheel);
            if (tmp.exists()) delete(tmp);
            if (!tmp.mkdirs()) throw new IOException("failed to create " + tmp);
            progress.onStep("extracting " + filename);
            extract(archive, tmp);
            if (target.exists()) delete(target);
            if (!tmp.renameTo(target)) throw new IOException("failed to move " + tmp);
            progress.onStep("installed " + name + " " + resolved);
        } finally {
            if (archive.exists()) archive.delete();
            if (tmp.exists()) delete(tmp);
        }
        JsonArray requires = info.getAsJsonArray("requires_dist");
        if (requires != null) {
            for (JsonElement element : requires) {
                String dep = dependencyName(element.getAsString());
                if (dep == null || dep.equals(name)) continue;
                try {
                    installInternal(dep, progress, visited, depth + 1);
                } catch (Exception e) {
                    delete(target);
                    throw new IOException("dependency " + dep + " not installable: " + e.getMessage());
                }
            }
        }
    }

    private static JsonObject findWheel(JsonArray files) {
        if (files == null) return null;
        for (JsonElement element : files) {
            JsonObject file = element.getAsJsonObject();
            String filename = str(file, "filename").toLowerCase(Locale.US);
            if (!"bdist_wheel".equals(str(file, "packagetype"))) continue;
            if (!filename.endsWith(".whl") || !filename.contains("-none-any.whl")) continue;
            if (filename.contains("-py2-none-any.whl")) continue;
            return file;
        }
        return null;
    }

    private static String dependencyName(String require) {
        if (require == null) return null;
        String value = require.trim();
        int marker = value.indexOf(';');
        if (marker >= 0) {
            String condition = value.substring(marker + 1).trim().toLowerCase(Locale.US);
            if (condition.contains("extra ==")) return null;
            if (condition.contains("sys_platform") && (condition.contains("win32") || condition.contains("darwin"))) return null;
            value = value.substring(0, marker);
        }
        Matcher matcher = DEP_NAME.matcher(value.trim());
        return matcher.find() ? normalize(matcher.group()) : null;
    }

    private static boolean isInstalled(String name) {
        for (PythonPackage item : getInstalled()) if (normalize(item.getName()).equals(name)) return true;
        return isBundled(name);
    }

    private static boolean isBundled(String name) {
        if (!com.chaquo.python.Python.isStarted()) return false;
        try {
            com.chaquo.python.Python.getInstance().getModule("importlib.metadata").callAttr("version", name);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static PythonPackage read(File dir) {
        File[] infos = dir.listFiles(file -> file.isDirectory() && file.getName().endsWith(".dist-info"));
        if (infos != null && infos.length > 0) {
            File metadata = new File(infos[0], "METADATA");
            if (metadata.exists()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(metadata), StandardCharsets.UTF_8))) {
                    String name = null;
                    String version = null;
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.isEmpty()) break;
                        if (line.startsWith("Name:")) name = line.substring(5).trim();
                        else if (line.startsWith("Version:")) version = line.substring(8).trim();
                    }
                    if (name != null) return new PythonPackage(name, version, dir);
                } catch (IOException ignored) {
                }
            }
        }
        String folder = dir.getName();
        int split = folder.lastIndexOf('-');
        if (split > 0) return new PythonPackage(folder.substring(0, split), folder.substring(split + 1), dir);
        return new PythonPackage(folder, "", dir);
    }

    private static JsonObject fetch(String url) throws IOException {
        Request request = new Request.Builder().url(url).header("User-Agent", "FongMiTV-PythonManager").build();
        try (Response response = OkHttp.client().newCall(request).execute()) {
            if (response.code() == 404) throw new IOException("not found on PyPI");
            if (!response.isSuccessful()) throw new IOException("PyPI HTTP " + response.code());
            ResponseBody body = response.body();
            if (body == null) throw new IOException("empty response");
            return JsonParser.parseString(body.string()).getAsJsonObject();
        }
    }

    private static void download(String url, File target) throws IOException {
        Request request = new Request.Builder().url(url).header("User-Agent", "FongMiTV-PythonManager").build();
        try (Response response = OkHttp.client().newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("wheel HTTP " + response.code());
            ResponseBody body = response.body();
            if (body == null) throw new IOException("empty wheel");
            try (InputStream in = body.byteStream(); OutputStream out = new FileOutputStream(target)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            }
        }
    }

    private static void verify(File file, JsonObject wheel) throws Exception {
        try {
            JsonObject digests = wheel.getAsJsonObject("digests");
            String sha256 = digests == null ? null : str(digests, "sha256");
            if (sha256 == null || sha256.isEmpty()) return;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) digest.update(buffer, 0, read);
            }
            StringBuilder hex = new StringBuilder();
            for (byte b : digest.digest()) hex.append(String.format(Locale.US, "%02x", b));
            if (!hex.toString().equals(sha256)) throw new IOException("sha256 mismatch");
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("sha256 check failed", e);
        }
    }

    private static void extract(File archive, File target) throws IOException {
        String base = target.getCanonicalPath() + File.separator;
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(archive)))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("__MACOSX/") || name.startsWith(".")) continue;
                File out = new File(target, name);
                if (!out.getCanonicalPath().startsWith(base)) throw new IOException("unsafe entry " + name);
                if (entry.isDirectory()) {
                    if (!out.mkdirs() && !out.isDirectory()) throw new IOException("failed to create " + out);
                } else {
                    File parent = out.getParentFile();
                    if (parent != null && !parent.exists() && !parent.mkdirs()) throw new IOException("failed to create " + parent);
                    try (OutputStream os = new FileOutputStream(out)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = zip.read(buffer)) != -1) os.write(buffer, 0, read);
                    }
                }
            }
        }
    }

    private static String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.US).replaceAll("[-_.]+", "-");
    }

    private static String safeName(String name) {
        return normalize(name).replace('-', '_');
    }

    private static String str(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element == null || element.isJsonNull() ? "" : element.getAsString();
    }

    private static void delete(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) delete(child);
        file.delete();
    }
}
