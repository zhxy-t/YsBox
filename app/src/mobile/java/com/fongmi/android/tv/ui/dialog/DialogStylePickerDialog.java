package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.setting.Setting;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 弹窗风格预览选择器：仿内置壁纸选择，5 张小弹窗预览卡，点哪张直接应用。
 * 每张卡按真实风格绘制：背景色/圆角/主文字/次文字/强调色/按钮，所见即所得。
 */
public final class DialogStylePickerDialog {

    /** 与 Setting.getDialogStyle() 的 0~8 一一对应 */
    private static final String[] NAMES = {"经典深色", "纯白 Material", "暗色模式", "iOS 风格", "霓虹渐变", "原始", "毛玻璃", "紫夜", "M3 深色"};
    // surface, primary, secondary, accent, card(按钮底), radius
    private static final int[] SURFACE = {0xFF1A1D24, 0xFFFFFFFF, 0xFF0A0A0A, 0xFFF2F2F7, 0xFF101A33, 0xFFFFFBFE, 0xFF1E2633, 0xFF1A1428, 0xFF1C1B1F};
    private static final int[] PRIMARY = {0xFFF2F4F8, 0xFF101216, 0xFFF0F0F0, 0xFF1C1C1E, 0xFFFFFFFF, 0xFF1C1B1F, 0xFFF2F6FC, 0xFFF4F0FF, 0xFFE6E1E5};
    private static final int[] SECONDARY = {0xFFA8B0C0, 0xFF5F6368, 0xFF888888, 0xFF8E8E93, 0xFFAAAAFF, 0xFF49454F, 0xFFAEB9C9, 0xFFBDADD9, 0xFFCAC4D0};
    private static final int[] ACCENT = {0xFFD9A420, 0xFF6750A4, 0xFF4A9EFF, 0xFF007AFF, 0xFFFF006E, 0xFF6750A4, 0xFF8AB4F8, 0xFFB79CFF, 0xFFD0BCFF};
    private static final int[] RADIUS = {20, 20, 20, 20, 20, 20, 20, 20, 20};
    // 卡片/弹窗边框色：比 surface 明显一点，保证「有边框」肉眼可见
    private static final int[] BORDER = {0xFFD9A420, 0xFFC9CFDA, 0xFF2F5A8A, 0xFFC7C7CC, 0xFFFFFFFF, 0x00000000, 0xFF8AB4F8, 0xFFB79CFF, 0x00000000};

    /** 霓虹三色（与 bg_dialog_neon.xml 完全一致） */
    private static final int[] NEON = {0xFF00D4FF, 0xFF7B61FF, 0xFFFF006E};
    private static final int NEON_INDEX = 4;
    private static final int ORIGINAL_INDEX = 5;
    private static final int ORIGINAL_DARK_INDEX = 8;
    private static final int BTN_RADIUS = 14;

    public interface Listener {
        void onPick(int style);
    }

    private DialogStylePickerDialog() {
    }

    public static void show(Activity activity, Listener listener) {
        int current = clamp(Setting.getDialogStyle());
        int pad = dp(activity, 14);
        int gap = dp(activity, 10);
        int cardH = dp(activity, 96);

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad / 2, pad, 0);

        AlertDialog[] ref = new AlertDialog[1];
        for (int i = 0; i < NAMES.length; i++) {
            final int style = i;
            FrameLayout cell = new FrameLayout(activity);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, cardH);
            cp.setMargins(0, gap, 0, 0);
            cell.setLayoutParams(cp);
            cell.setClickable(true);

            // 弹窗本体预览
            LinearLayout card = new LinearLayout(activity);
            card.setOrientation(LinearLayout.VERTICAL);
            float density = activity.getResources().getDisplayMetrics().density;
            boolean neon = i == NEON_INDEX;
            if (neon) {
                // 渐变边框直接作为卡片背景，内容画在它上面；否则后加的不透明描边层会盖住卡片内容
                GradientDrawable inner = new GradientDrawable();
                inner.setColor(SURFACE[i]);
                inner.setCornerRadius(dp(density, RADIUS[i]));
                card.setBackground(gradientBorder(density, inner, RADIUS[i], Math.max(1, dp(density, 3))));
            } else {
                card.setBackground(cardBackground(density, i, false));
            }
            int cpad = dp(activity, 12);
            card.setPadding(cpad, cpad, cpad, cpad);

