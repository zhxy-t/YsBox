package com.fongmi.android.tv.ui.lab;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;

import com.fongmi.android.tv.ui.custom.CustomWallView;

/**
 * 实验室 UI 设计语言：与 Trae 主 App 一致——半透明卡面＋浅色字（shape_item 同口径）。
 * 页面背景与其它页面同源：铺自定义壁纸（CustomWallView），内容根透明；
 * 不再使用近黑实底（Theme.Lab 的 windowBackground 已改透明）。
 * 独立成套，不依赖其它模块的皮肤。布局/条目/顺序一律不动，只出皮。
 */
public final class LabSkin {

    /** 点缀黄（只做按钮底） */
    public static final int YELLOW = 0xFFF2B33D;
    /** 主字（标题/名称） */
    public static final int TEXT_MAIN = 0xFFFFFFFF;
    /** 正文 */
    public static final int TEXT_BODY = 0xE6FFFFFF;
    /** 次字 */
    public static final int TEXT_SUB = 0xB3FFFFFF;
    /** 暗字：次字 50% 透明 */
    public static final int TEXT_DIM = 0x80FFFFFF;
    /** 绿字（运行中/已装） */
    public static final int GREEN = 0xFF35D884;
    /** 绿点 #22B45E */
    public static final int GREEN_DOT = 0xFF22B45E;
    /** 出错红 #E5484D */
    public static final int RED = 0xFFE5484D;
    /** 琥珀字（状态/进度文案） */
    public static final int AMBER = 0xFFF0C040;
    /** 黄钮上的字 #171310 */
    public static final int ON_YELLOW = 0xFF171310;
    /** 卡面主字 */
    public static final int TEXT_ON_DARK = 0xFFFFFFFF;
    /** 卡面次字 */
    public static final int TEXT_ON_DARK_DIM = 0xCCFFFFFF;
    /** 输入框占位字 */
    public static final int HINT_ON_DARK = 0x99FFFFFF;
    /** 卡面报错红 */
    public static final int RED_ON_DARK = 0xFFFFA49A;
    /** 壁纸上的标题字 */
    public static final int WALL_TITLE = 0xFFFFFFFF;
    /** 壁纸上的副行字 */
    public static final int WALL_SUB = 0xCCFFFFFF;
    /** 壁纸上的弱字 */
    public static final int WALL_DIM = 0x99FFFFFF;
    /** 金色环境签 */
    public static final int GOLD_TAG = 0xFFFFD27A;
    /** 运行中绿字 */
    public static final int GREEN_ON_DARK = 0xFF35D884;
    /** 运行中计数黄 */
    public static final int COUNT_YELLOW = 0xFFF0C040;

    // —— 弹窗专用字色（弹窗面为浅色白玻璃，字必须深色） ——
    /** 弹窗标题/正文 */
    public static final int DLG_TEXT = 0xFF1A1A1A;
    /** 弹窗次字 */
    public static final int DLG_TEXT_DIM = 0xFF666666;
    /** 弹窗输入框占位 */
    public static final int DLG_HINT = 0xFF999999;
    /** 弹窗报错 */
    public static final int DLG_ERR = 0xFFD32F2F;

    public interface BoolConsumer {
        void accept(boolean on);
    }

    /** 壁纸上的字：加深色投影，壁纸偏亮时也看得清 */
    public static void halo(android.widget.TextView t) {
        t.setShadowLayer(12, 0, 2, 0x99000000);
    }

