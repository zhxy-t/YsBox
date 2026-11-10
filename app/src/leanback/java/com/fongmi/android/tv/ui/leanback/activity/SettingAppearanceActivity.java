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
import android.net.Uri;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.WallUtil;
import com.fongmi.android.tv.ui.dialog.DialogStylePickerDialog;
import com.fongmi.android.tv.ui.dialog.WallPickerDialog;

public class SettingAppearanceActivity extends BaseActivity {

    private static final String[] LANGUAGES = {"", "zh-CN", "zh-TW", "en"};
    private static final String[] LANGUAGE_NAMES = {"跟随系统", "简体中文", "繁體中文", "English"};
    private static final String[] RATIO_NAMES = {"跟随站点", "竖屏 3:4", "横屏 16:9", "方形 1:1"};
    private static final String[] RADIUS_NAMES = {"跟随站点", "直角", "4dp", "8dp", "12dp", "圆形"};
    private static final int REQUEST_WALL_LOCAL = 1003;
    private String[] sizes;
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
        mBinding.dialogStyleText.setText(DialogStylePickerDialog.nameOf(Setting.getDialogStyle()));
        updateFontSizeText();
        sizes = ResUtil.getStringArray(R.array.select_size);
        mBinding.coverText.setText(sizes[PlayerSetting.getSize()]);
        mBinding.coverRatioText.setText(RATIO_NAMES[PlayerSetting.getCoverRatio()]);
        mBinding.coverRadiusText.setText(RADIUS_NAMES[PlayerSetting.getCoverRadius()]);
        mBinding.wallDefaultText.setText(Setting.getWall() == 0 ? getString(R.string.wall_random) : getString(R.string.wall_builtin, getBuiltInWall()));
        mBinding.wallLocalText.setText(R.string.wall_local_desc);
        mBinding.wallUrlText.setText(R.string.wall_url_desc);
        mBinding.wallBlurText.setText(Setting.isWallBlur() ? R.string.state_on : R.string.state_off);
    }

    @Override
    protected void initEvent() {
        mBinding.language.setOnClickListener(this::setLanguage);
        mBinding.themeColor.setOnClickListener(this::setThemeColor);
        mBinding.dialogStyle.setOnClickListener(this::setDialogStyle);
        mBinding.fontSize.setOnClickListener(this::showFontSizeDialog);
        mBinding.wallBlur.setOnClickListener(this::toggleWallBlur);
        mBinding.wallDefault.setOnClickListener(this::setWallDefault);
        mBinding.wallLocal.setOnClickListener(this::setWallLocal);
        mBinding.wallUrl.setOnClickListener(this::setWallUrl);
        mBinding.cover.setOnClickListener(this::setCover);
        mBinding.coverRatio.setOnClickListener(this::setCoverRatio);
        mBinding.coverRadius.setOnClickListener(this::setCoverRadius);
    }

    private void setLanguage(View view) {
        int checked = indexOf(LANGUAGES, Setting.getLanguage());
        DialogThemeHelper.builder(this).setTitle(R.string.setting_language).setSingleChoiceItems(LANGUAGE_NAMES, checked, (dialog, which) -> {
            Setting.putLanguage(LANGUAGES[which]);
            dialog.dismiss();
            recreate();
        }).show();
    }

    private void setThemeColor(View view) {
        int checked = findIndex(THEME_COLORS, Setting.getThemeColor());
        DialogThemeHelper.builder(this).setTitle(R.string.setting_theme_color).setSingleChoiceItems(getThemeColorNames(), checked, (dialog, which) -> {
            Setting.putThemeColor(THEME_COLORS[which]);
            mBinding.themeColorText.setText(getThemeColorName(THEME_COLORS[which]));
            dialog.dismiss();
            RefreshEvent.theme();
            recreate();
        }).show();
    }

    private void setDialogStyle(View view) {
        DialogStylePickerDialog.show(this, style ->
                mBinding.dialogStyleText.setText(DialogStylePickerDialog.nameOf(style)));
    }

    private void updateFontSizeText() {
        int[] labels = {R.string.setting_font_size_0, R.string.setting_font_size_1, R.string.setting_font_size_2, R.string.setting_font_size_3, R.string.setting_font_size_4, R.string.setting_font_size_5};
        mBinding.fontSizeText.setText(labels[Setting.getFontScaleIndex()]);
    }

    private void showFontSizeDialog(View view) {
        CharSequence[] items = {getString(R.string.setting_font_size_0), getString(R.string.setting_font_size_1), getString(R.string.setting_font_size_2), getString(R.string.setting_font_size_3), getString(R.string.setting_font_size_4), getString(R.string.setting_font_size_5)};
        int checked = Setting.getFontScaleIndex();
        DialogThemeHelper.builder(this)
                .setTitle(R.string.setting_font_size)
                .setSingleChoiceItems(items, checked, (d, which) -> {
                    Setting.putFontScaleIndex(which);
                    d.dismiss();
                    updateFontSizeText();
                    recreate();
                })
                .show();
    }

    private void toggleWallBlur(View view) {
        Setting.putWallBlur(!Setting.isWallBlur());
        mBinding.wallBlurText.setText(Setting.isWallBlur() ? R.string.state_on : R.string.state_off);
        ConfigEvent.wall();
    }

    private void setWallDefault(View view) {
        WallPickerDialog.show(this, wall ->
                mBinding.wallDefaultText.setText(wall == 0 ? getString(R.string.wall_random) : getString(R.string.wall_builtin, wall)));
    }

    private void setWallLocal(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_WALL_LOCAL);
    }

    private void setWallUrl(View view) {
        EditText edit = new EditText(this);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        edit.setPadding(padding, padding, padding, padding);
        edit.setSingleLine(true);
        edit.setHint(R.string.wall_url_hint);
        AlertDialog dialog = DialogThemeHelper.builder(this).setTitle(R.string.wall_url).setView(edit).setNegativeButton(R.string.dialog_negative, null).setPositiveButton(R.string.dialog_positive, null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = edit.getText().toString().trim();
            if (TextUtils.isEmpty(url)) {
                edit.setError(getString(R.string.wall_url_hint));
                return;
            }
            dialog.dismiss();
            applyWallUrl(url);
        }));
        dialog.show();
    }

    private void applyWallLocal(Uri uri) {
        WallUtil.applyLocal(uri, getWallListener());
    }

    private void applyWallUrl(String url) {
        WallUtil.applyUrl(url, getWallListener());
    }

    private WallUtil.Listener getWallListener() {
        return new WallUtil.Listener() {
            @Override
            public void success() {
                Toast.makeText(SettingAppearanceActivity.this, R.string.wall_applied, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void error() {
                Toast.makeText(SettingAppearanceActivity.this, R.string.wall_failed, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private int getBuiltInWall() {
        int wall = Setting.getWall();
        return wall >= WallUtil.MIN_BUILT_IN && wall <= WallUtil.MAX_BUILT_IN ? wall : WallUtil.MIN_BUILT_IN;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_WALL_LOCAL || resultCode != Activity.RESULT_OK) return;
        if (data == null || data.getData() == null) return;
        mBinding.wallLocalText.setText(R.string.wall_local_desc);
        applyWallLocal(data.getData());
    }

    private void setCover(View view) {
        DialogThemeHelper.builder(this).setTitle(R.string.setting_cover).setSingleChoiceItems(sizes, PlayerSetting.getSize(), (dialog, which) -> {
            mBinding.coverText.setText(sizes[which]);
            PlayerSetting.putSize(which);
            RefreshEvent.size();
            dialog.dismiss();
        }).show();
    }

    private void setCoverRatio(View view) {
        DialogThemeHelper.builder(this).setTitle(R.string.setting_cover_ratio).setSingleChoiceItems(RATIO_NAMES, PlayerSetting.getCoverRatio(), (dialog, which) -> {
            PlayerSetting.putCoverRatio(which);
            mBinding.coverRatioText.setText(RATIO_NAMES[which]);
            RefreshEvent.size();
            dialog.dismiss();
        }).show();
    }

    private void setCoverRadius(View view) {
        DialogThemeHelper.builder(this).setTitle(R.string.setting_cover_radius).setSingleChoiceItems(RADIUS_NAMES, PlayerSetting.getCoverRadius(), (dialog, which) -> {
            PlayerSetting.putCoverRadius(which);
            mBinding.coverRadiusText.setText(RADIUS_NAMES[which]);
            RefreshEvent.size();
            dialog.dismiss();
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