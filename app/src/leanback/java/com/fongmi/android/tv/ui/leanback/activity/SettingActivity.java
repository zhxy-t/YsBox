package com.fongmi.android.tv.ui.leanback.activity;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Updater;
import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.api.config.WallConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.bean.Live;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.ActivitySettingBinding;
import com.fongmi.android.tv.db.BackupManager;
import com.fongmi.android.tv.event.ConfigEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.impl.ConfigListener;
import com.fongmi.android.tv.impl.LiveListener;
import com.fongmi.android.tv.impl.SiteListener;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.leanback.base.BaseActivity;
import com.fongmi.android.tv.ui.leanback.dialog.ConfigDialog;
import com.fongmi.android.tv.ui.leanback.dialog.DohDialog;
import com.fongmi.android.tv.ui.leanback.dialog.HistoryDialog;
import com.fongmi.android.tv.ui.leanback.dialog.LiveDialog;
import com.fongmi.android.tv.ui.leanback.dialog.RestoreDialog;
import com.fongmi.android.tv.ui.leanback.dialog.SiteDialog;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.PermissionUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.bean.Doh;
import com.github.catvod.net.OkHttp;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends BaseActivity implements ConfigListener, SiteListener, LiveListener, DohDialog.Listener {

    private ActivitySettingBinding mBinding;
    private String[] backup;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettingActivity.class));
    }

    private int getDohIndex() {
        return Math.max(0, VodConfig.get().getDoh().indexOf(Doh.objectFrom(Setting.getDoh())));
    }

    private String[] getDohList() {
        List<String> list = new ArrayList<>();
        for (Doh item : VodConfig.get().getDoh()) list.add(item.getName());
        return list.toArray(new String[0]);
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivitySettingBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        com.fongmi.android.tv.utils.GroupHeaderUtil.apply(mBinding.getRoot());
        mBinding.vod.requestFocus();
        mBinding.vodUrl.setText(VodConfig.getDesc());
        mBinding.liveUrl.setText(LiveConfig.getDesc());
        mBinding.wallUrl.setText(WallConfig.getDesc());
        mBinding.versionText.setText(BuildConfig.VERSION_NAME);
        setCacheText();
        setOtherText();
    }

    private void setOtherText() {
        mBinding.dohText.setText(getDohList()[getDohIndex()]);
        mBinding.uiModeText.setText(com.fongmi.android.tv.ui.UiMode.isLeanback() ? R.string.setting_ui_mode_tv : R.string.setting_ui_mode_mobile);
        mBinding.aboutText.setText((com.fongmi.android.tv.ui.UiMode.isLeanback() ? "leanback" : "mobile"));
        mBinding.proxyText.setText(TextUtils.isEmpty(Setting.getProxy()) ? getString(R.string.setting_proxy_none) : Setting.getProxy());
        mBinding.backupText.setText((backup = ResUtil.getStringArray(R.array.select_backup))[Setting.getBackupMode()]);
    }

    private void setCacheText() {
        FileUtil.getCacheSize(new Callback() {
            @Override
            public void success(String result) {
                mBinding.cacheText.setText(result);
            }
        });
    }

    @Override
    protected void initEvent() {
        mBinding.vod.setOnClickListener(this::onVod);
        mBinding.enhanced.setOnClickListener(this::onEnhanced);
        mBinding.doh.setOnClickListener(this::setDoh);
        mBinding.proxy.setOnClickListener(this::setProxy);
        mBinding.live.setOnClickListener(this::onLive);
        mBinding.wall.setOnClickListener(this::onWall);
        mBinding.cache.setOnClickListener(this::onCache);
        mBinding.backup.setOnClickListener(this::onBackup);
        mBinding.backup.setOnLongClickListener(this::onBackupMode);
        mBinding.player.setOnClickListener(this::onPlayer);
        mBinding.danmaku.setOnClickListener(this::onDanmaku);
        mBinding.subtitle.setOnClickListener(this::onSubtitle);
        mBinding.appearance.setOnClickListener(this::onAppearance);
        mBinding.restore.setOnClickListener(this::onRestore);
        mBinding.version.setOnClickListener(this::onVersion);
        mBinding.about.setOnClickListener(this::onAbout);
        mBinding.uiMode.setOnClickListener(this::onUiMode);
        mBinding.vod.setOnLongClickListener(this::onVodEdit);
        mBinding.vodHome.setOnClickListener(this::onVodHome);
        mBinding.live.setOnLongClickListener(this::onLiveEdit);
        mBinding.liveHome.setOnClickListener(this::onLiveHome);
        mBinding.wall.setOnLongClickListener(this::onWallEdit);
        mBinding.vodHistory.setOnClickListener(this::onVodHistory);
        mBinding.liveHistory.setOnClickListener(this::onLiveHistory);
        mBinding.wallDefault.setOnClickListener(this::setWallDefault);
        mBinding.wallRefresh.setOnClickListener(this::setWallRefresh);
        mBinding.wallRefresh.setOnLongClickListener(this::onWallHistory);
    }

    @Override
    public void setConfig(Config config) {
        if (config.getUrl().startsWith("file")) {
            PermissionUtil.requestFile(this, allGranted -> load(config));
        } else {
            load(config);
        }
    }

    private void load(Config config) {
        switch (config.getType()) {
            case 0:
                VodConfig.load(config, getCallback());
                break;
            case 1:
                LiveConfig.load(config, getCallback());
                break;
            case 2:
                Setting.putWall(0);
                WallConfig.load(config, getCallback());
                break;
        }
    }

    private Callback getCallback() {
        return new Callback() {
            @Override
            public void start() {
                Notify.progress(getActivity());
            }

            @Override
            public void success() {
                Notify.dismiss();
                setCacheText();
            }

            @Override
            public void error(String msg) {
                Notify.dismiss();
                Notify.show(msg);
            }
        };
    }

    @Override
    public void setSite(Site item) {
        VodConfig.get().setHome(item);
    }

    @Override
    public void setLive(Live item) {
        LiveConfig.get().setHome(item);
    }

    private void onVod(View view) {
        ConfigDialog.create().vod().show(this);
    }

    private void onLive(View view) {
        ConfigDialog.create().live().show(this);
    }

    private void onWall(View view) {
        ConfigDialog.create().wall().show(this);
    }

    private boolean onVodEdit(View view) {
        ConfigDialog.create().vod().edit().show(this);
        return true;
    }

    private boolean onLiveEdit(View view) {
        ConfigDialog.create().live().edit().show(this);
        return true;
    }

    private boolean onWallEdit(View view) {
        ConfigDialog.create().wall().edit().show(this);
        return true;
    }

    private void onVodHome(View view) {
        SiteDialog.create().action().show(this);
    }

    private void onLiveHome(View view) {
        LiveDialog.create().action().show(this);
    }

    private void onVodHistory(View view) {
        HistoryDialog.create().vod().show(this);
    }

    private void onLiveHistory(View view) {
        HistoryDialog.create().live().show(this);
    }

    private void onPlayer(View view) {
        SettingPlayerActivity.start(this);
    }

    private void onEnhanced(View view) {
        TvEnhancedActivity.start(this);
    }

    private void onDanmaku(View view) {
        SettingDanmakuActivity.start(this);
    }

    private void onSubtitle(View view) {
        SettingSubtitleActivity.start(this);
    }

    private void onAppearance(View view) {
        SettingAppearanceActivity.start(this);
    }

    private void onVersion(View view) {
        Updater.create().force().start(this);
    }

    private void onAbout(View view) {
        mBinding.aboutText.setText((com.fongmi.android.tv.ui.UiMode.isLeanback() ? "leanback" : "mobile"));
    }

    private void setWallDefault(View view) {
        Setting.putWall(Setting.getWall() == 4 ? 1 : Setting.getWall() + 1);
        Setting.putWallType(0);
        ConfigEvent.wall();
    }

    private void setWallRefresh(View view) {
        Setting.putWall(0);
        WallConfig.get().load(getCallback());
    }

    private boolean onWallHistory(View view) {
        HistoryDialog.create().wall().show(this);
        return true;
    }

    private void setProxy(View view) {
        EditText edit = new EditText(this);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        edit.setPadding(padding, padding, padding, padding);
        edit.setSingleLine(true);
        edit.setHint("socks5://127.0.0.1:1080");
        edit.setText(Setting.getProxy());
        edit.setSelection(edit.getText().length());
        DialogThemeHelper.builder(this).setTitle(R.string.setting_proxy).setView(edit).setNegativeButton(R.string.dialog_negative, null).setPositiveButton(R.string.dialog_positive, (dialog, which) -> {
            String proxy = edit.getText().toString().trim();
            Setting.putProxy(proxy);
            OkHttp.setProxy(proxy);
            mBinding.proxyText.setText(TextUtils.isEmpty(proxy) ? getString(R.string.setting_proxy_none) : proxy);
        }).show();
    }

    private void setDoh(View view) {
        DohDialog.create().index(getDohIndex()).show(this);
    }

    @Override
    public void setDoh(Doh doh) {
        OkHttp.dns().setDoh(doh);
        Setting.putDoh(doh.toString());
        mBinding.dohText.setText(doh.getName());
    }

    private void onCache(View view) {
        FileUtil.clearCache(new Callback() {
            @Override
            public void success() {
                setCacheText();
            }
        });
    }

    private void onBackup(View view) {
        PermissionUtil.requestFile(this, allGranted -> BackupManager.backup(new Callback() {
            @Override
            public void success() {
                Notify.show(R.string.backup_success);
            }

            @Override
            public void error() {
                Notify.show(R.string.backup_fail);
            }
        }));
    }

    private boolean onBackupMode(View view) {
        int index = Setting.getBackupMode();
        Setting.putBackupMode(index = index == backup.length - 1 ? 0 : index + 1);
        mBinding.backupText.setText(backup[index]);
        return true;
    }

    private void onRestore(View view) {
        PermissionUtil.requestFile(this, allGranted -> RestoreDialog.create().callback(new Callback() {
            @Override
            public void success() {
                Notify.show(R.string.restore_success);
                setOtherText();
                initConfig();
            }

            @Override
            public void error() {
                Notify.show(R.string.restore_fail);
            }
        }).show(this));
    }

    private void onUiMode(View view) {
        DialogThemeHelper.builder(this)
                .setTitle(R.string.setting_ui_mode_confirm)
                .setMessage(R.string.setting_ui_mode_confirm_msg)
                .setNegativeButton(R.string.dialog_negative, null)
                .setPositiveButton(R.string.dialog_positive, (d, w) -> {
                    boolean leanback = com.fongmi.android.tv.ui.UiMode.isLeanback();
                    com.fongmi.android.tv.ui.UiMode.set(leanback ? com.fongmi.android.tv.ui.UiMode.MOBILE : com.fongmi.android.tv.ui.UiMode.LEANBACK);
                    startActivity(new Intent(this, com.fongmi.android.tv.ui.activity.LauncherActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                })
                .show();
    }

    private void initConfig() {
        VodConfig.get().init().load(getCallback());
        LiveConfig.get().init().load();
        WallConfig.get().init().load();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigEvent(ConfigEvent event) {
        if (event.type() != ConfigEvent.Type.COMMON) return;
        mBinding.vodUrl.setText(VodConfig.getDesc());
        mBinding.liveUrl.setText(LiveConfig.getDesc());
        mBinding.wallUrl.setText(WallConfig.getDesc());
    }

}
