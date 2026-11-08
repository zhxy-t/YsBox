package com.fongmi.android.tv.ui.leanback.activity;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivitySettingPlayerBinding;
import com.fongmi.android.tv.impl.BufferListener;
import com.fongmi.android.tv.impl.UaListener;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.setting.SpeedSetting;
import com.fongmi.android.tv.ui.leanback.base.BaseActivity;
import com.fongmi.android.tv.ui.leanback.dialog.BufferDialog;
import com.fongmi.android.tv.ui.leanback.dialog.UaDialog;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingPlayerActivity extends BaseActivity implements UaListener, BufferListener {

    private ActivitySettingPlayerBinding mBinding;
    private String[] render;
    private String[] scale;
    private String[] engine;
    private String[] flag;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettingPlayerActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivitySettingPlayerBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setVisible();
        setPlaybackModeText();
        mBinding.engine.requestFocus();
        mBinding.flagText.setText((flag = ResUtil.getStringArray(R.array.select_flag))[Setting.getFlag()]);
        mBinding.playSpeedText.setText(SpeedSetting.format(SpeedSetting.getPlayback()));
        mBinding.longPressText.setText(SpeedSetting.format(SpeedSetting.getLongPress()));
        mBinding.bufferText.setText(String.valueOf(PlayerSetting.getBuffer()));
        mBinding.backgroundText.setText(Setting.getSwitch(PlayerSetting.isBackgroundOn()));
        mBinding.scaleText.setText((scale = ResUtil.getStringArray(R.array.select_scale))[PlayerSetting.getScale()]);
    }

    @Override
    protected void initEvent() {
        mBinding.engine.setOnClickListener(this::setEngine);
        mBinding.decode.setOnClickListener(this::onDecodeSetting);
        mBinding.flag.setOnClickListener(this::setFlag);
        mBinding.playSpeed.setOnClickListener(this::onPlaybackSpeed);
        mBinding.longPress.setOnClickListener(this::onLongPress);
        mBinding.render.setOnClickListener(this::setRender);
        mBinding.scale.setOnClickListener(this::setScale);
        mBinding.background.setOnClickListener(this::onBackground);
        mBinding.buffer.setOnClickListener(this::onBuffer);
        mBinding.preload.setOnClickListener(this::onPreloadSetting);
        mBinding.ua.setOnClickListener(this::onUa);
    }

    private void setVisible() {
        boolean exo = PlayerSetting.isExo();
        if (PlayerSetting.isBackgroundPiP()) PlayerSetting.putBackground(1);
        mBinding.buffer.setVisibility(exo ? View.VISIBLE : View.GONE);
    }

    private void setEngine(View view) {
        DialogThemeHelper.builder(this).setTitle(R.string.player_engine).setSingleChoiceItems(engine, PlayerSetting.getEngine(), (dialog, which) -> {
            if (which == PlayerSetting.ENGINE_MPV && !com.fongmi.android.tv.player.engine.MpvPlayerEngine.isAvailable()) {
                com.fongmi.android.tv.utils.Notify.show(R.string.error_play_mpv_load_failed);
                return;
            }
            PlayerSetting.putEngine(which);
            setPlaybackModeText();
            setVisible();
            dialog.dismiss();
        }).show();
    }

    private void setRender(View view) {
        int index = (PlayerSetting.getRender() + 1) % render.length;
        PlayerSetting.putRender(index);
        setPlaybackModeText();
    }

    private void setPlaybackModeText() {
        engine = ResUtil.getStringArray(R.array.select_engine);
        render = ResUtil.getStringArray(R.array.select_render);
        mBinding.engineText.setText(engine[PlayerSetting.getEngine()]);
        mBinding.renderText.setText(render[PlayerSetting.getRender()]);
    }

    private void setScale(View view) {
        DialogThemeHelper.builder(this).setTitle(R.string.player_scale).setSingleChoiceItems(scale, PlayerSetting.getScale(), (dialog, which) -> {
            mBinding.scaleText.setText(scale[which]);
            PlayerSetting.putScale(which);
            dialog.dismiss();
        }).show();
    }

    private void onBuffer(View view) {
        BufferDialog.show(this);
    }

    @Override
    public void setBuffer(int times) {
        mBinding.bufferText.setText(String.valueOf(times));
        PlayerSetting.putBuffer(times);
    }

    private void onBackground(View view) {
        PlayerSetting.putBackground(PlayerSetting.isBackgroundOn() ? 0 : 1);
        mBinding.backgroundText.setText(Setting.getSwitch(PlayerSetting.isBackgroundOn()));
    }

    private void onPlaybackSpeed(View view) {
        showSpeedDialog(false);
    }

    private void onLongPress(View view) {
        showSpeedDialog(true);
    }

    private void showSpeedDialog(boolean longPress) {
        float[] values = longPress ? SpeedSetting.getLongPressPresets() : SpeedSetting.getPresets();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) labels[i] = SpeedSetting.format(values[i]);
        float current = longPress ? SpeedSetting.getLongPress() : SpeedSetting.getPlayback();
        DialogThemeHelper.builder(this).setTitle(longPress ? R.string.speed_setting_long_press : R.string.speed_setting_current).setSingleChoiceItems(labels, SpeedSetting.indexOf(values, current), (dialog, which) -> {
            if (longPress) {
                SpeedSetting.putLongPress(values[which]);
                mBinding.longPressText.setText(labels[which]);
            } else {
                SpeedSetting.putPlayback(values[which]);
                mBinding.playSpeedText.setText(labels[which]);
            }
            dialog.dismiss();
        }).show();
    }

    private void setFlag(View view) {
        DialogThemeHelper.builder(this).setTitle(R.string.player_flag).setSingleChoiceItems(flag, Setting.getFlag(), (dialog, which) -> {
            mBinding.flagText.setText(flag[which]);
            Setting.putFlag(which);
            dialog.dismiss();
        }).show();
    }

    private void onPreloadSetting(View view) {
        SettingPreloadActivity.start(this);
    }

    private void onDecodeSetting(View view) {
        SettingDecodeActivity.start(this);
    }

    private void onUa(View view) {
        UaDialog.show(this);
    }

    @Override
    public void setUa(String ua) {
        Setting.putUa(ua);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBinding != null) setPlaybackModeText();
    }
}
