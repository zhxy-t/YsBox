package com.fongmi.android.tv.ui.dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.widget.TextView;
import com.fongmi.android.tv.setting.Setting;
/**
 * 统一「非弹窗胶囊」主题入口：背景风格 / 原始弧度 / 内边距 / 选中态联动 / 超长滚动。
 *  - 弧度与内边距保持与原版 XML drawable 完全一致，不因换背景而走样；
 *  - 选中态用 activated（背景加浓 + 描边/文字跟随弹窗强调色），marquee 用 selected；
 *  - 文字超长一律跑马灯滚动，滚动开关由 select() 控制。
 */
public final class ChipThemeHelper {
    public static final int STYLE_DARK = 0;
    public static final int STYLE_GLASS = 1;
    /** 胶囊内边距按弧度档位（与原版 drawable 的 <padding> 一致） */
    private static final int[] PADDING_28 = {14, 8};   // selector_chip_glass / shape_item_round
    private static final int[] PADDING_16 = {14, 8};   // selector_detail_chip_glass
    private static final int[] PADDING_8  = {8, 8};    // shape_search_hot_word
    private static final int[] PADDING_FILTER = {12, 8}; // shape_accent 筛选胶囊
    private static final int TEXT_NORMAL = 0xFFFFFFFF;
    private static final int TEXT_DARK = 0xFF101216;
    /** 胶囊文字色必须跟随自身底色：白色磨砂玻璃用深色字，半透明深灰黑用白字。 */
    public static int textNormal() { return getStyle() == STYLE_GLASS ? TEXT_DARK : TEXT_NORMAL; }
    /** 详情卡片与线路胶囊同源：同一底色 + 同一描边，保证「胶囊背景」设置同时控制两者。 */
    public static int cardColor()  { return getStyle() == STYLE_GLASS ? GLASS_NORMAL : DARK_NORMAL; }
    public static int cardStroke() { return getStyle() == STYLE_GLASS ? GLASS_STROKE_NORMAL : DARK_STROKE_NORMAL; }
    private static final int DARK_NORMAL = 0x8014181F;
    private static final int DARK_STROKE_NORMAL = 0x26FFFFFF;
    private static final int GLASS_NORMAL = 0x40FFFFFF;
    private static final int GLASS_SELECTED = 0xB3FFFFFF;
    private static final int GLASS_STROKE_NORMAL = 0x26FFFFFF;
    private ChipThemeHelper() {}
    public static int getStyle() { return Setting.getChipStyle(); }
    public static String nameOf(int style) { return style == STYLE_GLASS ? "白色磨砂玻璃" : "半透明深灰黑"; }
    /** 按原版弧度档位取内边距 */
    private static int[] paddingFor(float radiusDp) {
        if (radiusDp >= 28f) return PADDING_28;
        if (radiusDp >= 17f) return PADDING_16;
        if (radiusDp >= 9f)  return PADDING_FILTER;
        return PADDING_8;
    }
    /** 生成胶囊背景（含 activated/聚焦描边），半径 dp */
    public static GradientDrawable background(Context ctx, float radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        float density = ctx.getResources().getDisplayMetrics().density;
        bg.setCornerRadius(radiusDp * density);
        int style = getStyle();
        bg.setColor(style == STYLE_GLASS ? GLASS_NORMAL : DARK_NORMAL);
        bg.setStroke(Math.round(1 * density),
                style == STYLE_GLASS ? GLASS_STROKE_NORMAL : DARK_STROKE_NORMAL);
        return bg;
    }
    /**
     * 一次性给胶囊 TextView 应用「背景 + 内边距 + 文字色 + 滚动」：
     *  - 背景选中态(activated)：底色加浓、描边/文字用弹窗风格强调色；
     *  - 文字超长跑马灯，selected 为滚动开关（调用方用 select() 控制）；
     *  - 幂等：重复 apply 刷新背景/文字色，不重复叠加内边距。
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
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_activated}, selectedBg2);
        states.addState(new int[]{android.R.attr.state_focused}, selectedBg2);
        states.addState(new int[]{}, normalBg);
        RippleDrawable ripple = new RippleDrawable(
                ColorStateList.valueOf(0x33FFFFFF), states, null);
        view.setBackground(ripple);
        view.setTextColor(new ColorStateList(
                new int[][]{
                        {android.R.attr.state_activated},
                        {android.R.attr.state_focused},
                        {}},
                new int[]{accent, accent, textNormal()}));
        int[] pad = paddingFor(radiusDp);
        int px = Math.round(pad[0] * density), py = Math.round(pad[1] * density);
        view.setPadding(px, py, px, py);
        view.setSingleLine();
        view.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        view.setMarqueeRepeatLimit(-1);
        view.setHorizontalFadingEdgeEnabled(true);
        if (view.getTag(com.fongmi.android.tv.R.id.tag_chip_applied) == null) {
            view.setSelected(true);
            view.setTag(com.fongmi.android.tv.R.id.tag_chip_applied, Boolean.TRUE);
        }
    }
    /** 选中态开关：activated 背景联动 + marquee 滚动开关 */
    public static void select(TextView view, boolean on) {
        view.setActivated(on);
        view.setSelected(on);
    }
}
