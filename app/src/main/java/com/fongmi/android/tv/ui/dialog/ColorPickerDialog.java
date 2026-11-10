package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.setting.Setting;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.function.IntConsumer;

/**
 * 圆形色块选择弹窗：所有颜色选择（主题色、字幕文字/边框/背景色）共用这一套 UI，
 * 单行横向滑动，选中项加白色圆环，打开时自动滚到当前颜色。
 */
public final class ColorPickerDialog {

    private ColorPickerDialog() {
    }

    /** 普通颜色：透明色按半透明白显示。 */
    public static void show(Activity activity, int titleRes, int[] colors, int current, IntConsumer onPick) {
        show(activity, titleRes, colors, current, onPick, false);
    }

    /** 主题色：-1 显示为灰色 ×，0 显示为「自动」壁纸色块。 */
    public static void showTheme(Activity activity, int titleRes, int[] colors, int current, IntConsumer onPick) {
        show(activity, titleRes, colors, current, onPick, true);
    }

    private static void show(Activity activity, int titleRes, int[] colors, int current, IntConsumer onPick, boolean theme) {
        int pad = dp(activity, 16);
        int cell = dp(activity, 40);
        int gap = dp(activity, 8);
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(pad, pad, pad, pad);
        HorizontalScrollView scroll = new HorizontalScrollView(activity);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.addView(row, new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.WRAP_CONTENT, HorizontalScrollView.LayoutParams.WRAP_CONTENT));
        AlertDialog[] ref = new AlertDialog[1];
        View selectedView = null;
        int selectedIndex = 0;
        for (int i = 0; i < colors.length; i++) {
            int color = colors[i];
            FrameLayout holder = new FrameLayout(activity);
            GradientDrawable ring = new GradientDrawable();
            ring.setShape(GradientDrawable.OVAL);
            ring.setColor(Color.TRANSPARENT);
            if (color == current) ring.setStroke(dp(activity, 2), DialogThemeHelper.accent(activity));
            holder.setBackground(ring);
            View dot = new View(activity);
            GradientDrawable fill = new GradientDrawable();
            fill.setShape(GradientDrawable.OVAL);
            if (theme && color == -1) fill.setColor(0xFF9E9E9E);
            else if (theme && color == 0) fill.setColor(Setting.getWallColor());
            else fill.setColor(color == Color.TRANSPARENT ? 0x22FFFFFF : color);
            if (!theme || color != 0) fill.setStroke(dp(activity, 1), 0x33FFFFFF);
            dot.setBackground(fill);
            FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(cell, cell);
            dotParams.gravity = Gravity.CENTER;
            holder.addView(dot, dotParams);
            if (theme && (color == -1 || color == 0)) {
                TextView label = new TextView(activity);
                label.setText(color == -1 ? "\u00D7" : "自动");
                label.setTextSize(color == -1 ? 16 : 8);
                label.setTextColor(Color.WHITE);
                label.setGravity(Gravity.CENTER);
                FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(cell, cell);
                labelParams.gravity = Gravity.CENTER;
                holder.addView(label, labelParams);
            }
            LinearLayout.LayoutParams holderParams = new LinearLayout.LayoutParams(cell + dp(activity, 10), cell + dp(activity, 10));
            holderParams.setMarginStart(gap);
            holderParams.setMarginEnd(gap);
            holder.setLayoutParams(holderParams);
            holder.setClickable(true);
            holder.setOnClickListener(view -> {
                onPick.accept(color);
                if (ref[0] != null) ref[0].dismiss();
            });
            if (color == current) {
                selectedView = holder;
                selectedIndex = i;
            }
            row.addView(holder);
        }
        final View target = selectedView;
        final int index = selectedIndex;
        ref[0] = DialogThemeHelper.builder(activity)
                .setTitle(titleRes)
                .setView(scroll)
                .show();
        scroll.post(() -> {
            int x = target != null ? target.getLeft() : index * (cell + dp(activity, 10) + gap * 2);
            int w = scroll.getWidth();
            scroll.smoothScrollTo(Math.max(0, x - w / 2 + cell / 2), 0);
        });
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density);
    }
}