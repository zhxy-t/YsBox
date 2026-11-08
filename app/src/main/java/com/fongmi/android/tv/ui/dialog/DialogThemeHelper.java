package com.fongmi.android.tv.ui.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.setting.Setting;

/** 统一弹窗主题入口：所有自绘裸 Dialog / DialogFragment 都从这里取主题与语义色。 */
public final class DialogThemeHelper {

    private DialogThemeHelper() {}

    /** 普通弹窗主题（带 5 种风格的语义色与按钮样式） */
    @StyleRes public static int getTheme() {
        switch (Setting.getDialogStyle()) {
            case 0: return R.style.Theme_App_DialogClassic;
            case 1: return R.style.Theme_App_DialogWhite;
            case 2: return R.style.Theme_App_DialogAmoled;
            case 4: return R.style.Theme_App_DialogNeon;
            case 3:
            default: return R.style.Theme_App_DialogIOS;
        }
    }

    /** 纯语义色 overlay：给 BottomSheet / SideSheet 用 ContextThemeWrapper 叠加 */
    @StyleRes public static int getOverlay() {
        switch (Setting.getDialogStyle()) {
            case 0: return R.style.ThemeOverlay_XingChen_GlassDialog_Dark;
            case 1: return R.style.ThemeOverlay_FongMi_Dialog_White;
            case 2: return R.style.ThemeOverlay_FongMi_Dialog_Amoled;
            case 4: return R.style.ThemeOverlay_FongMi_Dialog_Neon;
            case 3:
            default: return R.style.ThemeOverlay_FongMi_Dialog_IOS;
        }
    }

    /** 透明窗口变体：给 loading / 原 Theme_Translucent_NoTitleBar 的自绘弹窗 */
    @StyleRes public static int getTranslucentTheme() {
        switch (Setting.getDialogStyle()) {
            case 0: return R.style.Theme_App_DialogClassic_Translucent;
            case 1: return R.style.Theme_App_DialogWhite_Translucent;
            case 2: return R.style.Theme_App_DialogAmoled_Translucent;
            case 4: return R.style.Theme_App_DialogNeon_Translucent;
            case 3:
            default: return R.style.Theme_App_DialogIOS_Translucent;
        }
    }

    /** 当前风格的按钮边框资源（常态=透明底+风格色边框；按下=风格色填充） */
    public static int btnStyleRes() {
        switch (Setting.getDialogStyle()) {
            case 0: return R.drawable.dialog_btn_style_classic;
            case 1: return R.drawable.dialog_btn_style_white;
            case 2: return R.drawable.dialog_btn_style_amoled;
            case 4: return R.drawable.dialog_btn_style_neon;
            case 3:
            default: return R.drawable.dialog_btn_style_ios;
        }
    }