    /** 卡片面：与主 App 的 shape_item 同口径（15% 黑＋浅描边＋12dp 圆角） */
    public static GradientDrawable glassDark(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x26000000);
        d.setCornerRadius(dp(c, 12));
        d.setStroke(dp(c, 1), 0x1AFFFFFF);
        return d;
    }

    /** 弹窗面板：与主 App 的 dialog_glass 同口径（白玻璃、深色字、圆角 20dp） */
    public static GradientDrawable dialogPanel(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0xF2FFFFFF);
        d.setCornerRadius(dp(c, 20));
        d.setStroke(dp(c, 1), 0x1F000000);
        return d;
    }

    /** 弹窗选项卡：白面板上的浅灰独立卡 */
    public static GradientDrawable dialogOption(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x0F000000);
        d.setCornerRadius(dp(c, 16));
        d.setStroke(dp(c, 1), 0x12000000);
        return d;
    }

    /** 弹窗中性钮 */
    public static GradientDrawable dialogNeutral(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x14000000);
        d.setCornerRadius(dp(c, 18));
        return d;
    }

    /** 进度轨道 */
    public static GradientDrawable progressTrack(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x24FFFFFF);
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 进度黄条（稿 .fill） */
    public static GradientDrawable progressFill(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(YELLOW);
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 状态徽：运行中（稿 .chip.run） */
    public static GradientDrawable chipRun(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x292FD47E);
        d.setCornerRadius(dp(c, 999));
        d.setStroke(dp(c, 1), 0x5935D884);
        return d;
    }

    /** 状态徽：已停止 */
    public static GradientDrawable chipStop(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x14FFFFFF);
        d.setCornerRadius(dp(c, 999));
        d.setStroke(dp(c, 1), 0x24FFFFFF);
        return d;
    }

    /** 中性小胶囊（卸载钮） */
    public static GradientDrawable capsuleNeutral(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x1FFFFFFF);
        d.setCornerRadius(dp(c, 999));
        d.setStroke(dp(c, 1), 0x33FFFFFF);
        return d;
    }

    /** 黄色小胶囊（稿 env .install：环境名后面的「安装」钮） */
    public static GradientDrawable capsuleYellow(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(YELLOW);
        d.setCornerRadius(dp(c, 13));
        return d;
    }

    /** 大钮（服务详情「停止/启动」全宽钮） */
    public static GradientDrawable btnBigDark(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x1FFFFFFF);
        d.setCornerRadius(dp(c, 15));
        return d;
    }

    /** 运行记录行 */
    public static GradientDrawable logRowDark(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x14FFFFFF);
        d.setCornerRadius(dp(c, 14));
        d.setStroke(dp(c, 1), 0x1AFFFFFF);
        return d;
    }

    /** 行卡（服务详情页下半区功能行） */
    public static GradientDrawable rowDark(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x14FFFFFF);
        d.setCornerRadius(dp(c, 12));
        d.setStroke(dp(c, 1), 0x12FFFFFF);
        return d;
    }

    /** 胶囊钮（全部停止/运行中） */
    public static GradientDrawable pillDark(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x1F000000);
        d.setCornerRadius(dp(c, 18));
        d.setStroke(dp(c, 1), 0x29FFFFFF);
        return d;
    }

    /** 地址盒 */
    public static GradientDrawable addrBox(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x12FFFFFF);
        d.setCornerRadius(dp(c, 12));
        d.setStroke(dp(c, 1), 0x1AFFFFFF);
        return d;
    }

    /** 输入框 */
    public static GradientDrawable darkInput(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x1A000000);
        d.setCornerRadius(dp(c, 14));
        d.setStroke(dp(c, 2), 0x38FFFFFF);
        return d;
    }

    /**
     * 迷你自启开关（稿 .mini）：36×21dp，开态黄底白钮，关态半透明白底。
     * 纯 View 自绘，不用系统 Switch（系统 Switch 在各 ROM 上皮不一）。
     */
    public static View miniSwitch(Context c, boolean on, BoolConsumer cb) {
        final boolean[] state = {on};
        final android.widget.FrameLayout sw = new android.widget.FrameLayout(c);
        int w = dp(c, 36), h = dp(c, 21);
        sw.setLayoutParams(new ViewGroup.LayoutParams(w, h));
        final GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(on ? YELLOW : 0x2EFFFFFF);
        sw.setBackground(bg);
        final View knob = new View(c);
        int ks = dp(c, 16);
        final android.widget.FrameLayout.LayoutParams klp =
                new android.widget.FrameLayout.LayoutParams(ks, ks);
        klp.gravity = android.view.Gravity.CENTER_VERTICAL;
        klp.leftMargin = dp(c, on ? 18 : 3);
        klp.rightMargin = dp(c, on ? 3 : 18);
        knob.setLayoutParams(klp);
        GradientDrawable kd = new GradientDrawable();
        kd.setShape(GradientDrawable.OVAL);
        kd.setColor(0xFFFFFFFF);
        knob.setBackground(kd);
        sw.addView(knob);
        sw.setClickable(true);
        sw.setFocusable(true);
        sw.setOnClickListener(v -> {
            state[0] = !state[0];
            bg.setColor(state[0] ? YELLOW : 0x2EFFFFFF);
            android.widget.FrameLayout.LayoutParams p =
                    (android.widget.FrameLayout.LayoutParams) knob.getLayoutParams();
            p.leftMargin = dp(c, state[0] ? 18 : 3);
            p.rightMargin = dp(c, state[0] ? 3 : 18);
            knob.setLayoutParams(p);
            if (cb != null) cb.accept(state[0]);
        });
        return sw;
    }

    private LabSkin() {
    }

    // ==================== 跟随弹窗风格的弹窗配色 ====================
    // 实验室弹窗原先写死白玻璃底 + 橙色按钮（0xF2FFFFFF / 0xFFF2B33D），
    // 完全无视「弹窗风格」设置，所以切 Neon/Amoled 时图1、图4 依然是白底橙钮。
    // 以下方法统一从当前弹窗风格的主题里取语义色，与主 App 其它弹窗同源。
    // 注意：只改「弹窗」相关配色；实验室页面的卡片/文字仍用上面的常量，不动页面观感。

    /** 弹窗面：取当前风格的 dialogSurface（自带圆角与描边） */
    public static android.graphics.drawable.Drawable dlgPanel(Context c) {
        int res = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.surfaceRes(c);
        if (res != 0) {
            try {
                android.graphics.drawable.Drawable d = androidx.core.content.ContextCompat.getDrawable(c, res);
                if (d != null) return d;
            } catch (Exception ignored) {
            }
        }
        return dialogPanel(c);
    }

    /** 弹窗主字色：取当前风格的 dialogTextPrimary */
    public static int dlgText(Context c) {
        return com.fongmi.android.tv.ui.dialog.DialogThemeHelper.primaryText(c);
    }

    /** 弹窗次字色：取当前风格的 dialogTextSecondary */
    public static int dlgTextDim(Context c) {
        return com.fongmi.android.tv.ui.dialog.DialogThemeHelper.secondaryText(c);
    }

    /** 弹窗占位字色：取当前风格的 dialogTextHint */
    public static int dlgHint(Context c) {
        return com.fongmi.android.tv.ui.dialog.DialogThemeHelper.hintText(c);
    }

    /** 弹窗强调色（原橙色主钮）：取当前风格的 dialogAccent */
    public static int dlgAccent(Context c) {
        return com.fongmi.android.tv.ui.dialog.DialogThemeHelper.accent(c);
    }

    /** 主钮底色：跟随风格强调色（原写死橙色 YELLOW） */
    public static GradientDrawable dlgBtnAccent(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(dlgAccent(c));
        d.setCornerRadius(dp(c, 999));
        // 统一加边框：比填充色略深，保证浅色/深色都有清晰轮廓
        d.setStroke(dp(c, 1) > 0 ? Math.max(1, dp(c, 1)) : 1, darken(dlgAccent(c), 0.22f));
        return d;
    }
    /** 常态主钮：透明底 + 强调色边框（与按下态形成对比） */
    public static GradientDrawable dlgBtnAccentOutline(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x00000000);
        d.setCornerRadius(dp(c, 999));
        d.setStroke(Math.max(1, dp(c, 1)), dlgAccent(c));
        return d;
    }
    /** 主钮强调色边框色（给文字/边框共用） */
    public static int dlgAccentStroke(Context c) {
        return dlgAccent(c);
    }

    /**
     * 弹窗按钮统一背景：取当前风格的按钮 drawable
     * （常态=透明底+风格色边框；按下=风格色填充；霓虹=渐变边框/渐变填充）。
     * 与 Material 弹窗按钮用的是同一份资源，保证两套弹窗观感一致。
     */
    public static android.graphics.drawable.Drawable dlgButton(Context c) {
        int res = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.btnStyleRes();
        android.graphics.drawable.Drawable d = androidx.core.content.ContextCompat.getDrawable(c, res);
        return d != null ? d.mutate() : dlgBtnAccent(c);
    }

    /** 主钮文字色：描边态直接用风格强调色，与边框同色 */
    public static int dlgButtonText(Context c) {
        return dlgAccent(c);
    }

    /**
     * 给自绘按钮装上「按下时文字变渐变」的效果（仅霓虹风格生效，其它风格自动跳过）。
     * 背景的渐变边框由 dlgButton() 提供，这里只负责文字。
     */
    public static void applyDlgPressEffect(android.widget.TextView tv) {
        com.fongmi.android.tv.ui.dialog.NeonButtonStyler.apply(tv);
    }
    private static int darken(int color, float f) {
        int r = (int) (Color.red(color) * (1 - f));
        int g = (int) (Color.green(color) * (1 - f));
        int b = (int) (Color.blue(color) * (1 - f));
        return Color.argb(Color.alpha(color), r, g, b);
    }

    /** 主钮文字色：跟随风格强调色自动取对比色 */
    public static int dlgOnAccent(Context c) {
        return isLight(dlgAccent(c)) ? 0xFF171310 : 0xFFFFFFFF;
    }

    /** 中性钮底：跟随风格 surface 的浅淡层（原 dialogNeutral） */
    public static GradientDrawable dlgBtnNeutral(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(withAlpha(dlgText(c), 0x1F));
        d.setCornerRadius(dp(c, 18));
        // 统一加边框
        d.setStroke(Math.max(1, dp(c, 1)), withAlpha(dlgText(c), 0x33));
        return d;
    }

    /** 选项卡：跟随风格 dialogCardSurface（原 dialogOption 浅灰块） */
    public static android.graphics.drawable.Drawable dlgOptionCard(Context c) {
        int res = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.cardSurfaceRes(c);
        if (res != 0) {
            try {
                android.graphics.drawable.Drawable d = androidx.core.content.ContextCompat.getDrawable(c, res);
                if (d != null) return d;
            } catch (Exception ignored) {
            }
        }
        return dialogOption(c);
    }

    /** 输入框：跟随风格 dialogInputSurface（原 darkInput） */
    public static android.graphics.drawable.Drawable dlgInput(Context c) {
        int res = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.inputSurfaceRes(c);
        if (res != 0) {
            try {
                android.graphics.drawable.Drawable d = androidx.core.content.ContextCompat.getDrawable(c, res);
                if (d != null) return d;
            } catch (Exception ignored) {
            }
        }
        return darkInput(c);
    }

    /** 进度轨道 / 填充：跟随风格强调色 */
    public static GradientDrawable dlgProgressTrack(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(withAlpha(dlgText(c), 0x24));
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    public static GradientDrawable dlgProgressFill(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(dlgAccent(c));
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 弹窗报错色：深色风格用浅红，浅色风格用深红 */
    public static int dlgError(Context c) {
        return isLight(dlgText(c)) ? 0xFFD32F2F : 0xFFFFA49A;
    }

    private static boolean isLight(int color) {
        double lum = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color));
        return lum > 160;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    public static int dp(Context c, int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics()));
    }

    /** 状态栏融合：透明栏＋浅色图标，内容延伸到栏下（与 BaseActivity 同一写法） */
    public static void edgeToEdge(AppCompatActivity a) {
        EdgeToEdge.enable(a,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            a.getWindow().setStatusBarContrastEnforced(false);
            a.getWindow().setNavigationBarContrastEnforced(false);
        }
    }

    /**
     * 与 BaseActivity 一致：内容之下铺全局壁纸，页面根透明，背景跟主 App 其它页同源。
     */
    public static void setContentView(AppCompatActivity a, View content) {
        a.setContentView(content);
        ViewGroup root = a.findViewById(android.R.id.content);
        if (root == null) return;
        for (int i = 0; i < root.getChildCount(); i++) {
            if (root.getChildAt(i) instanceof CustomWallView) return;
        }
        root.addView(new CustomWallView(a, null), 0,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public static int statusBarHeight(Context c) {
        int id = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? c.getResources().getDimensionPixelSize(id) : dp(c, 24);
    }

    public static int navBarHeight(Context c) {
        int id = c.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return id > 0 ? c.getResources().getDimensionPixelSize(id) : 0;
    }

    /** 卡面：与主 App 条目卡同口径（15% 黑、1dp 描边、10dp 圆角） */
    public static GradientDrawable cardBackground(Context c, boolean running) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x26000000);
        d.setCornerRadius(dp(c, 10));
        d.setStroke(dp(c, 1), running ? 0x804ADE80 : 0x1AFFFFFF);
        return d;
    }

    /** 状态徽：灰（未装/已停止） */
    public static GradientDrawable tagGray(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x26FFFFFF);
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 状态徽：绿（运行中） */
    public static GradientDrawable tagGreen(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x334ADE80); // rgba(74,222,128,.20)
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 黄色主钮（只做点缀） */
    public static GradientDrawable buttonYellow(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(YELLOW);
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 灰色次钮 */
    public static GradientDrawable buttonGray(Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(0x1FFFFFFF);
        d.setCornerRadius(dp(c, 999));
        return d;
    }

    /** 小圆点（运行中） */
    public static GradientDrawable dot(Context c, int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        int s = dp(c, 8);
        d.setSize(s, s);
        return d;
    }
}
