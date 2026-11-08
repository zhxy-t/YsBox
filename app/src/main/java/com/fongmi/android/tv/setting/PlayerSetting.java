package com.fongmi.android.tv.setting;

import com.github.catvod.utils.Prefers;

public class PlayerSetting {

    public static final int ENGINE_EXO = 0;
    public static final int ENGINE_MPV = 1;
    public static final int ENGINE_IJK = 2;
    public static final int ENGINE_SYSTEM = 3;
    public static final int EXO = ENGINE_EXO;
    public static final int MPV = ENGINE_MPV;
    public static final int IJK = ENGINE_IJK;
    public static final int SYSTEM = ENGINE_SYSTEM;
    public static final int RENDER_SURFACE = 0;
    public static final int RENDER_TEXTURE = 1;
    public static final int MIN_SCALE = 0;
    public static final int MAX_SCALE = 4;
    private static final int MIN_SIZE = 0;
    private static final int MAX_SIZE = 3;
    private static final int MIN_BUFFER = 1;
    private static final int MAX_BUFFER = 10;
    private static final int MIN_BACKGROUND = 0;
    private static final int MAX_BACKGROUND = 2;
    private static final int MIN_COVER_RATIO = 0;
    private static final int MAX_COVER_RATIO = 3;
    private static final int MIN_COVER_RADIUS = 0;
    private static final int MAX_COVER_RADIUS = 5;
    public static final int RATIO_FOLLOW = 0;
    public static final int RATIO_PORTRAIT = 1;
    public static final int RATIO_LANDSCAPE = 2;
    public static final int RATIO_SQUARE = 3;
    public static final int RADIUS_FOLLOW = 0;
    public static final int RADIUS_0 = 1;
    public static final int RADIUS_4 = 2;
    public static final int RADIUS_8 = 3;
    public static final int RADIUS_12 = 4;
    public static final int RADIUS_CIRCLE = 5;

    public static int getEngine() {
        int engine = Prefers.getInt("player_engine", ENGINE_EXO);
        if (engine != ENGINE_EXO && engine != ENGINE_MPV) {
            engine = ENGINE_EXO;
            Prefers.put("player_engine", ENGINE_EXO);
        }
        return engine;
    }

    public static void putEngine(int engine) {
        Prefers.put("player_engine", engine == ENGINE_MPV ? ENGINE_MPV : ENGINE_EXO);
        if (isExo() && DecodeSetting.isTunnel()) putRender(RENDER_SURFACE);
    }

    public static boolean isExo() {
        return getEngine() == ENGINE_EXO;
    }

    public static boolean isMpv() {
        return getEngine() == ENGINE_MPV;
    }

    public static boolean isDebug() {
        return Prefers.getBoolean("player_debug");
    }

    public static void putDebug(boolean debug) {
        Prefers.put("player_debug", debug);
    }

    public static boolean isBlurayMenu() {
        return false;
    }

    public static boolean isLibass() {
        return Prefers.getBoolean("player_libass", true);
    }

    public static void putLibass(boolean libass) {
        Prefers.put("player_libass", libass);
    }

    public static int getRender() {
        return Math.clamp(Prefers.getInt("render", RENDER_SURFACE), RENDER_SURFACE, RENDER_TEXTURE);
    }

    public static void putRender(int render) {
        Prefers.put("render", Math.clamp(render, RENDER_SURFACE, RENDER_TEXTURE));
        if (isExo() && DecodeSetting.isTunnel() && getRender() == RENDER_TEXTURE) DecodeSetting.putTunnel(false);
    }

    public static int getSize() {
        return Math.clamp(Prefers.getInt("size", 2), MIN_SIZE, MAX_SIZE);
    }

    public static void putSize(int size) {
        Prefers.put("size", Math.clamp(size, MIN_SIZE, MAX_SIZE));
    }

    public static int getCoverRatio() {
        return Math.clamp(Prefers.getInt("cover_ratio", RATIO_FOLLOW), MIN_COVER_RATIO, MAX_COVER_RATIO);
    }

    public static void putCoverRatio(int ratio) {
        Prefers.put("cover_ratio", Math.clamp(ratio, MIN_COVER_RATIO, MAX_COVER_RATIO));
    }

    /** 0 表示跟随站点样式，其余返回封面宽高比。 */
    public static float getCoverRatioValue() {
        return switch (getCoverRatio()) {
            case RATIO_PORTRAIT -> 0.75f;
            case RATIO_LANDSCAPE -> 1.78f;
            case RATIO_SQUARE -> 1.0f;
            default -> 0f;
        };
    }

    public static int getCoverRadius() {
        return Math.clamp(Prefers.getInt("cover_radius", RADIUS_FOLLOW), MIN_COVER_RADIUS, MAX_COVER_RADIUS);
    }

    public static void putCoverRadius(int radius) {
        Prefers.put("cover_radius", Math.clamp(radius, MIN_COVER_RADIUS, MAX_COVER_RADIUS));
    }

    /** 返回 dp 值；FOLLOW 返回 -1，CIRCLE 返回 -1（由调用方按圆形处理）。 */
    public static int getCoverRadiusDp() {
        return switch (getCoverRadius()) {
            case RADIUS_0 -> 0;
            case RADIUS_4 -> 4;
            case RADIUS_8 -> 8;
            case RADIUS_12 -> 12;
            default -> -1;
        };
    }

