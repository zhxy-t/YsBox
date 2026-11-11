package com.fongmi.android.tv.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.github.catvod.net.OkHttp;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 视频宽高后台预取：HLS playlist 取 RESOLUTION、MP4 头部取 avc1/tkhd 宽高。
 * 供进入全屏前精准判断横竖屏，避免先横后竖闪一下。
 * 所有网络操作在后台线程，失败静默丢弃。
 */
public class VideoDimensionFetcher {

    private static final long FETCH_TIMEOUT_MS = 5000;
    private static final int MP4_HEADER_SIZE = 64 * 1024;

    private static final Pattern RESOLUTION = Pattern.compile("RESOLUTION=(\\d+)x(\\d+)");
    private static final Pattern BANDWIDTH = Pattern.compile("BANDWIDTH=(\\d+)");

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "video-dim-fetch");
        t.setDaemon(true);
        return t;
    });
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final java.util.Set<String> PENDING = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    public interface Callback {
        void onResult(String url, int width, int height);
    }

    public static void preFetchDebounced(String url) {
        if (TextUtils.isEmpty(url)) return;
        if (VideoDimensionCache.get(url) != null) return;
        String low = url.toLowerCase();
        if (!low.startsWith("http://") && !low.startsWith("https://")) return;
        if (!PENDING.add(url)) return;
        MAIN.postDelayed(() -> {
            PENDING.remove(url);
            preFetch(url);
        }, 500);
    }

    public static void preFetch(String url) {
        fetch(url, null);
    }

    public static void fetch(String url, Callback callback) {
        if (TextUtils.isEmpty(url)) return;
        int[] cached = VideoDimensionCache.get(url);
        if (cached != null) {
            if (callback != null) {
                int w = cached[0], h = cached[1];
                MAIN.post(() -> callback.onResult(url, w, h));
            }
            return;
        }
        String low = url.toLowerCase();
        if (!low.startsWith("http://") && !low.startsWith("https://")) return;
        EXECUTOR.execute(() -> {
            try {
                int[] wh = doFetch(url);
                if (wh != null) {
                    VideoDimensionCache.put(url, wh[0], wh[1]);
                    if (callback != null) {
                        MAIN.post(() -> callback.onResult(url, wh[0], wh[1]));
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    private static int[] doFetch(String url) throws Exception {
        String low = url.toLowerCase();
        if (low.contains(".m3u8") || low.contains("m3u8")) return fetchHls(url);
        return fetchMp4Header(url);
    }

    private static OkHttpClient client() {
        return OkHttp.client().newBuilder()
                .connectTimeout(FETCH_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(FETCH_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .callTimeout(FETCH_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build();
    }

    private static String get(String url, String range) throws IOException {
        Request.Builder builder = new Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0 Mobile Safari/537.36");
        if (!TextUtils.isEmpty(range)) builder.header("Range", range);
        try (Response resp = client().newCall(builder.build()).execute()) {
            if (!resp.isSuccessful()) return null;
            ResponseBody body = resp.body();
            return body == null ? null : body.string();
        }
    }

    private static byte[] getBytes(String url, String range) throws IOException {
        Request.Builder builder = new Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0 Mobile Safari/537.36");
        if (!TextUtils.isEmpty(range)) builder.header("Range", range);
        try (Response resp = client().newCall(builder.build()).execute()) {
            if (!resp.isSuccessful()) return null;
            ResponseBody body = resp.body();
            return body == null ? null : body.bytes();
        }
    }

    private static int[] fetchHls(String url) {
        try {
            String playlist = get(url, null);
            if (TextUtils.isEmpty(playlist)) return null;
            String[] lines = playlist.split("\n");
            int bestW = 0, bestH = 0;
            long bestBw = -1;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.startsWith("#EXT-X-STREAM-INF")) continue;
                Matcher rm = RESOLUTION.matcher(line);
                if (!rm.find()) continue;
                int w = Integer.parseInt(rm.group(1));
                int h = Integer.parseInt(rm.group(2));
                long bw = 0;
                Matcher bm = BANDWIDTH.matcher(line);
                if (bm.find()) {
                    try { bw = Long.parseLong(bm.group(1)); } catch (Exception ignored) {}
                }
                if (bw >= bestBw) { bestBw = bw; bestW = w; bestH = h; }
            }
            if (bestW > 0 && bestH > 0) return new int[]{bestW, bestH};
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String variant = resolveUrl(url, line);
                if (variant != null && !variant.equals(url)) return fetchHls(variant);
                break;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String resolveUrl(String base, String relative) {
        try {
            if (relative.startsWith("http://") || relative.startsWith("https://")) return relative;
            int slash = base.lastIndexOf('/');
            if (slash < 0) return null;
            return base.substring(0, slash + 1) + relative;
        } catch (Exception e) {
            return null;
        }
    }

    private static int[] fetchMp4Header(String url) {
        try {
            byte[] data = getBytes(url, "bytes=0-" + (MP4_HEADER_SIZE - 1));
            if (data == null || data.length < 100) return null;
            return parseAvc1Dimensions(data);
        } catch (Exception e) {
            return null;
        }
    }

    private static int readU16(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }

    private static int readU32(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
    }

    private static int[] parseAvc1Dimensions(byte[] data) {
        try {
            for (int i = 0; i < data.length - 100; i++) {
                if (data[i] == 'a' && data[i + 1] == 'v' && data[i + 2] == 'c' && data[i + 3] == '1') {
                    int base = i + 8;
                    int widthOffset = base + 8 + 30;
                    int heightOffset = widthOffset + 2;
                    if (heightOffset + 1 >= data.length) continue;
                    int w = readU16(data, widthOffset);
                    int h = readU16(data, heightOffset);
                    if (w >= 16 && w <= 8192 && h >= 16 && h <= 8192) return new int[]{w, h};
                }
            }
            for (int i = 0; i < data.length - 100; i++) {
                if (data[i] == 't' && data[i + 1] == 'k' && data[i + 2] == 'h' && data[i + 3] == 'd') {
                    int base = i + 8;
                    if (base >= data.length) continue;
                    int version = data[base] & 0xFF;
                    int wOff = version == 0 ? base + 76 : base + 88;
                    int hOff = version == 0 ? base + 80 : base + 92;
                    if (hOff + 3 >= data.length) continue;
                    int w = readU32(data, wOff) >> 16;
                    int h = readU32(data, hOff) >> 16;
                    if (w >= 16 && w <= 8192 && h >= 16 && h <= 8192) return new int[]{w, h};
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
