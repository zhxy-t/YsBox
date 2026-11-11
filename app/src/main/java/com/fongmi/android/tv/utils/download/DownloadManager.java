package com.fongmi.android.tv.utils.download;

import android.text.TextUtils;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.setting.DownloadSetting;
import com.github.catvod.net.OkHttp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadManager {

    public interface Listener {
        void onChanged();
    }

    private static volatile DownloadManager instance;

    private final ConcurrentHashMap<String, DownloadTask> tasks = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    private volatile ExecutorService executor;
    private volatile int executorThreads;

    private DownloadManager() {
        loadFromDisk();
    }

    private void loadFromDisk() {
        File[] files = DownloadSetting.getDir().listFiles((dir, name) -> !name.endsWith(".part"));
        if (files == null) return;
        for (File file : files) {
            DownloadTask task = new DownloadTask(file.getAbsolutePath(), file.getName(), null);
            task.setFilePath(file.getAbsolutePath());
            task.setTotal(file.length());
            task.setDownloaded(file.length());
            task.setStatus(DownloadTask.DONE);
            tasks.put(task.getId(), task);
        }
    }

    public static DownloadManager get() {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) instance = new DownloadManager();
            }
        }
        return instance;
    }

    public void addListener(Listener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void notifyChanged() {
        App.post(() -> {
            for (Listener listener : listeners) listener.onChanged();
        });
    }

    public List<DownloadTask> getTasks() {
        List<DownloadTask> list = new ArrayList<>(tasks.values());
        list.sort((a, b) -> Long.compare(b.getCreateTime(), a.getCreateTime()));
        return list;
    }

    public DownloadTask add(String url, String name) {
        return add(url, name, new java.util.HashMap<>());
    }

    public DownloadTask add(String url, String name, Map<String, String> headers) {
        if (TextUtils.isEmpty(url)) return null;
        if (TextUtils.isEmpty(name)) name = guessName(url);
        DownloadTask task = new DownloadTask(url, name, headers);
        File file = target(task.getName(), extension(url));
        task.setFilePath(file.getAbsolutePath());
        tasks.put(task.getId(), task);
        notifyChanged();
        start(task);
        return task;
    }

    public void cancel(String id) {
        DownloadTask task = tasks.get(id);
        if (task == null || task.isFinished()) return;
        task.setStatus(DownloadTask.CANCELED);
        Call call = task.getCall();
        if (call != null) call.cancel();
        notifyChanged();
    }

    public void retry(String id) {
        DownloadTask task = tasks.get(id);
        if (task == null || !task.isFinished()) return;
        task.setStatus(DownloadTask.WAITING);
        task.setDownloaded(0);
        task.setTotal(-1);
        task.setError(null);
        notifyChanged();
        start(task);
    }

    public void remove(String id) {
        DownloadTask task = tasks.get(id);
        if (task == null) return;
        cancel(id);
        if (task.getFilePath() != null) new File(task.getFilePath()).delete();
        tasks.remove(id);
        notifyChanged();
    }

    public void clearFinished() {
        boolean changed = false;
        for (DownloadTask task : new ArrayList<>(tasks.values())) {
            if (task.getStatus() == DownloadTask.DONE) {
                if (task.getFilePath() != null) new File(task.getFilePath()).delete();
                tasks.remove(task.getId());
                changed = true;
            }
        }
        if (changed) notifyChanged();
    }

    private synchronized ExecutorService executor() {
        int threads = DownloadSetting.getThreads();
        if (executor == null || executorThreads != threads) {
            if (executor != null) executor.shutdown();
            executor = Executors.newFixedThreadPool(threads);
            executorThreads = threads;
        }
        return executor;
    }

    private void start(DownloadTask task) {
        executor().execute(() -> download(task));
    }

    private void download(DownloadTask task) {
        task.setStatus(DownloadTask.RUNNING);
        notifyChanged();
        File part = new File(task.getFilePath() + ".part");
        Response response = null;
        try {
            Request.Builder builder = new Request.Builder().url(task.getUrl());
            for (Map.Entry<String, String> entry : task.getHeaders().entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
            Call call = OkHttp.client().newCall(builder.build());
            task.setCall(call);
            response = call.execute();
            if (!response.isSuccessful()) throw new IllegalStateException("HTTP " + response.code());
            ResponseBody body = response.body();
            if (body == null) throw new IllegalStateException("empty body");
            task.setTotal(body.contentLength());
            long lastNotify = 0;
            try (InputStream in = body.byteStream(); OutputStream out = new FileOutputStream(part)) {
                byte[] buffer = new byte[16384];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    if (task.getStatus() == DownloadTask.CANCELED) break;
                    out.write(buffer, 0, len);
                    task.addDownloaded(len);
                    long now = System.currentTimeMillis();
                    if (now - lastNotify > 500) {
                        lastNotify = now;
                        notifyChanged();
                    }
                }
            }
            if (task.getStatus() == DownloadTask.CANCELED) {
                part.delete();
                return;
            }
            long actual = part.length();
            File target = new File(task.getFilePath());
            if (target.exists() && !target.delete()) throw new IllegalStateException("unable to replace file");
            if (!part.renameTo(target)) throw new IllegalStateException("unable to move file");
            if (task.getTotal() <= 0) task.setTotal(actual);
            task.setDownloaded(actual);
            task.setStatus(DownloadTask.DONE);
        } catch (Exception e) {
            part.delete();
            if (task.getStatus() == DownloadTask.CANCELED) {
                task.setStatus(DownloadTask.CANCELED);
            } else {
                task.setError(e.getMessage());
                task.setStatus(DownloadTask.ERROR);
            }
        } finally {
            if (response != null) response.close();
            task.setCall(null);
            notifyChanged();
        }
    }

    public static boolean isSupported(String url) {
        return !TextUtils.isEmpty(url) && !url.contains(".m3u8") && !url.startsWith("magnet:");
    }

    private File target(String name, String ext) {
        File dir = DownloadSetting.getDir();
        File file = new File(dir, sanitize(name) + ext);
        int index = 1;
        while (file.exists()) file = new File(dir, sanitize(name) + "_" + index++ + ext);
        return file;
    }

    private static String sanitize(String name) {
        String value = name == null ? "" : name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return value.isEmpty() ? "video" : value;
    }

    private static String extension(String url) {
        String path = url;
        int query = path.indexOf('?');
        if (query >= 0) path = path.substring(0, query);
        int index = path.lastIndexOf('.');
        if (index < 0) return ".mp4";
        String ext = path.substring(index).toLowerCase(java.util.Locale.ROOT);
        return ext.length() > 6 ? ".mp4" : ext;
    }

    private static String guessName(String url) {
        String path = url;
        int query = path.indexOf('?');
        if (query >= 0) path = path.substring(0, query);
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }
}