    public static int getScale() {
        return Math.clamp(Prefers.getInt("scale"), MIN_SCALE, MAX_SCALE);
    }

    public static void putScale(int scale) {
        Prefers.put("scale", Math.clamp(scale, MIN_SCALE, MAX_SCALE));
    }


    public static int getEpisodeColumn() {
        return Math.min(Math.max(Prefers.getInt("episode_column", 2), 1), 2);
    }

    public static void putEpisodeColumn(int column) {
        Prefers.put("episode_column", column == 1 ? 1 : 2);
    }
    public static int getBackground() {
        return Math.clamp(Prefers.getInt("background", 2), MIN_BACKGROUND, MAX_BACKGROUND);
    }

    public static void putBackground(int background) {
        Prefers.put("background", Math.clamp(background, MIN_BACKGROUND, MAX_BACKGROUND));
    }

    public static boolean isBackgroundOff() {
        return getBackground() == 0;
    }

    public static boolean isBackgroundOn() {
        return getBackground() == 1 || getBackground() == 2;
    }

    public static boolean isBackgroundPiP() {
        return getBackground() == 2;
    }

    public static int getBuffer() {
        return Math.clamp(Prefers.getInt("buffer"), MIN_BUFFER, MAX_BUFFER);
    }

    public static void putBuffer(int buffer) {
        Prefers.put("buffer", Math.clamp(buffer, MIN_BUFFER, MAX_BUFFER));
    }

    public static int getPlayer() {
        return getEngine();
    }

    public static int sanitizePlayer(int player) {
        return player == ENGINE_MPV ? ENGINE_MPV : EXO;
    }

    public static int getBufferBytesOption() {
        return 0;
    }

    public static int getBackBufferOption() {
        return getBackBufferOption(getPlayer());
    }

    public static int getBackBufferOption(int kernel) {
        return 0;
    }

    public static int getPlayCacheOption() {
        return 0;
    }

    public static long getPlayCacheSize() {
        return getPlayCacheSize(getPlayer());
    }

    public static long getPlayCacheSize(int kernel) {
        return 128L * 1024L * 1024L;
    }

    public static boolean isCaption() {
        return SubtitleSetting.isSystemStyle();
    }

    public static boolean isAudioPrefer() {
        return DecodeSetting.isAudioPrefer();
    }

    public static boolean isAudioPrefer(int kernel) {
        return isAudioPrefer();
    }

    public static boolean isAudioPassThrough() {
        return DecodeSetting.isAudioPassThrough();
    }

    public static boolean isAudioPassThrough(int kernel) {
        return isAudioPassThrough();
    }

    public static boolean isVideoPrefer() {
        return DecodeSetting.isVideoPrefer();
    }

    public static boolean isVideoPrefer(int kernel) {
        return isVideoPrefer();
    }

    public static boolean isPreferAAC() {
        return DecodeSetting.isPreferAAC();
    }

    public static boolean isPreferAAC(int kernel) {
        return isPreferAAC();
    }

    public static boolean isOsdTitle() {
        return Prefers.getBoolean("player_osd_title", true);
    }

    public static void putOsdTitle(boolean value) {
        Prefers.put("player_osd_title", value);
    }

    public static boolean isOsdResolution() {
        return Prefers.getBoolean("player_osd_resolution", true);
    }

    public static void putOsdResolution(boolean value) {
        Prefers.put("player_osd_resolution", value);
    }

    public static boolean isOsdTime() {
        return Prefers.getBoolean("player_osd_time", false);
    }

    public static void putOsdTime(boolean value) {
        Prefers.put("player_osd_time", value);
    }

    public static boolean isOsdProgress() {
        return Prefers.getBoolean("player_osd_progress", true);
    }

    public static void putOsdProgress(boolean value) {
        Prefers.put("player_osd_progress", value);
    }

    public static boolean isOsdTraffic() {
        return Prefers.getBoolean("player_osd_traffic", false);
    }

    public static void putOsdTraffic(boolean value) {
        Prefers.put("player_osd_traffic", value);
    }

    public static boolean isOsdMini() {
        return Prefers.getBoolean("player_osd_mini", false);
    }

    public static void putOsdMini(boolean value) {
        Prefers.put("player_osd_mini", value);
    }

    public static boolean isOsdDiagnostics() {
        return Prefers.getBoolean("player_osd_diagnostics", false);
    }

    public static void putOsdDiagnostics(boolean value) {
        Prefers.put("player_osd_diagnostics", value);
    }

    public static boolean isOsdEnabled() {
        return isOsdTitle() || isOsdResolution() || isOsdTime() || isOsdProgress() || isOsdTraffic() || isOsdMini() || isOsdDiagnostics();
    }

    public static void resetOsd() {
        Prefers.put("player_osd_title", true);
        Prefers.put("player_osd_resolution", true);
        Prefers.put("player_osd_time", false);
        Prefers.put("player_osd_progress", true);
        Prefers.put("player_osd_traffic", false);
        Prefers.put("player_osd_mini", false);
        Prefers.put("player_osd_diagnostics", false);
    }

}
