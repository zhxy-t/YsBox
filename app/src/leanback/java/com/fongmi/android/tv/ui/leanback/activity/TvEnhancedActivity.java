package com.fongmi.android.tv.ui.leanback.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.loader.BaseLoader;
import com.fongmi.android.tv.databinding.TvActivityEnhancedBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.activity.AiSettingsActivity;
import com.fongmi.android.tv.ui.activity.DownloadActivity;
import com.fongmi.android.tv.ui.activity.HkPageActivity;
import com.fongmi.android.tv.ui.activity.MiniAppActivity;
import com.fongmi.android.tv.ui.activity.PanDriveLoginActivity;
import com.fongmi.android.tv.ui.activity.PanLoginActivity;
import com.fongmi.android.tv.ui.activity.PluginActivity;
import com.fongmi.android.tv.ui.activity.TmdbActivity;
import com.fongmi.android.tv.ui.lab.LabHomeActivity;
import com.fongmi.android.tv.ui.lab.LabRunner;
import com.fongmi.android.tv.ui.leanback.base.BaseActivity;
import com.fongmi.android.tv.utils.GroupHeaderUtil;
import com.fongmi.android.tv.utils.download.DownloadManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * TV 版增强功能页：原生 leanback 布局（1080p 基准），与设置页风格一致；
 * 手机模式仍走 mobile 的 EnhancedActivity，两套界面互不影响。
 */
public class TvEnhancedActivity extends BaseActivity {

    private TvActivityEnhancedBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, TvEnhancedActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = TvActivityEnhancedBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        refresh();
        mBinding.lab.requestFocus();
    }

    @Override
    protected void initEvent() {
        mBinding.plugin.setOnClickListener(view -> PluginActivity.start(this));
        mBinding.download.setOnClickListener(view -> DownloadActivity.start(this));
        mBinding.lab.setOnClickListener(view -> LabHomeActivity.start(this));
        mBinding.panDriveLogin.setOnClickListener(view -> PanDriveLoginActivity.start(this));
        mBinding.panLogin.setOnClickListener(view -> PanLoginActivity.start(this));
        mBinding.miniApp.setOnClickListener(view -> openMiniApp());
        mBinding.miniAppText.setOnClickListener(view -> toggleMiniApp());
        mBinding.incognito.setOnClickListener(this::setIncognito);
        mBinding.drpy.setOnClickListener(this::setDrpy);
        mBinding.adblock.setOnClickListener(this::setAdblock);
        mBinding.aggregatedSearch.setOnClickListener(this::setAggregatedSearch);
        mBinding.ai.setOnClickListener(view -> AiSettingsActivity.start(this));
        mBinding.tmdb.setOnClickListener(view -> TmdbActivity.start(this));
        GroupHeaderUtil.apply(mBinding.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBinding != null) refresh();
    }

    private void refresh() {
        int plugins = countPlugins(true) + countPlugins(false);
        mBinding.pluginText.setText(plugins == 0 ? getString(R.string.setting_plugin_desc) : getString(R.string.plugin_count, plugins));
        int tasks = DownloadManager.get().getTasks().size();
        mBinding.downloadText.setText(tasks == 0 ? getString(R.string.setting_download_desc) : getString(R.string.download_count, tasks));
        mBinding.labText.setText(getString(R.string.lab_running_summary, LabRunner.runningCount(this)));
        mBinding.panDriveLoginText.setText(R.string.pan_drive_unsupported);
        mBinding.panLoginText.setText(PanLoginActivity.getSummary(this));
        mBinding.incognitoText.setText(Setting.getSwitch(Setting.isIncognito()));
        mBinding.drpyText.setText(Setting.getSwitch(Setting.isDrpyBuiltin()));
        mBinding.adblockText.setText(Setting.getSwitch(Setting.isAdblock()));
        mBinding.aggregatedSearchText.setText(Setting.getSwitch(Setting.isAggregatedSearch()));
        mBinding.miniAppText.setText(MiniAppActivity.isEnabled(this) ? R.string.state_on : R.string.state_off);
        mBinding.aiText.setText(com.fongmi.android.tv.ai.AiStore.summary(this));
        mBinding.tmdbText.setText(TmdbActivity.getSummary(this));
    }

    private int countPlugins(boolean py) {
        // 只统计用户脚本目录；cache/py|js 是运行时缓存，不计入「共 N 个脚本」
        File own = new File(getFilesDir(), py ? "plugins/py" : "plugins/js");
        Set<String> names = new HashSet<>();
        collect(own, py, names);
        return names.size();
    }

    private void collect(File dir, boolean py, Set<String> names) {
        String[] files = dir == null ? null : dir.list();
        if (files == null) return;
        String ext = py ? ".py" : ".js";
        for (String name : files) if (name.endsWith(ext)) names.add(name);
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

    private void toggleMiniApp() {
        boolean enabled = !MiniAppActivity.isEnabled(this);
        MiniAppActivity.setEnabled(this, enabled);
        refresh();
    }

    private void openMiniApp() {
        if (!MiniAppActivity.isEnabled(this)) MiniAppActivity.setEnabled(this, true);
        HkPageActivity.start(this);
        refresh();
    }
}
