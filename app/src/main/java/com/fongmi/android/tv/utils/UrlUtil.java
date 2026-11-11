package com.fongmi.android.tv.utils;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.TextUtils;

import com.fongmi.android.tv.server.Server;
import com.github.catvod.utils.Path;
import com.github.catvod.utils.UriUtil;
import com.google.common.net.HttpHeaders;

import java.io.File;

public class UrlUtil {

    public static Uri uri(String url) {
        // null 兜底：外部推送/剪贴板/导入插件事件可能带 null 文本，
        // 直接 trim() 会 NPE（VideoActivity.push 的崩溃根因）。
        if (url == null) url = "";
        url = url.trim().replace("\\", "");
        return url.startsWith("/") ? Uri.fromFile(new File(url)) : Uri.parse(url);
    }

    public static String scheme(String url) {
        return url == null ? "" : scheme(uri(url));
    }

    public static String scheme(Uri uri) {
        String scheme = uri.getScheme();
        return scheme == null ? "" : scheme.toLowerCase().trim();
    }

    public static String host(String url) {
        return url == null ? "" : host(uri(url));
    }

    public static String host(Uri uri) {
        String host = uri.getHost();
        return host == null ? "" : host.toLowerCase().trim();
    }

    public static String path(String url) {
        return url == null ? "" : path(uri(url));
    }

    public static String path(Uri uri) {
        String path = uri.getLastPathSegment();
        return path == null ? "" : path.trim();
    }

    public static String toLocalUrl(Uri uri) {
        String path = uri.getPath();
        String root = Path.rootPath();
        if (!ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme()) || TextUtils.isEmpty(path) || !path.startsWith(root + File.separator)) return uri.toString();
        return "file://" + path.substring(root.length() + 1);
    }

    /**
     * 把 SAF 的 externalstorage document 地址还原成本地 file:// 地址，
     * 例如 content://com.android.externalstorage.documents/document/primary%3AYsBox%2F优雅.json
     * 转为 file://YsBox/优雅.json。其他地址原样返回。
     */
    public static String toLocalUrl(String url) {
        if (TextUtils.isEmpty(url) || !url.startsWith("content://com.android.externalstorage.documents/")) return url;
        try {
            String[] split = DocumentsContract.getDocumentId(Uri.parse(url)).split(":", 2);
            if (split.length < 2) return url;
            File file = "primary".equalsIgnoreCase(split[0]) ? new File(Environment.getExternalStorageDirectory(), split[1]) : new File("/storage/" + split[0] + "/" + split[1]);
            return toLocalUrl(Uri.fromFile(file));
        } catch (RuntimeException e) {
            return url;
        }
    }

    public static String resolve(String baseUri, String referenceUri) {
        return UriUtil.resolve(baseUri, referenceUri);
    }

    public static String convert(String url) {
        String scheme = scheme(url);
        String prefix = scheme + "://";
        return switch (scheme) {
            case "assets" -> url.replace(prefix, Server.get().getAddress("/"));
            case "proxy" -> url.replace(prefix, Server.get().getAddress("/proxy?"));
            case "file" -> Server.get().getAddress("/file/") + Uri.encode(url.substring((prefix).length()), "/");
            default -> url;
        };
    }

    public static String getName(String url) {
        Uri uri = uri(url);
        String path = path(uri);
        String host = host(uri);
        return !path.isEmpty() ? path : !host.isEmpty() ? host : url;
    }

    public static String fixHeader(String key) {
        if (HttpHeaders.USER_AGENT.equalsIgnoreCase(key)) return HttpHeaders.USER_AGENT;
        if (HttpHeaders.REFERER.equalsIgnoreCase(key)) return HttpHeaders.REFERER;
        if (HttpHeaders.COOKIE.equalsIgnoreCase(key)) return HttpHeaders.COOKIE;
        return key;
    }
}
