package com.fongmi.android.tv.ui.fragment;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.FragmentSettingSubtitleBinding;
import com.fongmi.android.tv.setting.SubtitleSetting;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.dialog.ColorPickerDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.function.IntConsumer;

public class SettingSubtitleFragment extends BaseFragment {

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
    private static final long[] DELAYS = {-3000, -2000, -1000, -500, 0, 500, 1000, 2000, 3000};
    private static final String[] LANGUAGE_TAGS = {"", "zh-Hans", "zh-Hant", "en", "ja", "ko"};
    private static final String[] LANGUAGE_NAMES = {"跟随系统", "简体中文", "繁體中文", "English", "日本語", "한국어"};

    private FragmentSettingSubtitleBinding mBinding;

    public static SettingSubtitleFragment newInstance() {
        return new SettingSubtitleFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentSettingSubtitleBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
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
        mBinding.subtitleAutoMatch.setOnClickListener(this::setAutoMatch);
        mBinding.subtitleDelay.setOnClickListener(this::setDelay);
        mBinding.subtitleLanguage.setOnClickListener(this::setLanguage);
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
        mBinding.subtitleAutoMatchText.setText(SubtitleSetting.isAutoMatch() ? R.string.subtitle_auto_match_on : R.string.subtitle_auto_match_off);
        mBinding.subtitleDelayText.setText(formatDelay(SubtitleSetting.getDelayMs()));
        mBinding.subtitleLanguageText.setText(getLanguageName(SubtitleSetting.getPreferredLanguage()));
    }

    private void setStyle(View view) {
        SubtitleSetting.putStyleSource((SubtitleSetting.getStyleSource() + 1) % 3);
        mBinding.subtitleStyleText.setText(getStyleName(SubtitleSetting.getStyleSource()));
    }

    private void setSize(View view) {
        String[] items = new String[SCALES.length];
        for (int i = 0; i < SCALES.length; i++) items[i] = formatScale(SCALES[i]);
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.subtitle_size).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(items, findNearest(SCALES, SubtitleSetting.getScale()), (dialog, which) -> {
            SubtitleSetting.putScale(SCALES[which]);
            mBinding.subtitleSizeText.setText(items[which]);
            dialog.dismiss();
        }).show();
    }

    private void setPosition(View view) {
        String[] items = new String[POSITIONS.length];
        for (int i = 0; i < POSITIONS.length; i++) items[i] = formatPosition(POSITIONS[i]);
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.subtitle_position).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(items, findNearest(POSITIONS, SubtitleSetting.getPosition()), (dialog, which) -> {
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
        ColorPickerDialog.show(requireActivity(), R.string.subtitle_background, BACKGROUND_COLORS, SubtitleSetting.getBackgroundBaseColor(), color -> {
            SubtitleSetting.putBackgroundColor(color);
            mBinding.subtitleBackgroundText.setText(getString(getBackgroundColorName(color)));
        });
    }

    private void showColorDialog(int title, int[] colors, int current, IntConsumer apply) {
        ColorPickerDialog.show(requireActivity(), title, colors, current, apply);
    }

    private void setSecondary(View view) {
        int current = SubtitleSetting.getSecondaryTrackId();
        int next = current == SubtitleSetting.SECONDARY_SUBTITLE_AUTO ? SubtitleSetting.SECONDARY_SUBTITLE_OFF : SubtitleSetting.SECONDARY_SUBTITLE_AUTO;
        SubtitleSetting.putSecondaryTrackId(next);
        mBinding.subtitleSecondaryText.setText(next == SubtitleSetting.SECONDARY_SUBTITLE_AUTO ? getString(R.string.subtitle_secondary_auto) : getString(R.string.subtitle_secondary_off));
    }

    private void setAutoMatch(View view) {
        SubtitleSetting.putAutoMatch(!SubtitleSetting.isAutoMatch());
        mBinding.subtitleAutoMatchText.setText(SubtitleSetting.isAutoMatch() ? R.string.subtitle_auto_match_on : R.string.subtitle_auto_match_off);
    }

    private void setDelay(View view) {
        String[] items = new String[DELAYS.length];
        for (int i = 0; i < DELAYS.length; i++) items[i] = formatDelay(DELAYS[i]);
        int index = 0;
        for (int i = 0; i < DELAYS.length; i++) if (DELAYS[i] == SubtitleSetting.getDelayMs()) index = i;
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.subtitle_delay).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(items, index, (dialog, which) -> {
            SubtitleSetting.putDelayMs(DELAYS[which]);
            mBinding.subtitleDelayText.setText(items[which]);
            dialog.dismiss();
        }).show();
    }

    private void setLanguage(View view) {
        int index = 0;
        for (int i = 0; i < LANGUAGE_TAGS.length; i++) if (LANGUAGE_TAGS[i].equals(SubtitleSetting.getPreferredLanguage())) index = i;
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.subtitle_preferred_language).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(LANGUAGE_NAMES, index, (dialog, which) -> {
            SubtitleSetting.putPreferredLanguage(LANGUAGE_TAGS[which]);
            mBinding.subtitleLanguageText.setText(LANGUAGE_NAMES[which]);
            dialog.dismiss();
        }).show();
    }

    private String getLanguageName(String tag) {
        for (int i = 0; i < LANGUAGE_TAGS.length; i++) if (LANGUAGE_TAGS[i].equals(tag)) return LANGUAGE_NAMES[i];
        return LANGUAGE_NAMES[0];
    }

    private String formatDelay(long ms) {
        if (ms == 0) return "0.0s";
        return String.format(Locale.US, "%+.1fs", ms / 1000f);
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