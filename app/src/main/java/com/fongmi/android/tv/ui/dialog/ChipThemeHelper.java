package com.fongmi.android.tv.ui.dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
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
    /** 跟随弹窗风格：浅色弹窗用白磨砂，深色弹窗用半透明深灰黑，由弹窗明暗自动决定 */
    public static final int STYLE_FOLLOW = 2;
    /** 胶囊内边距按弧度档位（与原版 drawable 的 <padding> 一致） */
    private static final int[] PADDING_28 = {14, 8};   // selector_chip_glass / shape_item_round
    private static final int[] PADDING_16 = {14, 8};   // selector_detail_chip_glass
    private static final int[] PADDING_8  = {8, 8};    // shape_search_hot_word
    private static final int[] PADDING_FILTER = {12, 8}; // shape_accent 筛选胶囊
    private static final int TEXT_NORMAL = 0xFFFFFFFF;
    private static final int TEXT_DARK = 0xFF101216;
    /** 胶囊文字色必须跟随自身底色：白色磨砂玻璃用深色字，半透明深灰黑用白字。 */
    public static int textNormal() { return chipIsLight() ? TEXT_DARK : TEXT_NORMAL; }
    /** 详情卡片与线路胶囊同源：同一底色 + 同一描边，保证「胶囊背景」设置同时控制两者。 */
    public static int cardColor()  { return chipIsLight() ? GLASS_NORMAL : DARK_NORMAL; }
    public static int cardStroke() { return chipIsLight() ? GLASS_STROKE_NORMAL : DARK_STROKE_NORMAL; }
    /** 胶囊底色明暗：白磨砂=浅、深灰黑=深；跟随档位时按弹窗风格明暗自动决定（浅色弹窗→白磨砂） */
    private static boolean chipIsLight() {
        int style = getStyle();
        if (style == STYLE_GLASS) return true;
        if (style == STYLE_DARK) return false;
        return DialogThemeHelper.isLightStyle();
    }
    private static final int DARK_NORMAL = 0x8014181F;
    private static final int DARK_STROKE_NORMAL = 0x26FFFFFF;
    private static final int GLASS_NORMAL = 0x40FFFFFF;
    private static final int GLASS_SELECTED = 0xB3FFFFFF;
    private static final int GLASS_STROKE_NORMAL = 0x26FFFFFF;
    private static final int NAV_DARK = 0xE614181F;
    private static final int NAV_DARK_STROKE = 0x33FFFFFF;
    private static final int NAV_LIGHT = 0xE6FFFFFF;
    private static final int NAV_LIGHT_STROKE = 0x2E000000;
    private ChipThemeHelper() {}
    public static int getStyle() { return Setting.getChipStyle(); }
    public static String nameOf(int style) {
        if (style == STYLE_GLASS) return "白色磨砂玻璃";
        if (style == STYLE_FOLLOW) return "跟随弹窗风格";
        return "半透明深灰黑";
    }
    /** 按原版弧度档位取内边距 */
    private static int[] paddingFor(float radiusDp) {
        if (radiusDp >= 28f) return PADDING_28;
        if (radiusDp >= 17f) return PADDING_16;
        if (radiusDp >= 9f)  return PADDING_FILTER;
        return PADDING_8;
    }
    /** 生成胶囊背景（含 activated/聚焦描边），半径 dp。
     *  底色/描边统一走 chipIsLight()，与详情页卡片(cardColor/cardStroke)完全同源，
     *  跟随档位时由弹窗明暗自动决定，不再固定回退深色。 */
    public static GradientDrawable background(Context ctx, float radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        float density = ctx.getResources().getDisplayMetrics().density;
        bg.setCornerRadius(radiusDp * density);
        bg.setColor(cardColor());
        bg.setStroke(Math.round(1 * density), cardStroke());
        return bg;
    }


    /** 功能页行项（增强功能/个性功能/实验室等）统一走同一套胶囊背景：
     *  与详情页卡片同底色同描边，ripple 保留点击反馈。XML 里给行项打 android:tag="chip_row" 即可。 */
    public static void applyRows(View root, float radiusDp) {
        if (root == null) return;
        if ("chip_row".equals(root.getTag())) makeRow(root, radiusDp);
        if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) root;
            for (int i = 0; i < g.getChildCount(); i++) applyRows(g.getChildAt(i), radiusDp);
        }
    }
    private static void makeRow(View view, float radiusDp) {
        float density = view.getResources().getDisplayMetrics().density;
        // shape_item 自带 12dp/8dp 内边距；换背景会丢失，先保存再恢复，
        // 让文字与胶囊边缘保持间距（与「备份」行子 View 自带 padding 的效果一致）。
        int pl = view.getPaddingLeft(), pt = view.getPaddingTop();
        int pr = view.getPaddingRight(), pb = view.getPaddingBottom();
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(radiusDp * density);
        bg.setColor(cardColor());
        bg.setStroke(Math.max(1, Math.round(density)), cardStroke());
        view.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22FFFFFF), bg, null));
        view.setPadding(pl, pt, pr, pb);
        applyRowTextColor(view, 0);
    }
    /** 行项内部文字跟随胶囊底色：浅底用深字、深底用白字，最多下钻两层（标题行 + 状态值）。 */
    private static void applyRowTextColor(View view, int depth) {
        if (depth > 2) return;
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(textNormal());
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) view;
            for (int i = 0; i < g.getChildCount(); i++) applyRowTextColor(g.getChildAt(i), depth + 1);
        }
    }
    /** 底栏悬浮胶囊：与详情卡片/功能胶囊同一套「胶囊背景」控制。
     *  浅色档铺白底深描边，深色档铺半透深灰黑+白描边，比普通胶囊更实一点保证图标可读。 */
    public static GradientDrawable navBackground(Context ctx, float radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        float density = ctx.getResources().getDisplayMetrics().density;
        bg.setCornerRadius(radiusDp * density);
        bg.setColor(chipIsLight() ? NAV_LIGHT : NAV_DARK);
        bg.setStroke(Math.round(1 * density), chipIsLight() ? NAV_LIGHT_STROKE : NAV_DARK_STROKE);
        return bg;
    }

    /** 底栏图标色：跟随胶囊底色做对比，避免浅色胶囊上出现白图标看不见 */
    public static ColorStateList navIconTint(Context ctx) {
        int accent = DialogThemeHelper.accent(ctx);
        int normal = chipIsLight() ? TEXT_DARK : TEXT_NORMAL;
        return new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, {}},
                new int[]{accent, normal});
    }

    /** 底栏选中指示器色：跟随胶囊底色，浅底用深色薄雾、深底用白色薄雾 */
    public static ColorStateList navIndicatorColor() {
        return ColorStateList.valueOf(chipIsLight() ? 0x2B101216 : 0x2BFFFFFF);
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
        boolean light = chipIsLight();
        int normal = light ? GLASS_NORMAL : DARK_NORMAL;
        int selectedBg = light ? GLASS_SELECTED : Color.argb(0xCC, 0x14, 0x18, 0x1F);
        int strokeNormal = light ? GLASS_STROKE_NORMAL : DARK_STROKE_NORMAL;
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
    /** 弹窗内胶囊：跟随弹窗风格明暗，与弹窗表面拉开对比；选中态=弹窗强调色实底。 */
    public static void applyDialog(TextView view, float radiusDp) {
        Context ctx = view.getContext();
        float density = ctx.getResources().getDisplayMetrics().density;
        int accent = DialogThemeHelper.accent(ctx);
        int surface = DialogThemeHelper.surfaceColor(ctx);
        boolean light = !isLight(surface);
        // 底色由弹窗表面色派生：深色面板上铺亮底(白 26%)，浅色面板上铺暗底(黑 14%)，保证任何风格都看得清
        int normalBg = light ? 0x42FFFFFF : 0x24000000;
        int normalTx = light ? 0xF5FFFFFF : 0xE6101216;
        int selTx = isLight(accent) ? 0xFF101216 : 0xFFFFFFFF;
        GradientDrawable nb = new GradientDrawable();
        nb.setShape(GradientDrawable.RECTANGLE);
        nb.setCornerRadius(radiusDp * density);
        nb.setColor(normalBg);
        GradientDrawable sb = new GradientDrawable();
        sb.setShape(GradientDrawable.RECTANGLE);
        sb.setCornerRadius(radiusDp * density);
        sb.setColor(accent);
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_activated}, sb);
        states.addState(new int[]{android.R.attr.state_focused}, sb);
        states.addState(new int[]{}, nb);
        view.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33808080), states, null));
        view.setTextColor(new ColorStateList(
                new int[][]{{android.R.attr.state_activated}, {android.R.attr.state_focused}, {}},
                new int[]{selTx, selTx, normalTx}));
        int px = Math.round(12 * density), py = Math.round(8 * density);
        view.setPadding(px, py, px, py);
        view.setSingleLine();
        view.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        view.setMarqueeRepeatLimit(-1);
        if (view.getTag(com.fongmi.android.tv.R.id.tag_chip_applied) == null) {
            view.setSelected(true);
            view.setTag(com.fongmi.android.tv.R.id.tag_chip_applied, Boolean.TRUE);
        }
    }
    private static boolean isLight(int c) {
        return 0.299 * android.graphics.Color.red(c) + 0.587 * android.graphics.Color.green(c) + 0.114 * android.graphics.Color.blue(c) > 160;
    }
    /** 选中态开关：activated 背景联动 + marquee 滚动开关 */
    public static void select(TextView view, boolean on) {
        view.setActivated(on);
        // 已 apply() 过的胶囊需要常驻 selected 才能跑马灯；普通选择器仍由 on 控制选中态
        boolean marquee = view.getTag(com.fongmi.android.tv.R.id.tag_chip_applied) != null;
        view.setSelected(on || marquee);
    }
}
