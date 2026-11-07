package com.fongmi.android.tv.ui.leanback.activity;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.loader.BaseLoader;
import com.fongmi.android.tv.databinding.ActivitySettingPersonalBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.leanback.base.BaseActivity;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.utils.Shell;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingPersonalActivity extends BaseActivity {

    private ActivitySettingPersonalBinding mBinding;
    private String[] size;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettingPersonalActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivitySettingPersonalBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mBinding.incognito.requestFocus();
        mBinding.incognitoText.setText(Setting.getSwitch(Setting.isIncognito()));
        mBinding.drpyText.setText(Setting.getSwitch(Setting.isDrpyBuiltin()));
        mBinding.adblockText.setText(Setting.getSwitch(Setting.isAdblock()));
        mBinding.aggregatedSearchText.setText(Setting.getSwitch(Setting.isAggregatedSearch()));
        mBinding.sizeText.setText((size = ResUtil.getStringArray(R.array.select_size))[PlayerSetting.getSize()]);
    }

    @Override
    protected void initEvent() {
        mBinding.incognito.setOnClickListener(this::setIncognito);
        mBinding.drpy.setOnClickListener(this::setDrpy);
        mBinding.adblock.setOnClickListener(this::setAdblock);
        mBinding.aggregatedSearch.setOnClickListener(this::setAggregatedSearch);
        mBinding.reset.setOnClickListener(this::onReset);
        mBinding.size.setOnClickListener(this::setSize);
    }

    private void setIncognito(View view) {
        Setting.putIncognito(!Setting.isIncognito());
        mBinding.incognitoText.setText(Setting.getSwitch(Setting.isIncognito()));
    }

    private void setDrpy(View view) {
        Setting.putDrpyBuiltin(!Setting.isDrpyBuiltin());
        mBinding.drpyText.setText(Setting.getSwitch(Setting.isDrpyBuiltin()));
        BaseLoader.get().clear();
    }

    private void setAdblock(View view) {
        Setting.putAdblock(!Setting.isAdblock());
        mBinding.adblockText.setText(Setting.getSwitch(Setting.isAdblock()));
    }

    private void setAggregatedSearch(View view) {
        Setting.putAggregatedSearch(!Setting.isAggregatedSearch());
        mBinding.aggregatedSearchText.setText(Setting.getSwitch(Setting.isAggregatedSearch()));
        RefreshEvent.home();
    }

    private void onReset(View view) {
        DialogThemeHelper.builder(this).setTitle(R.string.dialog_reset_app).setMessage(R.string.dialog_reset_app_data).setNegativeButton(R.string.dialog_negative, null).setPositiveButton(R.string.dialog_positive, (dialog, which) -> new Thread(() -> Shell.exec("pm clear " + getPackageName())).start()).show();
    }

    private void setSize(View view) {
        DialogThemeHelper.builder(this).setTitle(R.string.setting_size).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(size, PlayerSetting.getSize(), (dialog, which) -> {
            mBinding.sizeText.setText(size[which]);
            PlayerSetting.putSize(which);
            RefreshEvent.size();
            dialog.dismiss();
        }).show();
    }
}
