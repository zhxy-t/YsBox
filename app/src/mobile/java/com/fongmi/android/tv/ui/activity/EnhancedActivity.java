package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.loader.BaseLoader;
import com.fongmi.android.tv.databinding.ActivityEnhancedBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.lab.LabRunner;
import com.fongmi.android.tv.utils.GroupHeaderUtil;
import com.fongmi.android.tv.utils.download.DownloadManager;
import com.github.catvod.utils.Path;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EnhancedActivity extends BaseActivity {

    private ActivityEnhancedBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, EnhancedActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityEnhancedBinding.inflate(getLayoutInflater());
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(view -> onBackInvoked());
        refresh();
    }

    @Override
    protected void initEvent() {
        mBinding.plugin.setOnClickListener(view -> PluginActivity.start(this));
        mBinding.download.setOnClickListener(view -> DownloadActivity.start(this));
        mBinding.lab.setOnClickListener(view -> com.fongmi.android.tv.ui.lab.LabHomeActivity.start(this));
        mBinding.panDriveLogin.setOnClickListener(view -> PanDriveLoginActivity.start(this));
        mBinding.panLogin.setOnClickListener(view -> PanLoginActivity.start(this));
        mBinding.miniApp.setOnClickListener(view -> openMiniApp());
        GroupHeaderUtil.apply(mBinding.getRoot());
        mBinding.ai.setOnClickListener(view -> AiSettingsActivity.start(this));
        mBinding.tmdb.setOnClickListener(view -> TmdbActivity.start(this));
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
    }

    private void setDrpy(View view) {
        Setting.putDrpyBuiltin(!Setting.isDrpyBuiltin());
        BaseLoader.get().clear();
    }

    private void setAdblock(View view) {
        Setting.putAdblock(!Setting.isAdblock());
    }

    private void setAggregatedSearch(View view) {
        Setting.putAggregatedSearch(!Setting.isAggregatedSearch());
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
