package com.fongmi.android.tv.ui.fragment;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.FragmentSettingAppearanceBinding;
import com.fongmi.android.tv.event.ConfigEvent;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.dialog.DialogStylePickerDialog;
import com.fongmi.android.tv.ui.dialog.ThemeDialog;
import com.fongmi.android.tv.ui.dialog.WallPickerDialog;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.WallUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class SettingAppearanceFragment extends BaseFragment implements ThemeDialog.Listener {

    private static final String[] LANGUAGES = {"", "zh-CN", "zh-TW", "en"};
    private static final String[] LANGUAGE_NAMES = {"跟随系统", "简体中文", "繁體中文", "English"};
    private static final String[] RATIO_NAMES = {"跟随站点", "竖屏 3:4", "横屏 16:9", "方形 1:1"};
    private static final String[] RADIUS_NAMES = {"跟随站点", "直角", "4dp", "8dp", "12dp", "圆形"};
    private static final int REQUEST_WALL_LOCAL = 1003;

    private FragmentSettingAppearanceBinding mBinding;
    private String[] sizes;

    public static SettingAppearanceFragment newInstance() {
        return new SettingAppearanceFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentSettingAppearanceBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        mBinding.languageText.setText(getLanguageName());
        mBinding.themeColorText.setText(getThemeColorName(Setting.getThemeColor()));
        mBinding.dialogStyleText.setText(getDialogStyleName());
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
        mBinding.wallDefault.setOnClickListener(this::setWallDefault);
        mBinding.wallLocal.setOnClickListener(this::setWallLocal);
        mBinding.wallUrl.setOnClickListener(this::setWallUrl);
        mBinding.wallBlur.setOnClickListener(view -> {
            Setting.putWallBlur(!Setting.isWallBlur());
            mBinding.wallBlurText.setText(Setting.isWallBlur() ? R.string.state_on : R.string.state_off);
            ConfigEvent.wall();
        });
        mBinding.cover.setOnClickListener(this::setCover);
        mBinding.coverRatio.setOnClickListener(this::setCoverRatio);
        mBinding.coverRadius.setOnClickListener(this::setCoverRadius);
    }

    private void setWallDefault(View view) {
        WallPickerDialog.show(requireActivity(), wall ->
                mBinding.wallDefaultText.setText(wall == 0 ? getString(R.string.wall_random) : getString(R.string.wall_builtin, wall)));
    }

    private void setWallLocal(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_WALL_LOCAL);
    }

    private void setWallUrl(View view) {
        EditText edit = new EditText(requireActivity());
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        edit.setPadding(padding, padding, padding, padding);
        edit.setSingleLine(true);
        edit.setHint(R.string.wall_url_hint);
        AlertDialog dialog = DialogThemeHelper.builder(requireActivity()).setTitle(R.string.wall_url).setView(edit).setNegativeButton(R.string.dialog_negative, null).setPositiveButton(R.string.dialog_positive, null).create();
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
                if (!isAdded()) return;
                Toast.makeText(requireContext(), R.string.wall_applied, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void error() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), R.string.wall_failed, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private int getBuiltInWall() {
        int wall = Setting.getWall();
        return wall >= WallUtil.MIN_BUILT_IN && wall <= WallUtil.MAX_BUILT_IN ? wall : WallUtil.MIN_BUILT_IN;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_WALL_LOCAL || resultCode != Activity.RESULT_OK) return;
        if (data == null || data.getData() == null) return;
        mBinding.wallLocalText.setText(R.string.wall_local_desc);
        applyWallLocal(data.getData());
    }

    private void setCover(View view) {
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.setting_cover).setSingleChoiceItems(sizes, PlayerSetting.getSize(), (dialog, which) -> {
            mBinding.coverText.setText(sizes[which]);
            PlayerSetting.putSize(which);
            RefreshEvent.size();
            dialog.dismiss();
        }).show();
    }

    private void setCoverRatio(View view) {
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.setting_cover_ratio).setSingleChoiceItems(RATIO_NAMES, PlayerSetting.getCoverRatio(), (dialog, which) -> {
            PlayerSetting.putCoverRatio(which);
            mBinding.coverRatioText.setText(RATIO_NAMES[which]);
            RefreshEvent.size();
            dialog.dismiss();
        }).show();
    }

    private void setCoverRadius(View view) {
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.setting_cover_radius).setSingleChoiceItems(RADIUS_NAMES, PlayerSetting.getCoverRadius(), (dialog, which) -> {
            PlayerSetting.putCoverRadius(which);
            mBinding.coverRadiusText.setText(RADIUS_NAMES[which]);
            RefreshEvent.size();
            dialog.dismiss();
        }).show();
    }

    private void setLanguage(View view) {
        int checked = indexOf(LANGUAGES, Setting.getLanguage());
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.setting_language).setSingleChoiceItems(LANGUAGE_NAMES, checked, (dialog, which) -> {
            Setting.putLanguage(LANGUAGES[which]);
            dialog.dismiss();
            requireActivity().recreate();
        }).show();
    }

    private void setThemeColor(View view) {
        ThemeDialog.show(this);
    }

    private void updateFontSizeText() {
        int[] labels = {R.string.setting_font_size_0, R.string.setting_font_size_1, R.string.setting_font_size_2, R.string.setting_font_size_3, R.string.setting_font_size_4, R.string.setting_font_size_5};
        mBinding.fontSizeText.setText(labels[Setting.getFontScaleIndex()]);
    }

    private void showFontSizeDialog(View view) {
        CharSequence[] items = {getString(R.string.setting_font_size_0), getString(R.string.setting_font_size_1), getString(R.string.setting_font_size_2), getString(R.string.setting_font_size_3), getString(R.string.setting_font_size_4), getString(R.string.setting_font_size_5)};
        int checked = Setting.getFontScaleIndex();
        DialogThemeHelper.builder(requireActivity())
                .setTitle(R.string.setting_font_size)
                .setSingleChoiceItems(items, checked, (d, which) -> {
                    Setting.putFontScaleIndex(which);
                    d.dismiss();
                    updateFontSizeText();
                    requireActivity().recreate();
                })
                .show();
    }

    private void setDialogStyle(View view) {
        DialogStylePickerDialog.show(requireActivity(), style ->
                mBinding.dialogStyleText.setText(DialogStylePickerDialog.nameOf(style)));
    }
    private String getDialogStyleName() {
        return DialogStylePickerDialog.nameOf(Setting.getDialogStyle());
    }

    @Override
    public void setTheme(int color) {
        Setting.putThemeColor(color);
        mBinding.themeColorText.setText(getThemeColorName(color));
        RefreshEvent.theme();
    }

    private String getLanguageName() {
        return LANGUAGE_NAMES[indexOf(LANGUAGES, Setting.getLanguage())];
    }

    private String getThemeColorName(int color) {
        if (color == -1) return getString(R.string.setting_off);
        if (color == 0) return getString(R.string.setting_auto);
        return String.format(Locale.US, "#%06X", 0xFFFFFF & color);
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) if (array[i].equals(value)) return i;
        return 0;
    }
}