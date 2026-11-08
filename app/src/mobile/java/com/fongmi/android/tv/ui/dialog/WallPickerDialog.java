package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.event.ConfigEvent;
import com.fongmi.android.tv.setting.Setting;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 内置壁纸选择弹窗：四张默认壁纸两列预览，点哪张直接应用哪张。
 */
public final class WallPickerDialog {

    private static final int[] WALLS = {R.drawable.wallpaper_1, R.drawable.wallpaper_2, R.drawable.wallpaper_3, R.drawable.wallpaper_4};

    public interface Listener {

        void onPick(int wall);
    }

    private WallPickerDialog() {
    }

    public static void show(Activity activity, Listener listener) {
        int current = Setting.getWall();
        int pad = dp(activity, 16);
        int gap = dp(activity, 6);
        int height = dp(activity, 110);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad / 2, pad, 0);
        AlertDialog[] ref = new AlertDialog[1];
        for (int row = 0; row < 2; row++) {
            LinearLayout line = new LinearLayout(activity);
            line.setOrientation(LinearLayout.HORIZONTAL);
            if (row > 0) line.setPadding(0, gap, 0, 0);
            for (int col = 0; col < 2; col++) {
                int index = row * 2 + col;
                int wall = index + 1;
                FrameLayout cell = new FrameLayout(activity);
                ImageView image = new ImageView(activity);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setImageResource(WALLS[index]);
                cell.addView(image, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

                GradientDrawable ring = new GradientDrawable();
                ring.setShape(GradientDrawable.RECTANGLE);
                ring.setColor(Color.TRANSPARENT);
                ring.setStroke(dp(activity, 2), Color.WHITE);
                View border = new View(activity);
                border.setBackground(ring);
                border.setVisibility(current == wall ? View.VISIBLE : View.GONE);
                cell.addView(border, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

                TextView check = new TextView(activity);
                check.setText("\u2713");
                check.setTextSize(20);
                check.setTextColor(Color.WHITE);
                check.setGravity(Gravity.CENTER);
                check.setVisibility(current == wall ? View.VISIBLE : View.GONE);
                cell.addView(check, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, height, 1);
                params.setMarginStart(gap);
                params.setMarginEnd(gap);
                cell.setLayoutParams(params);
                cell.setClickable(true);
                cell.setOnClickListener(view -> {
                    Setting.putWall(wall);
                    Setting.putWallType(0);
                    ConfigEvent.wall();
                    if (listener != null) listener.onPick(wall);
                    if (ref[0] != null) ref[0].dismiss();
                });
                line.addView(cell);
            }
            root.addView(line);
        }
        ref[0] = DialogThemeHelper.builder(activity)
                .setTitle(R.string.wall_default)
                .setView(root)
                
                .show();
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density);
    }
}
