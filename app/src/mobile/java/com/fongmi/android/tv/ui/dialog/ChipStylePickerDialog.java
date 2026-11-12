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
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.event.RefreshEvent;

/**
 * 胶囊背景选择器：仿弹窗风格选择器，3 张卡片预览（深灰黑 / 白磨砂 / 跟随弹窗），点卡即应用。
 */
public final class ChipStylePickerDialog {

    private static final String[] NAMES = {"半透明深灰黑", "白色磨砂玻璃", "跟随弹窗风格"};
    private static final String[] DESCS = {
            "胶囊铺半透明深灰黑，暗色界面首选",
            "胶囊铺白色磨砂玻璃，亮色界面首选",
            "浅色弹窗自动用白磨砂，深色弹窗自动用深灰黑"
    };
    // 预览用固定色板（不依赖当前弹窗风格，自成一体展示三档区别）
    private static final int[] PREVIEW_NORMAL = {0x8014181F, 0x40FFFFFF, 0x8014181F};
    private static final int[] PREVIEW_STROKE = {0x26FFFFFF, 0x26FFFFFF, 0x26FFFFFF};
    private static final int ACCENT = 0xFF4A9EFF;

    public interface Listener {
        void onPick(int style);
    }

    private ChipStylePickerDialog() {
    }

    public static void show(Activity activity, Listener listener) {
        int current = Setting.getChipStyle();
        int pad = dp(activity, 14);
        int gap = dp(activity, 10);
        int cardH = dp(activity, 76);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad / 2, pad, 0);
        for (int i = 0; i < NAMES.length; i++) {
            final int style = i;
            FrameLayout cell = new FrameLayout(activity);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, cardH);
            cp.setMargins(0, gap, 0, 0);
            cell.setLayoutParams(cp);
            cell.setClickable(true);

            // 卡片本体：黑底卡 + 演示胶囊排
            LinearLayout card = new LinearLayout(activity);
            card.setOrientation(LinearLayout.VERTICAL);
            float density = activity.getResources().getDisplayMetrics().density;
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(0xFF14181F);
            bg.setCornerRadius(dp(density, 16));
            bg.setStroke(Math.max(1, dp(density, 1)), 0x40FFFFFF);
            card.setBackground(bg);
            int cpad = dp(activity, 12);
            card.setPadding(cpad, dp(activity, 10), cpad, dp(activity, 10));

            TextView title = new TextView(activity);
            title.setText(NAMES[i]);
            title.setTextColor(0xFFF2F4F8);
            title.setTextSize(14);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            card.addView(title);
            TextView desc = new TextView(activity);
            desc.setText(DESCS[i]);
            desc.setTextColor(0xFFA8B0C0);
            desc.setTextSize(10);
            desc.setPadding(0, dp(activity, 2), 0, dp(activity, 6));
            card.addView(desc);

            // 演示胶囊：两个，一个常态、一个选中态
            LinearLayout chips = new LinearLayout(activity);
            chips.setOrientation(LinearLayout.HORIZONTAL);
            chips.setGravity(Gravity.CENTER_VERTICAL);
            TextView n1 = new TextView(activity);
            n1.setText("普通胶囊");
            styleChip(n1, density, i, false);
            chips.addView(n1);
            TextView n2 = new TextView(activity);
            n2.setText("选中胶囊");
            styleChip(n2, density, i, true);
            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp2.setMarginStart(dp(activity, 8));
            n2.setLayoutParams(lp2);
            chips.addView(n2);
            card.addView(chips);
            cell.addView(card, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // 选中态：强调色描边 + 勾选角标（所有风格统一，区别只在胶囊本身）
            View border = new View(activity);
            GradientDrawable ring = new GradientDrawable();
            ring.setColor(Color.TRANSPARENT);
            ring.setCornerRadius(dp(density, 16));
            ring.setStroke(Math.max(1, dp(density, 3)), ACCENT);
            border.setBackground(ring);
            cell.addView(border, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            TextView check = new TextView(activity);
            check.setText("\u2713");
            check.setTextSize(22);
            check.setTextColor(Color.WHITE);
            check.setGravity(Gravity.CENTER);
            GradientDrawable cb = new GradientDrawable();
            cb.setColor(ACCENT);
            cb.setCornerRadius(dp(activity, 14));
            check.setBackground(cb);
            FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(dp(activity, 28), dp(activity, 28));
            clp.gravity = Gravity.END | Gravity.TOP;
            clp.setMargins(0, dp(activity, 6), dp(activity, 6), 0);
            check.setLayoutParams(clp);
            check.setVisibility(current == style ? View.VISIBLE : View.GONE);
            cell.addView(check);

            cell.setOnClickListener(v -> {
                Setting.putChipStyle(style);
                try { if (listener != null) listener.onPick(style); } catch (Exception ignored) {}
                RefreshEvent.theme();
            });
            root.addView(cell);
        }
        android.widget.ScrollView scroll = new android.widget.ScrollView(activity);
        scroll.setFillViewport(true);
        scroll.addView(root);
        DialogThemeHelper.builder(activity)
                .setTitle("胶囊背景")
                .setView(scroll)
                .show();
    }

    /** 演示胶囊：按档位绘制常态/选中态 */
    private static void styleChip(TextView chip, float density, int i, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(density, 8));
        if (selected) {
            bg.setColor(ACCENT);
            chip.setTextColor(Color.WHITE);
        } else {
            bg.setColor(PREVIEW_NORMAL[i]);
            bg.setStroke(Math.max(1, dp(density, 1)), PREVIEW_STROKE[i]);
            chip.setTextColor(i == 1 ? 0xFF101216 : 0xFFF2F4F8);
        }
        chip.setBackground(bg);
        chip.setPadding(dp(density, 12), dp(density, 6), dp(density, 12), dp(density, 6));
        chip.setTextSize(12);
        chip.setSingleLine();
    }

    public static String nameOf(int style) {
        return NAMES[style >= 0 && style < NAMES.length ? style : 0];
    }

    private static int dp(float density, int v) {
        return Math.round(v * density);
    }

    private static int dp(Activity a, int v) {
        return Math.round(v * a.getResources().getDisplayMetrics().density);
    }
}
