package com.fongmi.android.tv.setting;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.accessibility.CaptioningManager;

import androidx.annotation.Nullable;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.SubtitleView;

import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.player.subtitle.ExternalFont;
import com.github.catvod.utils.Prefers;

public class SubtitleSetting {

    public static final int STYLE_SOURCE_ORIGINAL = 0;
    public static final int STYLE_SOURCE_SYSTEM = 1;
    public static final int STYLE_SOURCE_CUSTOM = 2;
    public static final int SECONDARY_SUBTITLE_OFF = -2;
    public static final int SECONDARY_SUBTITLE_AUTO = -1;
    public static final float MIN_SCALE = 0.5f;
    public static final float MAX_SCALE = 2.0f;
    public static final float MIN_POSITION = -20.0f;
    public static final float MAX_POSITION = 30.0f;
    public static final float MIN_OPACITY = 0.0f;
    public static final float MAX_OPACITY = 1.0f;
    public static final float MIN_EDGE_WIDTH = 0.0f;
    public static final float MAX_EDGE_WIDTH = 6.0f;
    public static final float MIN_SHADOW = 0.0f;
    public static final float MAX_SHADOW = 8.0f;
    public static final float MIN_SECONDARY_POSITION = 0.0f;
    public static final float MAX_SECONDARY_POSITION = 150.0f;
    public static final long MIN_DELAY_MS = -30000L;
    public static final long MAX_DELAY_MS = 30000L;
    public static final String LANGUAGE_FOLLOW_SYSTEM = "";
    public static final int SUBTITLE_COLOR_WHITE = Color.WHITE;
    public static final int SUBTITLE_COLOR_YELLOW = 0xFFFFFF00;
    public static final int SUBTITLE_COLOR_CYAN = 0xFF00E5FF;
    public static final int SUBTITLE_COLOR_GREEN = 0xFF76FF03;
    public static final int SUBTITLE_COLOR_ORANGE = 0xFFFF9800;
    public static final int SUBTITLE_COLOR_PINK = 0xFFFF80AB;
    public static final int SUBTITLE_COLOR_RED = 0xFFFF5252;
    public static final int SUBTITLE_COLOR_BLUE = 0xFF64B5F6;
    public static final int SUBTITLE_COLOR_GRAY = 0xFFBDBDBD;
    public static final int SUBTITLE_COLOR_BLACK = Color.BLACK;
    public static final int SUBTITLE_BACKGROUND_DIM = 0x66000000;
    public static final int SUBTITLE_BACKGROUND_BLACK = 0xCC000000;
    public static final int SUBTITLE_BACKGROUND_GRAY = 0xCC202020;
    private static final int DEFAULT_TEXT_COLOR = SUBTITLE_COLOR_WHITE;
    private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int DEFAULT_EDGE_TYPE = CaptionStyleCompat.EDGE_TYPE_OUTLINE;
    private static final int DEFAULT_EDGE_COLOR = SUBTITLE_COLOR_BLACK;
    private static final float DEFAULT_SCALE = 1.0f;
    private static final float DEFAULT_POSITION = 0.0f;
    private static final float DEFAULT_TEXT_OPACITY = 1.0f;
    private static final float DEFAULT_EDGE_OPACITY = 1.0f;
    private static final float DEFAULT_BACKGROUND_OPACITY = 1.0f;
    // WebHTV-pinned Media3 CaptionStyleCompat does not expose the older FM constants.
    private static final float DEFAULT_EDGE_WIDTH = 2.0f;
    private static final float DEFAULT_SHADOW = 2.0f;
    private static final int DEFAULT_SECONDARY_TRACK = SECONDARY_SUBTITLE_OFF;
    private static final float DEFAULT_SECONDARY_POSITION = 10.0f;

    public static String getSearchToken() {
        return Prefers.getString("subtitle_search_token", "");
    }

    public static String getEffectiveToken() {
        String userToken = getSearchToken();
        if (!TextUtils.isEmpty(userToken)) return userToken;
        return VodConfig.get().getConfig().getAssrt();
    }

    public static void putSearchToken(String token) {
        Prefers.put("subtitle_search_token", token);
    }

