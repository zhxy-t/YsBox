package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.loader.BaseLoader;
import com.fongmi.android.tv.databinding.ActivityPersonalFeaturesBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.utils.GroupHeaderUtil;

/** 个性功能页：与增强功能页同构的独立列表页，条目来自设置页“个性功能”入口。 */
public class PersonalFeaturesActivity extends BaseActivity {

    private ActivityPersonalFeaturesBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, PersonalFeaturesActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityPersonalFeaturesBinding.inflate(getLayoutInflater());
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(view -> onBackInvoked());
        GroupHeaderUtil.apply(mBinding.getRoot());
        refresh();
    }

    @Override
    protected void initEvent() {
        mBinding.incognito.setOnClickListener(this::setIncognito);
        mBinding.drpy.setOnClickListener(this::setDrpy);
        mBinding.adblock.setOnClickListener(this::setAdblock);
        mBinding.aggregatedSearch.setOnClickListener(this::setAggregatedSearch);
        mBinding.miniApp.setOnClickListener(view -> toggleMiniApp());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBinding != null) refresh();
    }

    private void refresh() {
        mBinding.incognitoText.setText(Setting.getSwitch(Setting.isIncognito()));
        mBinding.drpyText.setText(Setting.getSwitch(Setting.isDrpyBuiltin()));
        mBinding.adblockText.setText(Setting.getSwitch(Setting.isAdblock()));
        mBinding.aggregatedSearchText.setText(Setting.getSwitch(Setting.isAggregatedSearch()));
        mBinding.miniAppText.setText(MiniAppActivity.isEnabled(this) ? R.string.state_on : R.string.state_off);
    }

    private void setIncognito(View view) {
        Setting.putIncognito(!Setting.isIncognito());
        refresh();
    }

    private void setDrpy(View view) {
        Setting.putDrpyBuiltin(!Setting.isDrpyBuiltin());
        BaseLoader.get().clear();
        refresh();
    }

    private void setAdblock(View view) {
        Setting.putAdblock(!Setting.isAdblock());
        refresh();
    }

    private void setAggregatedSearch(View view) {
        Setting.putAggregatedSearch(!Setting.isAggregatedSearch());
        RefreshEvent.home();
        refresh();
    }

    private void toggleMiniApp() {
        boolean enabled = !MiniAppActivity.isEnabled(this);
        MiniAppActivity.setEnabled(this, enabled);
        refresh();
    }
}
