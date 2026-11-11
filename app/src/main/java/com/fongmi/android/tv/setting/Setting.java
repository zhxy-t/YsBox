package com.fongmi.android.tv.setting;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.utils.Prefers;

import java.util.Locale;

public class Setting {

    private static final int MIN_WALL = 0;
    private static final int MAX_WALL = 4;
    private static final int MIN_WALL_TYPE = 0;
    private static final int MAX_WALL_TYPE = 2;
    private static final int MIN_SITE_MODE = 0;
    private static final int MAX_SITE_MODE = 1;
    private static final int MIN_SYNC_MODE = 0;
    private static final int MAX_SYNC_MODE = 2;
    private static final int MIN_FLAG = 0;
    private static final int MAX_FLAG = 1;
    // 弹窗风格：0=经典深色 1=纯白Material 2=暗色模式 3=iOS风格 4=霓虹渐变 5=原始（系统默认） 6=毛玻璃 7=紫夜 8=M3深色
    private static final int MIN_DIALOG_STYLE = 0;
    private static final int MAX_DIALOG_STYLE = 8;

    /** 全局字体缩放：0.85 / 1.0 / 1.15 / 1.3 / 1.5，最后一项 -1 表示跟随系统；默认跟随系统 */
    public static float[] getFontScales() {
        return new float[]{0.85f, 1.0f, 1.15f, 1.3f, 1.5f, -1f};
    }

    public static int getFontScaleIndex() {
        return Math.clamp(Prefers.getInt("font_scale_index", 5), 0, getFontScales().length - 1);
    }

    public static void putFontScaleIndex(int index) {
        Prefers.put("font_scale_index", Math.clamp(index, 0, getFontScales().length - 1));
    }

    public static float getFontScale() {
        return getFontScales()[getFontScaleIndex()];
    }

    /** 供所有 Activity 的 getResources() 覆盖：影响所有 sp 字号（-1 = 跟随系统，不覆盖） */
    public static Configuration applyFontScale(Configuration config) {
        float scale = getFontScale();
        if (scale > 0) config.fontScale = scale;
        return config;
    }

    public static String getSwitch(boolean value) {
        return ResUtil.getString(value ? R.string.setting_on : R.string.setting_off);
    }

    public static String getDoh() {
        return Prefers.getString("doh");
    }

    public static void putDoh(String doh) {
        Prefers.put("doh", doh);
    }

    public static String getUa() {
        return Prefers.getString("ua");
    }

    public static void putUa(String ua) {
        Prefers.put("ua", ua);
    }

    public static String getProxy() {
        return Prefers.getString("proxy");
    }

    public static void putProxy(String proxy) {
        Prefers.put("proxy", proxy);
    }

    public static boolean isCompactEpisodeTitle() {
        return Prefers.getBoolean("compact_episode_title");
    }

    public static void putCompactEpisodeTitle(boolean compact) {
        Prefers.put("compact_episode_title", compact);
    }

    public static int getBackupMode() {
        return Prefers.getInt("backup_mode", 0);
    }

    public static void putBackupMode(int mode) {
        Prefers.put("backup_mode", mode);
    }

    public static String getKeyword() {
        return Prefers.getString("keyword");
    }

    public static void putKeyword(String keyword) {
        Prefers.put("keyword", keyword);
    }

    public static String getHot() {
        return Prefers.getString("hot");
    }

    public static void putHot(String hot) {
        Prefers.put("hot", hot);
    }

    public static String getHotTv() {
        return Prefers.getString("hot_tv");
    }

    public static void putHotTv(String hot) {
        Prefers.put("hot_tv", hot);
    }

    public static String getHotMovie() {
        return Prefers.getString("hot_movie");
    }

    public static void putHotMovie(String hot) {
        Prefers.put("hot_movie", hot);
    }

    public static String getHotVariety() {
        return Prefers.getString("hot_variety");
    }

    public static void putHotVariety(String hot) {
        Prefers.put("hot_variety", hot);
    }

    public static int getWall() {
        return Math.clamp(Prefers.getInt("wall", 0), MIN_WALL, MAX_WALL);
    }

    /** 进程内缓存的随机壁纸编号：每次冷启动（进程重建）换一张，
     *  同一次使用过程中保持稳定 —— 避免进新页面 / 点模糊开关 / recreate 时反复换壁纸，
     *  也保证 CustomWallView 的 load() 与 getWallColor() 取到同一张、取色一致。 */
    private static int sWallRandom = 0;

    public static int getWallRandom() {
        if (sWallRandom < MIN_WALL + 1 || sWallRandom > MAX_WALL)
            sWallRandom = 1 + new java.util.Random().nextInt(Math.max(1, MAX_WALL));
        return sWallRandom;
    }

    /** 主动重新掷一次（供「换一张」类入口调用） */
    public static void resetWallRandom() {
        sWallRandom = 0;
    }

    public static void putWall(int wall) {
        Prefers.put("wall", Math.clamp(wall, MIN_WALL, MAX_WALL));
    }

    public static int getWallType() {
        return Math.clamp(Prefers.getInt("wall_type", 0), MIN_WALL_TYPE, MAX_WALL_TYPE);
    }

    public static void putWallType(int type) {
        Prefers.put("wall_type", Math.clamp(type, MIN_WALL_TYPE, MAX_WALL_TYPE));
    }

    public static boolean isWallBlur() {
        return Prefers.getBoolean("wall_blur");
    }

    public static void putWallBlur(boolean blur) {
        Prefers.put("wall_blur", blur);
    }

    public static int getThemeColor() {
        return Prefers.getInt("theme_color", -1);
    }

