package com.fongmi.android.tv.setting;

import com.fongmi.android.tv.App;
import com.github.catvod.utils.Prefers;

import java.io.File;

public class DownloadSetting {

    public static final int DEFAULT_THREADS = 3;
    public static final int MIN_THREADS = 1;
    public static final int MAX_THREADS = 8;
    public static final String DEFAULT_DIR = "/storage/emulated/0/Download/YsYs";

    public static int getThreads() {
        return Math.max(MIN_THREADS, Math.min(MAX_THREADS, Prefers.getInt("download_threads", DEFAULT_THREADS)));
    }

    public static void putThreads(int threads) {
        Prefers.put("download_threads", Math.max(MIN_THREADS, Math.min(MAX_THREADS, threads)));
    }

    public static String getDirPath() {
        String path = Prefers.getString("download_dir");
        return path == null || path.trim().isEmpty() ? DEFAULT_DIR : path.trim();
    }

    public static void putDirPath(String path) {
        Prefers.put("download_dir", path == null ? "" : path.trim());
    }

    public static File getDir() {
        File custom = new File(getDirPath());
        if (custom.exists() || custom.mkdirs()) return custom;
        File dir = App.get().getExternalFilesDir("download");
        if (dir == null) dir = new File(App.get().getFilesDir(), "download");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }
}
