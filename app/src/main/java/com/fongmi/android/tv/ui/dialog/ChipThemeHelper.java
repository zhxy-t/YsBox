package com.fongmi.android.tv.ui.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.widget.TextView;

import com.fongmi.android.tv.setting.Setting;

/**
 * 统一「非弹窗胶囊」主题入口：控制软件内所有页面级胶囊/小卡片的背景风格。
 *  - chip_style = 0（默认）：半透明深灰黑底 + 白字；选中态描边/字体跟随弹窗风格强调色
 *  - chip_style = 1：白色磨砂玻璃底（旧观感）；选中态描边/字体跟随弹窗风格强调色
 * 与 DialogThemeHelper 的关系：背景底色独立可控，但「选中时的强调色」始终取
 * 当前弹窗风格（dialogAccent），保证全局选中态视觉一致。
 */
public final class ChipThemeHelper {

    public static final int STYLE_DARK = 0;
    public static final int STYLE_GLASS = 1;

    /** 默认半透明深灰黑（与详情页 hero 卡 #8014181F 同源） */
    private static final int DARK_NORMAL = 0x8014181F;
    private static final int DARK_STROKE_NORMAL = 0x26FFFFFF;
    /** 白磨砂玻璃 */
    private static final int GLASS_NORMAL = 0x40FFFFFF;
    private static final int GLASS_SELECTED = 0xB3FFFFFF;
    private static final int GLASS_STROKE_NORMAL = 0x26FFFFFF;

    private ChipThemeHelper() {}

    public static int getStyle() { return Setting.getChipStyle(); }

    public static String nameOf(int style) { return style == STYLE_GLASS ? "白色磨砂玻璃" : "半透明深灰黑"; }

    /** 生成胶囊背景（含选中/聚焦描边），半径 dp */
    public static GradientDrawable background(Context ctx, float radiusDp) {
        int style = getStyle();
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        float density = ctx.getResources().getDisplayMetrics().density;
        bg.setCornerRadius(radiusDp * density);
        int normal = style == STYLE_GLASS ? GLASS_NORMAL : DARK_NORMAL;
        int strokeNormal = style == STYLE_GLASS ? GLASS_STROKE_NORMAL : DARK_STROKE_NORMAL;
        bg.setColor(normal);
        bg.setStroke(Math.round(1 * density), strokeNormal);
        return bg;
    }

    /**
     * 一次性给胶囊 TextView 应用「背景 + 文字色」的选中态联动：
     *  常态：底色按 chip 风格，白字；
     *  选中/聚焦：底色加浓、描边与文字用弹窗风格强调色。
     */
    public static void apply(TextView view, float radiusDp) {
        Context ctx = view.getContext();
        int style = getStyle();
        float density = ctx.getResources().getDisplayMetrics().density;
        int accent = DialogThemeHelper.accent(ctx);
        int normal = style == STYLE_GLASS ? GLASS_NORMAL : DARK_NORMAL;
        int selectedBg = style == STYLE_GLASS ? GLASS_SELECTED : Color.argb(0xCC, 0x14, 0x18, 0x1F);
        int strokeNormal = style == STYLE_GLASS ? GLASS_STROKE_NORMAL : DARK_STROKE_NORMAL;

        GradientDrawable normalBg = new GradientDrawable();
        normalBg.setShape(GradientDrawable.RECTANGLE);
        normalBg.setCornerRadius(radiusDp * density);
        normalBg.setColor(normal);
        normalBg.setStroke(Math.round(density), strokeNormal);

        GradientDrawable selectedBg2 = new GradientDrawable();
        selectedBg2.setShape(GradientDrawable.RECTANGLE);
        selectedBg2.setCornerRadius(radiusDp * density);
        selectedBg2.setColor(selectedBg);
        selectedBg2.setStroke(Math.round(1.5f * density), accent);

        android.graphics.drawable.StateListDrawable states = new android.graphics.drawable.StateListDrawable();
        states.addState(new int[]{android.R.attr.state_selected}, selectedBg2);
        states.addState(new int[]{android.R.attr.state_focused}, selectedBg2);
        states.addState(new int[]{}, normalBg);

        RippleDrawable ripple = new RippleDrawable(
                ColorStateList.valueOf(0x33FFFFFF), states, null);
        view.setBackground(ripple);

        // 文字：常态白，选中/聚焦用弹窗强调色
        view.setTextColor(new ColorStateList(
                new int[][]{
                        {android.R.attr.state_selected},
                        {android.R.attr.state_focused},
                        {}},
                new int[]{accent, accent, 0xFFFFFFFF}));
    }
}