            TextView title = new TextView(activity);
            title.setText(NAMES[i]);
            title.setTextColor(PRIMARY[i]);
            title.setTextSize(15);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(title);

            TextView body = new TextView(activity);
            body.setText("这是一段弹窗内容示例文字");
            body.setTextColor(SECONDARY[i]);
            body.setTextSize(11);
            body.setPadding(0, dp(activity, 4), 0, dp(activity, 8));
            card.addView(body);

            LinearLayout buttons = new LinearLayout(activity);
            buttons.setOrientation(LinearLayout.HORIZONTAL);
            buttons.setGravity(Gravity.END);
            TextView neg = new TextView(activity);
            neg.setText("取消");
            neg.setTextColor(neon ? Color.WHITE : ACCENT[i]);
            neg.setTextSize(12);
            neg.setPadding(dp(activity, 14), dp(activity, 5), dp(activity, 14), dp(activity, 5));
            neg.setBackground(buttonNormal(density, i, neon, false));
            attachPressEffect(neg, density, i, neon, false);
            buttons.addView(neg);
            TextView pos = new TextView(activity);
            pos.setText("确定");
            pos.setTextColor(neon ? Color.WHITE : ACCENT[i]);
            pos.setTextSize(12);
            pos.setPadding(dp(activity, 14), dp(activity, 5), dp(activity, 14), dp(activity, 5));
            pos.setBackground(buttonNormal(density, i, neon, true));
            attachPressEffect(pos, density, i, neon, true);
            // pos 尚未 addView，getLayoutParams() 为 null，必须显式赋值后再加入父容器
            LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            plp.setMarginStart(dp(activity, 8));
            pos.setLayoutParams(plp);
            buttons.addView(pos);
            card.addView(buttons);
            cell.addView(card, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // 选中态：描边 + 勾选（霓虹的渐变边框已画在卡片背景上；原始风格是系统默认无边框，均不叠加）
            if (!neon && i != ORIGINAL_INDEX && i != ORIGINAL_DARK_INDEX) {
                View border = new View(activity);
                border.setBackground(ringBackground(density, i, false));
                // 所有卡片常驻显示边框，不只是选中的
                border.setVisibility(View.VISIBLE);
                cell.addView(border, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            TextView check = new TextView(activity);
            check.setText("\u2713");
            check.setTextSize(22);
            check.setTextColor(Color.WHITE);
            check.setGravity(Gravity.CENTER);
            if (neon) {
                check.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR, NEON));
            } else {
                GradientDrawable cb = new GradientDrawable();
                cb.setColor(ACCENT[i]);
                cb.setCornerRadius(dp(activity, 14));
                check.setBackground(cb);
            }
            FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(dp(activity, 28), dp(activity, 28));
            clp.gravity = Gravity.END | Gravity.TOP;
            clp.setMargins(0, dp(activity, 6), dp(activity, 6), 0);
            check.setLayoutParams(clp);
            check.setVisibility(current == style ? View.VISIBLE : View.GONE);
            cell.addView(check);

            cell.setOnClickListener(v -> {
                Setting.putDialogStyle(style);
                // 先关弹窗再 recreate，避免 Activity 重建时 dialog 窗口已失效
                try { if (ref[0] != null) ref[0].dismiss(); } catch (Exception ignored) {}
                if (listener != null) listener.onPick(style);
                RefreshEvent.theme();
            });
            root.addView(cell);
        }

        android.widget.ScrollView scroll = new android.widget.ScrollView(activity);
        scroll.setFillViewport(true);
        scroll.addView(root);
        ref[0] = DialogThemeHelper.builder(activity)
                .setTitle("弹窗风格")
                .setView(scroll)
                