    public static int getStyleSource() {
        int legacy = Prefers.getBoolean("caption") ? STYLE_SOURCE_SYSTEM : STYLE_SOURCE_ORIGINAL;
        return Math.clamp(Prefers.getInt("subtitle_style_source", legacy), STYLE_SOURCE_ORIGINAL, STYLE_SOURCE_CUSTOM);
    }

    public static void putStyleSource(int source) {
        Prefers.put("subtitle_style_source", Math.clamp(source, STYLE_SOURCE_ORIGINAL, STYLE_SOURCE_CUSTOM));
    }

    public static boolean isSystemStyle() {
        return getStyleSource() == STYLE_SOURCE_SYSTEM;
    }

    public static boolean isCustomStyle() {
        return getStyleSource() == STYLE_SOURCE_CUSTOM;
    }

    public static float getScale() {
        float textSize = Prefers.getFloat("subtitle_text_size");
        float legacy = textSize == 0 ? DEFAULT_SCALE : textSize / SubtitleView.DEFAULT_TEXT_SIZE_FRACTION;
        return Math.clamp(Prefers.getFloat("subtitle_scale", legacy), MIN_SCALE, MAX_SCALE);
    }

    public static float getScale(Context context) {
        float scale = getScale();
        if (scale == DEFAULT_SCALE) scale *= getSystemCaptionFontScale(context);
        return scale;
    }

    private static float getSystemCaptionFontScale(Context context) {
        if (!isSystemStyle()) return 1.0f;
        CaptioningManager manager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        return manager == null ? 1.0f : manager.getFontScale();
    }

    public static void putScale(float value) {
        Prefers.put("subtitle_scale", Math.clamp(value, MIN_SCALE, MAX_SCALE));
    }

    public static float getPosition() {
        float value = Prefers.getFloat("subtitle_position", DEFAULT_POSITION);
        if (value != 0 && Math.abs(value) < 0.5f) value *= 100.0f;
        return Math.clamp(value, MIN_POSITION, MAX_POSITION);
    }

    public static void putPosition(float value) {
        Prefers.put("subtitle_position", Math.clamp(value, MIN_POSITION, MAX_POSITION));
    }

    public static int getTextBaseColor() {
        return Prefers.getInt("subtitle_foreground_color", DEFAULT_TEXT_COLOR);
    }

    public static void putTextColor(int color) {
        Prefers.put("subtitle_foreground_color", color);
    }

    public static int getTextColor() {
        return applyOpacity(getTextBaseColor(), getTextOpacity());
    }

    public static float getTextOpacity() {
        return getOpacity("subtitle_foreground_opacity", DEFAULT_TEXT_OPACITY);
    }

    public static void putTextOpacity(float opacity) {
        Prefers.put("subtitle_foreground_opacity", clampOpacity(opacity));
    }

    public static int getBackgroundBaseColor() {
        return Prefers.getInt("subtitle_background_color", DEFAULT_BACKGROUND_COLOR);
    }

    public static void putBackgroundColor(int color) {
        Prefers.put("subtitle_background_color", color);
    }

    public static int getBackgroundColor() {
        return applyOpacity(getBackgroundBaseColor(), getBackgroundOpacity());
    }

    public static float getBackgroundOpacity() {
        return getOpacity("subtitle_background_opacity", DEFAULT_BACKGROUND_OPACITY);
    }

    public static void putBackgroundOpacity(float opacity) {
        Prefers.put("subtitle_background_opacity", clampOpacity(opacity));
    }

    public static int getEdgeType() {
        return Math.clamp(Prefers.getInt("subtitle_edge_type", DEFAULT_EDGE_TYPE), CaptionStyleCompat.EDGE_TYPE_NONE, CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW);
    }

    public static void putEdgeType(int edgeType) {
        Prefers.put("subtitle_edge_type", Math.clamp(edgeType, CaptionStyleCompat.EDGE_TYPE_NONE, CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW));
    }

    public static int getEdgeBaseColor() {
        return Prefers.getInt("subtitle_edge_color", DEFAULT_EDGE_COLOR);
    }

