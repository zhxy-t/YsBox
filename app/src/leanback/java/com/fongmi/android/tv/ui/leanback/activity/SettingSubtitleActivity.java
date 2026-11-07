package com.fongmi.android.tv.ui.leanback.activity;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivitySettingSubtitleBinding;
import com.fongmi.android.tv.setting.SubtitleSetting;
import com.fongmi.android.tv.ui.leanback.base.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.function.IntConsumer;

public class SettingSubtitleActivity extends BaseActivity {

    private static final int[] TEXT_COLORS = {
            SubtitleSetting.SUBTITLE_COLOR_WHITE,
            SubtitleSetting.SUBTITLE_COLOR_YELLOW,
            SubtitleSetting.SUBTITLE_COLOR_CYAN,
            SubtitleSetting.SUBTITLE_COLOR_GREEN,
            SubtitleSetting.SUBTITLE_COLOR_ORANGE,
            SubtitleSetting.SUBTITLE_COLOR_PINK,
            SubtitleSetting.SUBTITLE_COLOR_RED,
            SubtitleSetting.SUBTITLE_COLOR_BLUE,
            SubtitleSetting.SUBTITLE_COLOR_BLACK,
            SubtitleSetting.SUBTITLE_COLOR_GRAY
    };

    private static final int[] BACKGROUND_COLORS = {
            Color.TRANSPARENT,
            SubtitleSetting.SUBTITLE_BACKGROUND_DIM,
            SubtitleSetting.SUBTITLE_BACKGROUND_BLACK,
            SubtitleSetting.SUBTITLE_BACKGROUND_GRAY
    };

    private static final float[] SCALES = {0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f};
    private static final float[] POSITIONS = {0f, 5f, 10f, 15f, 20f, 30f};

    private ActivitySettingSubtitleBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettingSubtitleActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivitySettingSubtitleBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mBinding.subtitleStyle.requestFocus();
        updateView();
    }

    @Override
    protected void initEvent() {
        mBinding.subtitleStyle.setOnClickListener(this::setStyle);
        mBinding.subtitleSize.setOnClickListener(this::setSize);
        mBinding.subtitlePosition.setOnClickListener(this::setPosition);
        mBinding.subtitleTextColor.setOnClickListener(this::setTextColor);
        mBinding.subtitleEdgeColor.setOnClickListener(this::setEdgeColor);
        mBinding.subtitleBackground.setOnClickListener(this::setBackground);
        mBinding.subtitleSecondary.setOnClickListener(this::setSecondary);
        mBinding.subtitleReset.setOnClickListener(this::onReset);
    }

    private void updateView() {
        mBinding.subtitleStyleText.setText(getStyleName(SubtitleSetting.getStyleSource()));
        mBinding.subtitleSizeText.setText(formatScale(SubtitleSetting.getScale()));
        mBinding.subtitlePositionText.setText(formatPosition(SubtitleSetting.getPosition()));
        mBinding.subtitleTextColorText.setText(getTextColorName(SubtitleSetting.getTextBaseColor()));
        mBinding.subtitleEdgeColorText.setText(getTextColorName(SubtitleSetting.getEdgeBaseColor()));
        mBinding.subtitleBackgroundText.setText(getBackgroundColorName(SubtitleSetting.getBackgroundBaseColor()));
        mBinding.subtitleSecondaryText.setText(SubtitleSetting.getSecondaryTrackId() == SubtitleSetting.SECONDARY_SUBTITLE_AUTO ? getString(R.string.subtitle_secondary_auto) : getString(R.string.subtitle_secondary_off));
    }

    private void setStyle(View view) {
        SubtitleSetting.putStyleSource((SubtitleSetting.getStyleSource() + 1) % 3);
        mBinding.subtitleStyleText.setText(getStyleName(SubtitleSetting.getStyleSource()));
    }

    private void setSize(View view) {
        String[] items = new String[SCALES.length];
        for (int i = 0; i < SCALES.length; i++) items[i] = formatScale(SCALES[i]);
        DialogThemeHelper.builder(this).setTitle(R.string.subtitle_size).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(items, findNearest(SCALES, SubtitleSetting.getScale()), (dialog, which) -> {
            SubtitleSetting.putScale(SCALES[which]);
            mBinding.subtitleSizeText.setText(items[which]);
            dialog.dismiss();
        }).show();
    }

    private void setPosition(View view) {
        String[] items = new String[POSITIONS.length];
        for (int i = 0; i < POSITIONS.length; i++) items[i] = formatPosition(POSITIONS[i]);
        DialogThemeHelper.builder(this).setTitle(R.string.subtitle_position).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(items, findNearest(POSITIONS, SubtitleSetting.getPosition()), (dialog, which) -> {
            SubtitleSetting.putPosition(POSITIONS[which]);
            mBinding.subtitlePositionText.setText(items[which]);
            dialog.dismiss();
        }).show();
    }

    private void setTextColor(View view) {
        showColorDialog(R.string.subtitle_text_color, TEXT_COLORS, SubtitleSetting.getTextBaseColor(), color -> {
            SubtitleSetting.putTextColor(color);
            mBinding.subtitleTextColorText.setText(getString(getTextColorName(color)));
        });
    }

    private void setEdgeColor(View view) {
        showColorDialog(R.string.subtitle_edge_color, TEXT_COLORS, SubtitleSetting.getEdgeBaseColor(), color -> {
            SubtitleSetting.putEdgeColor(color);
            mBinding.subtitleEdgeColorText.setText(getString(getTextColorName(color)));
        });
    }

    private void setBackground(View view) {
        String[] names = new String[BACKGROUND_COLORS.length];
        for (int i = 0; i < BACKGROUND_COLORS.length; i++) names[i] = getString(getBackgroundColorName(BACKGROUND_COLORS[i]));
        DialogThemeHelper.builder(this).setTitle(R.string.subtitle_background).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(names, findIndex(BACKGROUND_COLORS, SubtitleSetting.getBackgroundBaseColor()), (dialog, which) -> {
            SubtitleSetting.putBackgroundColor(BACKGROUND_COLORS[which]);
            mBinding.subtitleBackgroundText.setText(names[which]);
            dialog.dismiss();
        }).show();
    }

    private void showColorDialog(int title, int[] colors, int current, IntConsumer apply) {
        String[] names = new String[colors.length];
        for (int i = 0; i < colors.length; i++) names[i] = getString(getTextColorName(colors[i]));
        DialogThemeHelper.builder(this).setTitle(title).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(names, findIndex(colors, current), (dialog, which) -> {
            apply.accept(colors[which]);
            dialog.dismiss();
        }).show();
    }

    private void setSecondary(View view) {
        int current = SubtitleSetting.getSecondaryTrackId();
        int next = current == SubtitleSetting.SECONDARY_SUBTITLE_AUTO ? SubtitleSetting.SECONDARY_SUBTITLE_OFF : SubtitleSetting.SECONDARY_SUBTITLE_AUTO;
        SubtitleSetting.putSecondaryTrackId(next);
        mBinding.subtitleSecondaryText.setText(next == SubtitleSetting.SECONDARY_SUBTITLE_AUTO ? getString(R.string.subtitle_secondary_auto) : getString(R.string.subtitle_secondary_off));
    }

    private void onReset(View view) {
        SubtitleSetting.reset();
        updateView();
    }

    private int findIndex(int[] array, int value) {
        for (int i = 0; i < array.length; i++) if (array[i] == value) return i;
        return 0;
    }

    private int findNearest(float[] array, float value) {
        int index = 0;
        float diff = Math.abs(array[0] - value);
        for (int i = 1; i < array.length; i++) {
            float d = Math.abs(array[i] - value);
            if (d < diff) {
                diff = d;
                index = i;
            }
        }
        return index;
    }

    private int getStyleName(int source) {
        if (source == SubtitleSetting.STYLE_SOURCE_SYSTEM) return R.string.subtitle_style_system;
        if (source == SubtitleSetting.STYLE_SOURCE_CUSTOM) return R.string.subtitle_style_custom;
        return R.string.subtitle_style_original;
    }

    private int getTextColorName(int color) {
        if (color == SubtitleSetting.SUBTITLE_COLOR_WHITE) return R.string.subtitle_color_white;
        if (color == SubtitleSetting.SUBTITLE_COLOR_YELLOW) return R.string.subtitle_color_yellow;
        if (color == SubtitleSetting.SUBTITLE_COLOR_CYAN) return R.string.subtitle_color_cyan;
        if (color == SubtitleSetting.SUBTITLE_COLOR_GREEN) return R.string.subtitle_color_green;
        if (color == SubtitleSetting.SUBTITLE_COLOR_ORANGE) return R.string.subtitle_color_orange;
        if (color == SubtitleSetting.SUBTITLE_COLOR_PINK) return R.string.subtitle_color_pink;
        if (color == SubtitleSetting.SUBTITLE_COLOR_RED) return R.string.subtitle_color_red;
        if (color == SubtitleSetting.SUBTITLE_COLOR_BLUE) return R.string.subtitle_color_blue;
        if (color == SubtitleSetting.SUBTITLE_COLOR_BLACK) return R.string.subtitle_color_black;
        if (color == SubtitleSetting.SUBTITLE_COLOR_GRAY) return R.string.subtitle_color_gray;
        return R.string.subtitle_color_white;
    }

    private int getBackgroundColorName(int color) {
        if (color == Color.TRANSPARENT) return R.string.subtitle_color_transparent;
        if (color == SubtitleSetting.SUBTITLE_BACKGROUND_DIM) return R.string.subtitle_background_dim;
        if (color == SubtitleSetting.SUBTITLE_BACKGROUND_BLACK) return R.string.subtitle_color_black;
        if (color == SubtitleSetting.SUBTITLE_BACKGROUND_GRAY) return R.string.subtitle_color_gray;
        return R.string.subtitle_color_transparent;
    }

    private String formatScale(float value) {
        return String.format(Locale.US, "%.0f%%", value * 100);
    }

    private String formatPosition(float value) {
        return String.format(Locale.US, "%.0f%%", value);
    }
}