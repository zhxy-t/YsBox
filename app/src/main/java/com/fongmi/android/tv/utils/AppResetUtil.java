package com.fongmi.android.tv.utils;

import android.content.Context;

import com.github.catvod.utils.Prefers;

import java.io.File;

/**
 * 重置 App：清空偏好设置与私有数据后结束进程，避免依赖 pm clear 的 root 权限。
 */
public final class AppResetUtil {

    private AppResetUtil() {
    }

    public static void reset(Context context) {
        try {
            Prefers.getPrefers().edit().clear().commit();
        } catch (Exception ignored) {
        }
        File data = new File(context.getApplicationInfo().dataDir);
        delete(new File(data, "databases"));
        delete(new File(data, "shared_prefs"));
        delete(new File(data, "no_backup"));
        delete(context.getFilesDir());
        delete(context.getCacheDir());
        delete(context.getExternalFilesDir(null));
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private static void delete(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) for (File child : files) delete(child);
        }
        file.delete();
    }
}