    public static void putEdgeColor(int color) {
        Prefers.put("subtitle_edge_color", color);
    }

    public static int getEdgeColor() {
        return applyOpacity(getEdgeBaseColor(), getEdgeOpacity());
    }

    public static float getEdgeOpacity() {
        return getOpacity("subtitle_edge_opacity", DEFAULT_EDGE_OPACITY);
    }

    public static void putEdgeOpacity(float opacity) {
        Prefers.put("subtitle_edge_opacity", clampOpacity(opacity));
    }

    public static float getEdgeWidth() {
        return Math.clamp(Prefers.getFloat("subtitle_edge_width", DEFAULT_EDGE_WIDTH), MIN_EDGE_WIDTH, MAX_EDGE_WIDTH);
    }

    public static void putEdgeWidth(float value) {
        Prefers.put("subtitle_edge_width", Math.clamp(value, MIN_EDGE_WIDTH, MAX_EDGE_WIDTH));
    }

    public static float getShadow() {
        return Math.clamp(Prefers.getFloat("subtitle_shadow", DEFAULT_SHADOW), MIN_SHADOW, MAX_SHADOW);
    }

    public static void putShadow(float value) {
        Prefers.put("subtitle_shadow", Math.clamp(value, MIN_SHADOW, MAX_SHADOW));
    }

    public static int getSecondaryTrackId() {
        return clampSecondaryTrackId(Prefers.getInt("subtitle_secondary_track", DEFAULT_SECONDARY_TRACK));
    }

    public static void putSecondaryTrackId(int trackId) {
        Prefers.put("subtitle_secondary_track", clampSecondaryTrackId(trackId));
    }

    public static float getSecondaryPosition() {
        return Math.clamp(Prefers.getFloat("subtitle_secondary_position", DEFAULT_SECONDARY_POSITION), MIN_SECONDARY_POSITION, MAX_SECONDARY_POSITION);
    }

    public static void putSecondaryPosition(float value) {
        Prefers.put("subtitle_secondary_position", Math.clamp(value, MIN_SECONDARY_POSITION, MAX_SECONDARY_POSITION));
    }

    public static boolean isAutoMatch() {
        return Prefers.getBoolean("subtitle_auto_match", true);
    }

    public static void putAutoMatch(boolean autoMatch) {
        Prefers.put("subtitle_auto_match", autoMatch);
    }

