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
import com.fongmi.android.tv.utils.WallUtil;
/**
 * 内置壁纸选择弹窗：随机置顶，8 张内置壁纸两列预览，点哪张直接应用哪张。
 */
public final class WallPickerDialog {
    private static final int[] WALLS = {R.drawable.wallpaper_1, R.drawable.wallpaper_2, R.drawable.wallpaper_3, R.drawable.wallpaper_4,
                                        R.drawable.wallpaper_5, R.drawable.wallpaper_6, R.drawable.wallpaper_7, R.drawable.wallpaper_8};
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
        root.setPadding(pad, pad / 2, pad, pad / 4);
        AlertDialog[] ref = new AlertDialog[1];
        // 随机置顶：每次冷启动换一张内置壁纸
        FrameLayout randCell = randomCell(activity, current, ref, listener);
        root.addView(randCell);
        for (int row = 0; row < 4; row++) {
            LinearLayout line = new LinearLayout(activity);
            line.setOrientation(LinearLayout.HORIZONTAL);
            if (row > 0) line.setPadding(0, gap, 0, 0);
            for (int col = 0; col < 2; col++) {
                int index = row * 2 + col;
                if (index >= WALLS.length) break;
                int wall = index + 1;
                FrameLayout cell = wallpaperCell(activity, WALLS[index], wall, current, ref, listener);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, height, 1);
                params.setMarginStart(gap);
                params.setMarginEnd(gap);
                cell.setLayoutParams(params);
                line.addView(cell);
            }
            root.addView(line);
        }
        ref[0] = DialogThemeHelper.builder(activity)
                .setTitle(R.string.wall_default)
                .setView(root)
                .show();
    }
    /** 单张壁纸格子 */
    private static FrameLayout wallpaperCell(Activity activity, int resId, int wall, int current, AlertDialog[] ref, Listener listener) {
        FrameLayout cell = new FrameLayout(activity);
        ImageView image = new ImageView(activity);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setImageResource(resId);
        cell.addView(image, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        cell.addView(ring(activity, current == wall, DialogThemeHelper.accent(activity)),
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        TextView check = new TextView(activity);
        check.setText("\u2713");
        check.setTextSize(20);
        check.setTextColor(Color.WHITE);
        check.setGravity(Gravity.CENTER);
        check.setVisibility(current == wall ? View.VISIBLE : View.GONE);
        cell.addView(check, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        cell.setClickable(true);
        cell.setOnClickListener(view -> {
            Setting.putWall(wall);
            Setting.putWallType(0);
            ConfigEvent.wall();
            if (listener != null) listener.onPick(wall);
            if (ref[0] != null) ref[0].dismiss();
        });
        return cell;
    }
    /** 随机格子 */
    private static FrameLayout randomCell(Activity activity, int current, AlertDialog[] ref, Listener listener) {
        FrameLayout cell = new FrameLayout(activity);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(activity, 12));
        bg.setColor(DialogThemeHelper.isLightStyle() ? 0x14000000 : 0x33FFFFFF);
        cell.setBackground(bg);
        TextView text = new TextView(activity);
        text.setText(R.string.wall_random);
        text.setGravity(Gravity.CENTER);
        text.setTextSize(15);
        text.setTextColor(DialogThemeHelper.isLightStyle() ? 0xE6101216 : 0xFFFFFFFF);
        cell.addView(text, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        cell.addView(ring(activity, current == 0, DialogThemeHelper.accent(activity)),
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        TextView check = new TextView(activity);
        check.setText("\u2713");
        check.setTextSize(20);
        check.setTextColor(DialogThemeHelper.accent(activity));
        check.setGravity(Gravity.CENTER);
        check.setVisibility(current == 0 ? View.VISIBLE : View.GONE);
        cell.addView(check, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(activity, 44));
        lp.bottomMargin = dp(activity, 10);
        cell.setLayoutParams(lp);
        cell.setClickable(true);
        cell.setOnClickListener(view -> {
            Setting.putWall(0);
            Setting.putWallType(0);
            Setting.resetWallRandom();
            ConfigEvent.wall();
            if (listener != null) listener.onPick(0);
            if (ref[0] != null) ref[0].dismiss();
        });
        return cell;
    }
    private static View ring(Activity activity, boolean visible, int color) {
        View border = new View(activity);
        GradientDrawable ring = new GradientDrawable();
        ring.setShape(GradientDrawable.RECTANGLE);
        ring.setColor(Color.TRANSPARENT);
        ring.setStroke(dp(activity, 2), color);
        border.setBackground(ring);
        border.setVisibility(visible ? View.VISIBLE : View.GONE);
        return border;
    }
    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
