package com.fongmi.android.tv.ui.leanback.activity;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivitySettingAppearanceBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.leanback.base.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class SettingAppearanceActivity extends BaseActivity {

    private static final String[] LANGUAGES = {"", "zh-CN", "zh-TW", "en"};
    private static final String[] LANGUAGE_NAMES = {"跟随系统", "简体中文", "繁體中文", "English"};
    private static final int[] THEME_COLORS = {-1, 0, 0xFF000000, 0xFF1B1B1B, 0xFF263238, 0xFF455A64, 0xFF6D4C41, 0xFF6750A4, 0xFF3949AB, 0xFF1E88E5, 0xFF039BE5, 0xFF00ACC1, 0xFF00897B, 0xFF43A047, 0xFF7CB342, 0xFFC0CA33, 0xFFFB8C00, 0xFFF4511E, 0xFFE53935, 0xFFD81B60, 0xFF8E24AA};

    private ActivitySettingAppearanceBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettingAppearanceActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivitySettingAppearanceBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mBinding.language.requestFocus();
        mBinding.languageText.setText(getLanguageName());
        mBinding.themeColorText.setText(getThemeColorName(Setting.getThemeColor()));
    }

    @Override
    protected void initEvent() {
        mBinding.language.setOnClickListener(this::setLanguage);
        mBinding.themeColor.setOnClickListener(this::setThemeColor);
    }

    private void setLanguage(View view) {
        int checked = indexOf(LANGUAGES, Setting.getLanguage());
        DialogThemeHelper.builder(this).setTitle(R.string.setting_language).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(LANGUAGE_NAMES, checked, (dialog, which) -> {
            Setting.putLanguage(LANGUAGES[which]);
            dialog.dismiss();
            recreate();
        }).show();
    }

    private void setThemeColor(View view) {
        int checked = findIndex(THEME_COLORS, Setting.getThemeColor());
        DialogThemeHelper.builder(this).setTitle(R.string.setting_theme_color).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(getThemeColorNames(), checked, (dialog, which) -> {
            Setting.putThemeColor(THEME_COLORS[which]);
            mBinding.themeColorText.setText(getThemeColorName(THEME_COLORS[which]));
            dialog.dismiss();
            RefreshEvent.theme();
            recreate();
        }).show();
    }

    private String getLanguageName() {
        return LANGUAGE_NAMES[indexOf(LANGUAGES, Setting.getLanguage())];
    }

    private String getThemeColorName(int color) {
        if (color == -1) return getString(R.string.setting_off);
        if (color == 0) return getString(R.string.setting_auto);
        return String.format(Locale.US, "#%06X", 0xFFFFFF & color);
    }

    private String[] getThemeColorNames() {
        String[] names = new String[THEME_COLORS.length];
        for (int i = 0; i < THEME_COLORS.length; i++) names[i] = getThemeColorName(THEME_COLORS[i]);
        return names;
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) if (array[i].equals(value)) return i;
        return 0;
    }

    private int findIndex(int[] array, int value) {
        for (int i = 0; i < array.length; i++) if (array[i] == value) return i;
        return 0;
    }
}