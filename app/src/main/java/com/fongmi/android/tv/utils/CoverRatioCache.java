package com.fongmi.android.tv.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 封面高宽比缓存：竖图（高/宽 ≥ 1.6）视为短剧封面，点击时可直接进短剧播放。 */
public class CoverRatioCache {

    private static final int MAX_SIZE = 200;
    public static final float SHORT_DRAMA_THRESHOLD = 1.6f;

    private static final Map<String, Float> CACHE = Collections.synchronizedMap(new LinkedHashMap<String, Float>(MAX_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Float> eldest) {
            return size() > MAX_SIZE;
        }
    });

    public static void put(String url, int width, int height) {
        if (url == null || url.isEmpty() || width <= 0 || height <= 0) return;
        CACHE.put(url, (float) height / (float) width);
    }

    public static boolean isShortDramaCover(String url) {
        if (url == null || url.isEmpty()) return false;
        Float ratio = CACHE.get(url);
        return ratio != null && ratio >= SHORT_DRAMA_THRESHOLD;
    }

    public static void clear() {
        CACHE.clear();
    }
}
