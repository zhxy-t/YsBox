package com.fongmi.android.tv.bean;

import android.content.Context;
import android.content.SharedPreferences;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;

/**
 * 插件检测结果，按插件文件路径持久化。
 * UNKNOWN 未测 / OK 可用 / DEAD 异常 / TESTING 检测中（不持久化）。
 */
public final class PluginHealth {

    public static final int UNKNOWN = 0;
    public static final int OK = 1;
    public static final int DEAD = 3;
    public static final int TESTING = 4;

    private static final String PREF = "plugin_health";

    private PluginHealth() {
    }

    private static SharedPreferences pref() {
        return App.get().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static final class Result {

        public final int grade;
        public final long ms;

        Result(int grade, long ms) {
            this.grade = grade;
            this.ms = ms;
        }
    }

    public static Result get(String path) {
        String value = pref().getString(key(path), null);
        if (value == null) return new Result(UNKNOWN, 0);
        try {
            String[] parts = value.split(",");
            return new Result(Integer.parseInt(parts[0]), Long.parseLong(parts[1]));
        } catch (Throwable e) {
            return new Result(UNKNOWN, 0);
        }
    }

    public static void save(String path, int grade, long ms) {
        pref().edit().putString(key(path), grade + "," + ms).apply();
    }

    public static void clear(String path) {
        pref().edit().remove(key(path)).apply();
    }

    public static String label(int grade, long ms) {
        switch (grade) {
            case OK:
                return App.get().getString(R.string.plugin_health_ok, ms);
            case DEAD:
                return App.get().getString(R.string.plugin_health_dead);
            case TESTING:
                return App.get().getString(R.string.plugin_health_testing);
            default:
                return "";
        }
    }

    public static int color(int grade) {
        switch (grade) {
            case OK:
                return 0xFF1F9D5C;
            case DEAD:
                return 0xFFE5484D;
            default:
                return 0xFF8A8F98;
        }
    }

    private static String key(String path) {
        return "r_" + path;
    }
}