    public static void putThemeColor(int color) {
        Prefers.put("theme_color", color);
    }

    public static String getLanguage() {
        return Prefers.getString("language", "");
    }

    public static void putLanguage(String language) {
        Prefers.put("language", language);
    }

    public static int getDialogStyle() {
        return Math.clamp(Prefers.getInt("dialog_style", 3), MIN_DIALOG_STYLE, MAX_DIALOG_STYLE);
    }

    public static void putDialogStyle(int style) {
        Prefers.put("dialog_style", style);
    }

    public static Context wrapLocale(Context context) {
        Configuration config = new Configuration(context.getResources().getConfiguration());
        String tag = getLanguage();
        if (!TextUtils.isEmpty(tag)) config.setLocale(Locale.forLanguageTag(tag));
        // 字体缩放：在 Context 层就应用，保证 Activity 及所有 inflate 出来的 sp 字号一致生效
        float scale = getFontScale();
        if (scale <= 0) scale = android.content.res.Resources.getSystem().getConfiguration().fontScale;
        config.fontScale = scale;
        return context.createConfigurationContext(config);
    }

    public static int getWallColor() {
        return Prefers.getInt("wall_color", 0);
    }

    public static void putWallColor(int color) {
        Prefers.put("wall_color", color);
    }

    public static int getDynamicColor() {
        int color = getThemeColor();
        if (color == -1) return 0;
        return color != 0 ? color : getWallColor();
    }

    public static int getSiteMode() {
        return Math.clamp(Prefers.getInt("site_mode"), MIN_SITE_MODE, MAX_SITE_MODE);
    }

    public static void putSiteMode(int mode) {
        Prefers.put("site_mode", Math.clamp(mode, MIN_SITE_MODE, MAX_SITE_MODE));
    }

    public static int getSiteColumn() {
        return Prefers.getInt("site_column", 1);
    }

    public static void putSiteColumn(int column) {
        Prefers.put("site_column", column);
    }

    public static int getSearchColumn() {
        return Math.min(Math.max(Prefers.getInt("search_column", 1), 1), 2);
    }

    public static void putSearchColumn(int column) {
        Prefers.put("search_column", column == 2 ? 2 : 1);
    }

    public static boolean isSiteHealthSort() {
        return Prefers.getBoolean("site_health_sort", true);
    }

    public static void putSiteHealthSort(boolean sort) {
        Prefers.put("site_health_sort", sort);
    }

    public static boolean isSiteHealthDialogSort() {
        return Prefers.getBoolean("site_health_dialog_sort");
    }

    public static void putSiteHealthDialogSort(boolean sort) {
        Prefers.put("site_health_dialog_sort", sort);
    }

    public static int getSyncMode() {
        return Math.clamp(Prefers.getInt("sync_mode"), MIN_SYNC_MODE, MAX_SYNC_MODE);
    }

    public static void putSyncMode(int mode) {
        Prefers.put("sync_mode", Math.clamp(mode, MIN_SYNC_MODE, MAX_SYNC_MODE));
    }

    public static int getFlag() {
        return Math.clamp(Prefers.getInt("flag"), MIN_FLAG, MAX_FLAG);
    }

    public static void putFlag(int flag) {
        Prefers.put("flag", Math.clamp(flag, MIN_FLAG, MAX_FLAG));
    }

    public static boolean isAggregatedSearch() {
        return Prefers.getBoolean("aggregated_search");
    }

    public static void putAggregatedSearch(boolean search) {
        Prefers.put("aggregated_search", search);
    }

    public static boolean isIncognito() {
        return Prefers.getBoolean("incognito");
    }

    public static void putIncognito(boolean incognito) {
        Prefers.put("incognito", incognito);
    }

    public static boolean isDrpyBuiltin() {
        return Prefers.getBoolean("drpy_builtin", true);
    }

    public static void putDrpyBuiltin(boolean builtin) {
        Prefers.put("drpy_builtin", builtin);
    }

    public static boolean getUpdate() {
        return Prefers.getBoolean("update", true);
    }

    public static void putUpdate(boolean update) {
        Prefers.put("update", update);
    }

    public static boolean isAdblock() {
        return Prefers.getBoolean("adblock", true);
    }

    public static void putAdblock(boolean adblock) {
        Prefers.put("adblock", adblock);
    }

    public static java.util.List<String> getAdblockKeywords() {
        String json = Prefers.getString("adblock_keywords", "[]");
        try {
            java.util.List<String> list = new java.util.ArrayList<>();
            org.json.JSONArray array = new org.json.JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                list.add(array.optString(i));
            }
            return list;
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    public static void putAdblockKeywords(java.util.List<String> keywords) {
        try {
            org.json.JSONArray array = new org.json.JSONArray();
            for (String k : keywords) array.put(k);
            Prefers.put("adblock_keywords", array.toString());
        } catch (Exception e) {
            Prefers.put("adblock_keywords", "[]");
        }
    }

    public static void addAdblockKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;
        java.util.List<String> list = getAdblockKeywords();
        String k = keyword.trim();
        if (!list.contains(k)) {
            list.add(k);
            putAdblockKeywords(list);
        }
    }

    public static void removeAdblockKeyword(String keyword) {
        java.util.List<String> list = getAdblockKeywords();
        if (list.remove(keyword)) {
            putAdblockKeywords(list);
        }
    }

    public static boolean isZhuyin() {
        return Prefers.getBoolean("zhuyin");
    }

    public static void putZhuyin(boolean zhuyin) {
        Prefers.put("zhuyin", zhuyin);
    }
}