    /**
     * 统一自绘裸 Dialog 的窗口外观：透明底 + 标准暗化遮罩。
     * 只管外观，不改窗口尺寸与位置；调用方仍自行设置 width/height/gravity。
     * 注意：不能把这些写进 getTheme() 的 5 个 style —— 它们同时被
     * BaseActivity.applyDialogTheme() 用作整个 Activity 主题，加入 windowIsFloating
     * 会让 App 主界面变成浮动窗口。故改为逐窗口显式调用。
     */
    public static void applyStandardWindow(@Nullable Window window) {
        if (window == null) return;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.32f);
    }

    /**
     * 统一弹窗工厂：App 内所有 MaterialAlertDialog 都应通过它创建，而不是各自
     * new MaterialAlertDialogBuilder(activity)。原因：Activity 主主题里没有定义
     * materialAlertDialogTheme，直接 new 会回退到 Material 默认白底 + 默认宽度，
     * 导致切换弹窗风格时这些弹窗毫无反应（这就是"弹窗不统一"的根因）。
     *
     * 用法：把 new MaterialAlertDialogBuilder(x) 换成 DialogThemeHelper.builder(x)，
     * 其余链式调用 .setTitle(...).setMessage(...).show() 全部保持不变。
     *
     * 注意：只服务于 App 内弹窗。HK 小程序的弹窗需要真正的全局窗口，走各自的
     * 裸 new Dialog，不经过这里，不要改成调用本方法。
     */
    public static com.google.android.material.dialog.MaterialAlertDialogBuilder builder(Context ctx) {
        return new Builder(ctx);
    }

    /**
     * 带统一尺寸 + 统一按钮边框的 Material 弹窗 Builder。
     *
     * 只在 create() 里套用 applyDialogSize —— create() 是虚方法，即使调用方写成
     * builder(ctx).setTitle(..).show()（静态类型退回基类），基类 show() 内部仍会
     * 回调到这里的 create()，所以尺寸一定生效。
     *
     * 按钮样式用 decor.post 在窗口真正显示后再套，不覆盖 setOnShowListener，
     * 因此调用方原有的按钮点击拦截逻辑不受影响。
     */
    public static final class Builder extends com.google.android.material.dialog.MaterialAlertDialogBuilder {

        public Builder(Context ctx) {
            super(wrap(ctx));
        }

        @Override
        public androidx.appcompat.app.AlertDialog create() {
            androidx.appcompat.app.AlertDialog dialog = super.create();
            applyWindowBackground(dialog);
            applyDialogSize(dialog.getWindow());
            applyButtonStyle(dialog);
            applyListItemStyle(dialog);
            return dialog;
        }
    }

    /**
     * 强制把「当前风格的边框背景」设回窗口。
     * MaterialAlertDialogBuilder 会用自建的 MaterialShapeDrawable（纯 surface 填充、无描边）
     * 覆盖主题里的 android:windowBackground，导致 5 种风格的 stroke 边框全部丢失。
     * 在 create() 后重新设置，边框才会真正绘制（与 HkDialogs 的做法一致）。
     */
    private static void applyWindowBackground(@Nullable androidx.appcompat.app.AlertDialog dialog) {
        if (dialog == null || dialog.getWindow() == null) return;
        dialog.getWindow().setBackgroundDrawableResource(dialogBackgroundRes());
    }

    /** 当前风格对应的弹窗边框背景（带 stroke） */
    static int dialogBackgroundRes() {
        switch (Setting.getDialogStyle()) {
            case 0: return R.drawable.dialog_glass_dark;
            case 1: return R.drawable.bg_dialog_white;
            case 2: return R.drawable.bg_dialog_dark_amoled;
            case 4: return R.drawable.bg_dialog_neon;
            case 3:
            default: return R.drawable.bg_dialog_ios;
        }
    }

    /**
     * 给 Material 弹窗的三个按钮（positive/negative/neutral）套统一边框与点击态。
     *
     * 只在 create() 之后、窗口显示前调用，并且只处理「本 App 弹窗」——
     * HK 小程序的弹窗走自己的裸 Dialog，不经过 MaterialAlertDialogBuilder，
     * 所以这里的改动不会碰到 HK。
     *
     * 用 MaterialButton 原生 strokeColor / strokeWidth 而不是换背景 drawable：
     * 原生描边会保留 Material 自带的按下涟漪与文字变色，观感比自绘背景更自然。
     */
    /**
     * 给 AlertDialog 的列表 item（singleChoiceItems / multiChoiceItems / setAdapter）强制着色。
     * 系统 AlertDialog 的列表 item 用内置布局，主题里的 textColorAlertDialogListItem 经常不生效，
     * 这里直接遍历 ListView 的 children 并强制设置 textColor。
     */
    private static void applyListItemStyle(androidx.appcompat.app.AlertDialog dialog) {
        if (dialog == null || dialog.getWindow() == null) return;
        dialog.getWindow().getDecorView().post(() -> {
            try {
                android.widget.ListView listView = dialog.getListView();
                if (listView == null) return;
                int textColor = primaryText(dialog.getContext());
                for (int i = 0; i < listView.getChildCount(); i++) {
                    View child = listView.getChildAt(i);
                    if (child instanceof android.widget.TextView) {
                        ((android.widget.TextView) child).setTextColor(textColor);
                    } else if (child instanceof android.widget.CheckedTextView) {
                        ((android.widget.CheckedTextView) child).setTextColor(textColor);
                    } else if (child instanceof android.view.ViewGroup) {
                        // 有些 item 是 FrameLayout 包裹的
                        for (int j = 0; j < ((android.view.ViewGroup) child).getChildCount(); j++) {
                            View inner = ((android.view.ViewGroup) child).getChildAt(j);
                            if (inner instanceof android.widget.TextView) {
                                ((android.widget.TextView) inner).setTextColor(textColor);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    private static void applyButtonStyle(androidx.appcompat.app.AlertDialog dialog) {
        if (dialog == null || dialog.getWindow() == null) return;
        Window window = dialog.getWindow();
        window.getDecorView().post(() -> {
            try {
                styleButton(dialog, android.R.id.button1, true);
                styleButton(dialog, android.R.id.button2, false);
                styleButton(dialog, android.R.id.button3, false);
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * @param primary true=主按钮（强调色边框+强调色字），false=次按钮（主字色边框+主字色字）
     */
    private static void styleButton(androidx.appcompat.app.AlertDialog dialog, int id, boolean primary) {
        View v = dialog.findViewById(id);
        if (!(v instanceof TextView)) return;
        TextView button = (TextView) v;
        if (button.getVisibility() != View.VISIBLE) return;
        Context c = button.getContext();
        int accent = primary ? accent(c) : primaryText(c);
        int strokePx = Math.max(1, dp(c, 1) / 2 + 1);

        if (button instanceof com.google.android.material.button.MaterialButton) {
            com.google.android.material.button.MaterialButton mb = (com.google.android.material.button.MaterialButton) button;
            mb.setStrokeWidth(strokePx);
            mb.setStrokeColor(ColorStateList.valueOf(accent));
            mb.setCornerRadius(dp(c, 18));
            mb.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            mb.setTextColor(new ColorStateList(
                    new int[][]{{android.R.attr.state_pressed}, {}},
                    new int[]{isLight(accent) ? 0xFF171310 : 0xFFFFFFFF, accent}));
        } else {
            try {
                Drawable bg = ContextCompat.getDrawable(c, btnStyleRes());
                if (bg != null) button.setBackground(bg.mutate());
            } catch (Exception ignored) {
            }
            button.setTextColor(accent);
        }
        // 霓虹风格：按下时文字也变渐变
        NeonButtonStyler.apply(button);
    }

    /** 统一 App 内弹窗尺寸：居中窄卡片，不铺满全屏（给自绘 Dialog 手工调用） */
    public static void applyDialogSize(@Nullable Window window) {
        if (window == null) return;
        android.util.DisplayMetrics dm = window.getContext().getResources().getDisplayMetrics();
        int maxW = com.fongmi.android.tv.utils.ResUtil.dp2px(480);
        int w = Math.min(Math.round(dm.widthPixels * 0.88f), maxW);
        int maxH = Math.round(dm.heightPixels * 0.8f);
        android.view.View decor = window.getDecorView();
        window.setLayout(w, WindowManager.LayoutParams.WRAP_CONTENT);
        if (decor != null) {
            decor.post(() -> {
                int h = decor.getHeight();
                if (h > maxH) window.setLayout(w, maxH);
            });
        }
    }

    /**
     * 「列表类弹窗」统一尺寸（站点弹窗 / 源脚本弹窗 / 源码预览 共用同一套规则，
     * 保证这几个弹窗视觉上同宽同高）：
     *  宽 = max(min(屏宽 * (横屏0.5 / 竖屏0.92), 620dp), 320dp)
     *  高 = wrap_content，但封顶 屏高 * 0.62（超过则内部滚动，不顶状态栏）
     *
     * 只在这里改规则，三个弹窗一起变；不要在各自类里再写一套尺寸。
     */
    public static void applyListDialogSize(@Nullable Window window) {
        if (window == null) return;
        Context ctx = window.getContext();
        android.util.DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        boolean land = com.fongmi.android.tv.utils.ResUtil.isLand(ctx);
        int width = Math.min(Math.round(com.fongmi.android.tv.utils.ResUtil.getScreenWidth(ctx) * (land ? 0.5f : 0.92f)),
                com.fongmi.android.tv.utils.ResUtil.dp2px(620));
        int w = Math.max(width, com.fongmi.android.tv.utils.ResUtil.dp2px(320));
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = w;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = android.view.Gravity.CENTER;
        window.setAttributes(lp);
        window.setLayout(w, WindowManager.LayoutParams.WRAP_CONTENT);

        int maxH = Math.round(dm.heightPixels * 0.62f);
        android.view.View decor = window.getDecorView();
        if (decor != null) {
            decor.post(() -> {
                int h = decor.getHeight();
                if (h > maxH) window.setLayout(w, maxH);
            });
        }
    }

    /** 取当前弹窗主题下的语义色（Java 代码里替代 Color.parseColor） */
    /** 强制用当前弹窗风格主题解析，避免 Activity 主题（Theme.Splash 等）里没有语义色 */
    private static Context themed(Context ctx) {
        return new android.view.ContextThemeWrapper(ctx, getTheme());
    }

    @ColorInt public static int resolveColor(Context ctx, @AttrRes int attr) {
        TypedValue tv = new TypedValue();
        if (themed(ctx).getTheme().resolveAttribute(attr, tv, true)) return tv.data;
        return 0xFF888888;
    }

    public static int resolveResId(Context ctx, @AttrRes int attr) {
        TypedValue tv = new TypedValue();
        if (themed(ctx).getTheme().resolveAttribute(attr, tv, true)) return tv.resourceId;
        return 0;
    }

    /** 给自绘弹窗 inflate 布局用：返回带弹窗主题、且 ?attr 可解析的 Context */
    public static Context wrap(Context ctx) {
        return themed(ctx);
    }

    /**
     * 给弹窗内容 inflate 布局用：在 Activity 主题之上叠加当前风格的 overlay 主题。
     * Activity 的 Theme.App.Dialog* 只带 dialogXxx 语义色，没有 colorOnSurface /
     * colorOnSurfaceVariant 等 Material 颜色；不叠加 overlay 时这些 ?attr 会解析成黑色，
     * 深色弹窗上的文字/输入框提示就看不见了。
     */
    public static Context wrapOverlay(Context ctx) {
        return new android.view.ContextThemeWrapper(ctx, getOverlay());
    }

    @ColorInt public static int primaryText(Context c)   { return resolveColor(c, R.attr.dialogTextPrimary); }
    @ColorInt public static int secondaryText(Context c) { return resolveColor(c, R.attr.dialogTextSecondary); }
    @ColorInt public static int hintText(Context c)      { return resolveColor(c, R.attr.dialogTextHint); }
    @ColorInt public static int accent(Context c)        { return resolveColor(c, R.attr.dialogAccent); }
    @ColorInt public static int divider(Context c)       { return resolveColor(c, R.attr.dialogDivider); }

    public static int surfaceRes(Context c)              { return resolveResId(c, R.attr.dialogSurface); }
    public static int inputSurfaceRes(Context c)         { return resolveResId(c, R.attr.dialogInputSurface); }
    public static int cardSurfaceRes(Context c)          { return resolveResId(c, R.attr.dialogCardSurface); }

    private static boolean isLight(@ColorInt int color) {
        double lum = 0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color);
        return lum > 160;
    }

    private static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }
}