    public static long getDelayMs() {
        return Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, Prefers.getLong("subtitle_delay_ms", 0L)));
    }

    public static void putDelayMs(long delayMs) {
        Prefers.put("subtitle_delay_ms", Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, delayMs)));
    }

    public static String getPreferredLanguage() {
        return Prefers.getString("subtitle_preferred_language", LANGUAGE_FOLLOW_SYSTEM);
    }

    public static void putPreferredLanguage(String language) {
        Prefers.put("subtitle_preferred_language", language == null ? LANGUAGE_FOLLOW_SYSTEM : language);
    }

    private static int clampSecondaryTrackId(int trackId) {
        return Math.max(trackId, SECONDARY_SUBTITLE_OFF);
    }

    @Nullable
    public static ExternalFont.Item getFont() {
        return ExternalFont.find(Prefers.getString("subtitle_font", ""));
    }

    public static void putFont(@Nullable ExternalFont.Item font) {
        Prefers.put("subtitle_font", font == null ? "" : font.fileName());
    }

    @Nullable
    public static String getFontFamily() {
        ExternalFont.Item font = getFont();
        return font == null ? null : font.familyName();
    }

    private static float getOpacity(String key, float defaultValue) {
        return clampOpacity(Prefers.getFloat(key, defaultValue));
    }

    private static float clampOpacity(float opacity) {
        return Math.clamp(opacity, MIN_OPACITY, MAX_OPACITY);
    }

    private static int applyOpacity(int color, float opacity) {
        return Color.argb(Math.round(Color.alpha(color) * clampOpacity(opacity)), Color.red(color), Color.green(color), Color.blue(color));
    }

    public static boolean isStyleForced() {
        return getStyleSource() != STYLE_SOURCE_ORIGINAL;
    }

    public static boolean isScaleForced() {
        return getScale() != DEFAULT_SCALE;
    }

    public static boolean isScaleApplied() {
        return isSystemStyle() || isScaleForced();
    }

    public static boolean isPositionSet() {
        return getPosition() != DEFAULT_POSITION;
    }

    /** EXO 侧使用：用户是否挑过任意非默认颜色（MPV 的 OSD 不用这个）。 */
    private static boolean hasCustomColorValue() {
        return getTextBaseColor() != DEFAULT_TEXT_COLOR
                || getEdgeBaseColor() != DEFAULT_EDGE_COLOR
                || getBackgroundBaseColor() != DEFAULT_BACKGROUND_COLOR;
    }

    public static CaptionStyleCompat getStyle(Context context) {
        CaptioningManager manager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        if (isSystemStyle()) return manager == null ? CaptionStyleCompat.DEFAULT : CaptionStyleCompat.createFromCaptionStyle(manager.getUserStyle());
        // 默认的「原始」样式过去固定返回 DEFAULT，导致用户在面板里挑的颜色永远不生效。
        // 只要挑过任意非默认颜色就构造自定义样式；系统样式仍优先。
        if (isCustomStyle() || hasCustomColorValue()) return new CaptionStyleCompat(getTextColor(), getBackgroundColor(), Color.TRANSPARENT, getEdgeType(), getEdgeColor(), null);
        return CaptionStyleCompat.DEFAULT;
    }
    public static void applyStyle(Context context, @Nullable SubtitleView subtitleView) {
        if (subtitleView == null) return;
        subtitleView.reset();
        subtitleView.setStyle(getStyle(context));
        boolean forced = isStyleForced() || hasCustomColorValue() || isScaleForced();
        // 用户显式调过颜色/大小后关闭内嵌样式，避免 ASS/SSA 自带颜色与字号把设置覆盖掉。
        subtitleView.setApplyEmbeddedStyles(!forced);
        subtitleView.setApplyEmbeddedFontSizes(!isScaleApplied());
        if (isScaleApplied()) subtitleView.setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * getScale(context));
        if (isPositionSet()) subtitleView.setBottomPosition(getPosition() / 100.0f);
    }

    public static void resetAdjust() {
        Prefers.put("subtitle_scale", DEFAULT_SCALE);
        Prefers.put("subtitle_position", DEFAULT_POSITION);
    }

    public static void resetStyle() {
        Prefers.put("subtitle_style_source", STYLE_SOURCE_ORIGINAL);
        Prefers.put("subtitle_foreground_color", DEFAULT_TEXT_COLOR);
        Prefers.put("subtitle_background_color", DEFAULT_BACKGROUND_COLOR);
        Prefers.put("subtitle_edge_type", DEFAULT_EDGE_TYPE);
        Prefers.put("subtitle_edge_color", DEFAULT_EDGE_COLOR);
        Prefers.put("subtitle_foreground_opacity", DEFAULT_TEXT_OPACITY);
        Prefers.put("subtitle_background_opacity", DEFAULT_BACKGROUND_OPACITY);
        Prefers.put("subtitle_edge_opacity", DEFAULT_EDGE_OPACITY);
        Prefers.put("subtitle_edge_width", DEFAULT_EDGE_WIDTH);
        Prefers.put("subtitle_shadow", DEFAULT_SHADOW);
        putFont(null);
    }

    public static void resetAdvanced() {
        Prefers.put("subtitle_secondary_track", DEFAULT_SECONDARY_TRACK);
        Prefers.put("subtitle_secondary_position", DEFAULT_SECONDARY_POSITION);
        Prefers.put("subtitle_auto_match", true);
        Prefers.put("subtitle_delay_ms", 0L);
        Prefers.put("subtitle_preferred_language", LANGUAGE_FOLLOW_SYSTEM);
    }

    public static boolean isEnabled() {
        return Prefers.getBoolean("subtitle_enabled", true);
    }

    public static boolean hasCustomColor() {
        return isCustomStyle();
    }

    public static int getColor() {
        return getTextColor();
    }

    public static void reset() {
        resetAdjust();
        resetStyle();
        resetAdvanced();
    }
}
