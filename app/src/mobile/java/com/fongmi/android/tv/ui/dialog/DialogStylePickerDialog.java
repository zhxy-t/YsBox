package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
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

    /** 与 Setting.getDialogStyle() 的 0~4 一一对应 */
    private static final String[] NAMES = {"经典深色", "纯白 Material", "暗色模式", "iOS 风格", "霓虹渐变"};
    // surface, primary, secondary, accent, card(按钮底), radius
    private static final int[] SURFACE = {0xFF1A1D24, 0xFFFFFFFF, 0xFF0A0A0A, 0xFFF2F2F7, 0xFF101A33};
    private static final int[] PRIMARY = {0xFFF2F4F8, 0xFF101216, 0xFFF0F0F0, 0xFF1C1C1E, 0xFFFFFFFF};
    private static final int[] SECONDARY = {0xFFA8B0C0, 0xFF5F6368, 0xFF888888, 0xFF8E8E93, 0xFFAAAAFF};
    private static final int[] ACCENT = {0xFFD9A420, 0xFF6750A4, 0xFF4A9EFF, 0xFF007AFF, 0xFFFF006E};
    private static final int[] RADIUS = {16, 20, 20, 24, 20};

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
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(SURFACE[i]);
            bg.setCornerRadius(dp(activity, RADIUS[i]));
            bg.setStroke(dp(activity, 1), blend(SURFACE[i], PRIMARY[i], 0.25f));
            card.setBackground(bg);
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
            neg.setTextColor(ACCENT[i]);
            neg.setTextSize(12);
            neg.setPadding(dp(activity, 14), dp(activity, 5), dp(activity, 14), dp(activity, 5));
            buttons.addView(neg);
            TextView pos = new TextView(activity);
            pos.setText("确定");
            pos.setTextColor(Color.WHITE);
            pos.setTextSize(12);
            pos.setPadding(dp(activity, 14), dp(activity, 5), dp(activity, 14), dp(activity, 5));
            GradientDrawable pb = new GradientDrawable();
            pb.setColor(ACCENT[i]);
            pb.setCornerRadius(dp(activity, 14));
            pos.setBackground(pb);
            // pos 尚未 addView，getLayoutParams() 为 null，必须显式赋值后再加入父容器
            LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            plp.setMarginStart(dp(activity, 8));
            pos.setLayoutParams(plp);
            buttons.addView(pos);
            card.addView(buttons);
            cell.addView(card, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // 选中态：描边 + 勾选
            GradientDrawable ring = new GradientDrawable();
            ring.setColor(Color.TRANSPARENT);
            ring.setCornerRadius(dp(activity, RADIUS[i]));
            ring.setStroke(dp(activity, 3), ACCENT[i]);
            View border = new View(activity);
            border.setBackground(ring);
            border.setVisibility(current == style ? View.VISIBLE : View.GONE);
            cell.addView(border, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            TextView check = new TextView(activity);
            check.setText("\u2713");
            check.setTextSize(22);
            check.setTextColor(Color.WHITE);
            check.setGravity(Gravity.CENTER);
            GradientDrawable cb = new GradientDrawable();
            cb.setColor(ACCENT[i]);
            cb.setCornerRadius(dp(activity, 14));
            check.setBackground(cb);
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
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
    }

    private static int clamp(int v) {
        return v < 0 || v >= NAMES.length ? 3 : v;
    }

    private static int blend(int a, int b, float f) {
        int ar = Color.red(a), ag = Color.green(a), ab = Color.blue(a);
        int br = Color.red(b), bg = Color.green(b), bb = Color.blue(b);
        return Color.argb(0x33, (int) (ar + (br - ar) * f), (int) (ag + (bg - ag) * f), (int) (ab + (bb - ab) * f));
    }

    private static int dp(Activity a, int v) {
        return (int) (v * a.getResources().getDisplayMetrics().density);
    }

    public static String nameOf(int style) {
        return NAMES[clamp(style)];
    }
}