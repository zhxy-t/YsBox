package com.fongmi.android.tv.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivityCrashBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.github.catvod.utils.Prefers;

import java.util.Objects;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;

public class CrashActivity extends BaseActivity {

    private ActivityCrashBinding mBinding;

    @Override
    protected boolean customWall() {
        return false;
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityCrashBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCrash();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        applyDialogStyle();
    }

    /**
     * 崩溃页整页跟随当前弹窗风格。
     * 崩溃页不是 Dialog 而是独立 Activity，此前没有任何背景与配色，
     * iOS 等浅色风格下会露出系统默认白底 + Material 蓝紫按钮，与全局风格割裂。
     */
    private void applyDialogStyle() {
        int surface = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.surfaceColor(this);
        int primary = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.primaryText(this);
        int accent = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.accent(this);
        mBinding.getRoot().setBackgroundColor(surface);
        mBinding.text.setTextColor(primary);
        styleCrashButton(mBinding.restart, accent, true);
        styleCrashButton(mBinding.details, primary, false);
    }

    /** 崩溃页按钮：胶囊边框。TV 遥控器依赖焦点可视性，用 StateListDrawable 区分焦点态 */
    private void styleCrashButton(android.view.View view, int color, boolean primary) {
        if (!(view instanceof android.widget.TextView)) return;
        android.widget.TextView button = (android.widget.TextView) view;
        float density = getResources().getDisplayMetrics().density;
        int radius = (int) (24 * density);
        int padH = (int) (28 * density);
        int padV = (int) (10 * density);
        int strokeWidth = Math.max(1, (int) density);

        android.graphics.drawable.GradientDrawable normal = new android.graphics.drawable.GradientDrawable();
        normal.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        normal.setCornerRadius(radius);
        normal.setColor(primary ? ((color & 0x00FFFFFF) | 0x22000000) : android.graphics.Color.TRANSPARENT);
        normal.setStroke(strokeWidth, color);

        android.graphics.drawable.GradientDrawable focused = new android.graphics.drawable.GradientDrawable();
        focused.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        focused.setCornerRadius(radius);
        focused.setColor(color);
        focused.setStroke(strokeWidth * 2, color);

        android.graphics.drawable.StateListDrawable selector = new android.graphics.drawable.StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_focused}, focused);
        selector.addState(new int[]{}, normal);

        button.setBackground(selector);
        button.setTextColor(new android.content.res.ColorStateList(
                new int[][]{{android.R.attr.state_focused}, {}},
                new int[]{pickContrast(color), color}));
        button.setPadding(padH, padV, padH, padV);
    }

    /** 焦点态填充色较深/较艳，文字自动选黑或白保证可读 */
    private static int pickContrast(int color) {
        double lum = 0.299 * android.graphics.Color.red(color) + 0.587 * android.graphics.Color.green(color) + 0.114 * android.graphics.Color.blue(color);
        return lum > 160 ? android.graphics.Color.BLACK : android.graphics.Color.WHITE;
    }

    @Override
    protected void initEvent() {
        mBinding.details.setOnClickListener(v -> showError());
        mBinding.restart.setOnClickListener(v -> CustomActivityOnCrash.restartApplication(this, Objects.requireNonNull(CustomActivityOnCrash.getConfigFromIntent(getIntent()))));
    }

    private void setCrash() {
        String log = CustomActivityOnCrash.getActivityLogFromIntent(getIntent());
        if (TextUtils.isEmpty(log)) return;
        String[] lines = log.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            if (lines[i].isEmpty()) continue;
            if (lines[i].contains(HomeActivity.class.getSimpleName())) {
                Prefers.put("crash", true);
                break;
            }
        }
    }

    private void showError() {
        String details = CustomActivityOnCrash.getAllErrorDetailsFromIntent(this, getIntent());
        AlertDialog dialog = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.builder(this)
                .setTitle(R.string.crash_details_title)
                .setMessage(details)
                .setPositiveButton(R.string.playback_error_copy, (d, w) -> {
                    com.fongmi.android.tv.utils.Util.copy(details);
                    com.fongmi.android.tv.utils.Notify.show(R.string.playback_error_copy);
                })
                .setNegativeButton(R.string.crash_details_close, null)
                .create();
        dialog.setOnShowListener(d -> {
            android.widget.TextView message = dialog.findViewById(android.R.id.message);
            if (message != null) message.setTextIsSelectable(true);
        });
        dialog.show();
    }
}
