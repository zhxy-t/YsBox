package com.fongmi.android.tv.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;
import android.content.res.TypedArray;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

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
     * DialogThemeHelper.builder(activity)。原因：Activity 主主题里没有定义
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
     * 带统一尺寸的 Material 弹窗 Builder。
     *
     * 只在 create() 里套用 applyDialogSize —— create() 是虚方法，即使调用方写成
     * builder(ctx).setTitle(..).show()（静态类型退回基类），基类 show() 内部仍会
     * 回调到这里的 create()，所以尺寸一定生效。
     *
     * 注意不要改回 java 侧「不拦截」的写法，否则 59 处链式调用会全部丢失尺寸统一。
     */
    public static final class Builder extends com.google.android.material.dialog.MaterialAlertDialogBuilder {

        public Builder(Context ctx) {
            super(wrap(ctx));
        }

        @Override
        public androidx.appcompat.app.AlertDialog create() {
            androidx.appcompat.app.AlertDialog dialog = super.create();
            applyDialogSize(dialog.getWindow());
            return dialog;
        }
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

    @ColorInt public static int primaryText(Context c)   { return resolveColor(c, R.attr.dialogTextPrimary); }
    @ColorInt public static int secondaryText(Context c) { return resolveColor(c, R.attr.dialogTextSecondary); }
    @ColorInt public static int hintText(Context c)      { return resolveColor(c, R.attr.dialogTextHint); }
    @ColorInt public static int accent(Context c)        { return resolveColor(c, R.attr.dialogAccent); }
    @ColorInt public static int divider(Context c)       { return resolveColor(c, R.attr.dialogDivider); }
    public static int surfaceRes(Context c)              { return resolveResId(c, R.attr.dialogSurface); }
    public static int inputSurfaceRes(Context c)         { return resolveResId(c, R.attr.dialogInputSurface); }
    public static int cardSurfaceRes(Context c)          { return resolveResId(c, R.attr.dialogCardSurface); }
}