                .show();
    }

    /** 卡片背景：无边框，只留底色和圆角，边框由 ringBackground 统一提供 */
    private static Drawable cardBackground(float density, int i, boolean neon) {
        GradientDrawable inner = new GradientDrawable();
        inner.setColor(SURFACE[i]);
        inner.setCornerRadius(dp(density, RADIUS[i]));
        return inner;
    }

    /** 按钮常态：透明底 + 边框（霓虹主钮与卡片边框同款渐变填充） */
    private static Drawable buttonNormal(float density, int i, boolean neon, boolean primary) {
        GradientDrawable inner = new GradientDrawable();
        inner.setCornerRadius(dp(density, BTN_RADIUS));
        if (neon && primary) {
            // 与其他风格的选中填充同一语义：整颗渐变填充(三色 TL_BR)，文字白色
            return new GradientDrawable(GradientDrawable.Orientation.TL_BR, NEON);
        }
        inner.setColor(0x00000000);
        int strokeColor = primary ? ACCENT[i] : blend(ACCENT[i], SURFACE[i], 0.30f);
        inner.setStroke(Math.max(1, dp(density, 1)), strokeColor);
        return inner;
    }


    /** 选中外框：霓虹=渐变描边，其余=强调色描边 */
    private static Drawable ringBackground(float density, int i, boolean neon) {
        GradientDrawable inner = new GradientDrawable();
        inner.setCornerRadius(dp(density, RADIUS[i]));
        if (neon) {
            // 渐变环：内层必须用卡片自身的面色填充，才能“挖”出中间区域，
            // 否则整块都会被渐变铺满、挡住卡片内容。
            inner.setColor(SURFACE[i]);
            return gradientBorder(density, inner, RADIUS[i], Math.max(1, dp(density, 3)));
        }
        inner.setColor(Color.TRANSPARENT);
        inner.setStroke(Math.max(1, dp(density, 3)), ACCENT[i]);
        return inner;
    }

    /** 按下效果：所有卡片统一——填充变三色渐变(霓虹卡同款)、文字白色；抬起恢复常态 */
    private static void attachPressEffect(TextView btn, float density, int i, boolean neon, boolean primary) {
        final int normalColor = neon ? Color.WHITE : ACCENT[i];
        btn.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackground(pressedGradient(density));
                    ((TextView) v).setTextColor(Color.WHITE);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setBackground(buttonNormal(density, i, neon, primary));
                    ((TextView) v).setTextColor(normalColor);
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    /** 按下态：霓虹三色渐变填充（所有风格统一使用） */
    private static Drawable pressedGradient(float density) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, NEON);
        d.setCornerRadius(dp(density, BTN_RADIUS));
        return d;
    }

    /** 渐变边框 = 外层三色渐变 + 内层镂空缩进 stroke */
    private static Drawable gradientBorder(float density, GradientDrawable inner, int radiusDp, int strokePx) {
        GradientDrawable outer = new GradientDrawable(GradientDrawable.Orientation.TL_BR, NEON);
        outer.setCornerRadius(dp(density, radiusDp));
        inner.setCornerRadius(dp(density, radiusDp));
        LayerDrawable layer = new LayerDrawable(new Drawable[]{outer, inner});
        int s = Math.max(1, strokePx);
        layer.setLayerInset(1, s, s, s, s);
        return layer;
    }


    private static int clamp(int v) {
        return v < 0 || v >= NAMES.length ? 3 : v;
    }

    private static int blend(int a, int b, float f) {
        int ar = Color.red(a), ag = Color.green(a), ab = Color.blue(a);
        int br = Color.red(b), bg = Color.green(b), bb = Color.blue(b);
        return Color.argb(0xFF, (int) (ar + (br - ar) * f), (int) (ag + (bg - ag) * f), (int) (ab + (bb - ab) * f));
    }

    private static int dp(float density, int v) {
        return Math.round(v * density);
    }

    /** 兼容旧调用点：直接传 Activity */
    private static int dp(Activity a, int v) {
        return Math.round(v * a.getResources().getDisplayMetrics().density);
    }

    public static String nameOf(int style) {
        return NAMES[clamp(style)];
    }
}