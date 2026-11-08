package com.fongmi.android.tv.ui.fragment;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.FragmentSettingPlayerBinding;
import com.fongmi.android.tv.impl.BufferListener;
import com.fongmi.android.tv.impl.UaListener;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.setting.SpeedSetting;
import com.fongmi.android.tv.ui.activity.HomeActivity;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.dialog.BufferDialog;
import com.fongmi.android.tv.ui.dialog.PlayerOsdDialog;
import com.fongmi.android.tv.ui.dialog.UaDialog;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingPlayerFragment extends BaseFragment implements UaListener, BufferListener {

    private FragmentSettingPlayerBinding mBinding;
    private String[] background;
    private String[] render;
    private String[] scale;
    private String[] engine;
    private String[] flag;
    private String[] osd;

    public static SettingPlayerFragment newInstance() {
        return new SettingPlayerFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentSettingPlayerBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        setVisible();
        setPlaybackModeText();
        mBinding.flagText.setText((flag = ResUtil.getStringArray(R.array.select_flag))[Setting.getFlag()]);
        mBinding.playSpeedText.setText(SpeedSetting.format(SpeedSetting.getPlayback()));
        mBinding.longPressText.setText(SpeedSetting.format(SpeedSetting.getLongPress()));
        mBinding.bufferText.setText(String.valueOf(PlayerSetting.getBuffer()));
        mBinding.scaleText.setText((scale = ResUtil.getStringArray(R.array.select_scale))[PlayerSetting.getScale()]);
        mBinding.backgroundText.setText((background = ResUtil.getStringArray(R.array.select_background))[PlayerSetting.getBackground()]);
        mBinding.osdText.setText(getOsdText(osd = ResUtil.getStringArray(R.array.select_player_osd)));
    }

    @Override
    protected void initEvent() {
        mBinding.engine.setOnClickListener(this::setEngine);
        mBinding.decode.setOnClickListener(this::onDecode);
        mBinding.flag.setOnClickListener(this::onFlag);
        mBinding.playSpeed.setOnClickListener(this::onPlaybackSpeed);
        mBinding.longPress.setOnClickListener(this::onLongPress);
        mBinding.render.setOnClickListener(this::setRender);
        mBinding.scale.setOnClickListener(this::onScale);
        mBinding.background.setOnClickListener(this::onBackground);
        mBinding.buffer.setOnClickListener(this::onBuffer);
        mBinding.preload.setOnClickListener(this::onPreload);
        mBinding.ua.setOnClickListener(this::onUa);
        mBinding.osd.setOnClickListener(this::onOsd);
    }

    private void setVisible() {
        boolean exo = PlayerSetting.isExo();
        mBinding.buffer.setVisibility(exo ? View.VISIBLE : View.GONE);
    }

    private void setEngine(View view) {
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.player_engine).setSingleChoiceItems(engine, PlayerSetting.getEngine(), (dialog, which) -> {
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
        engine = engineNames();
        render = ResUtil.getStringArray(R.array.select_render);
        mBinding.engineText.setText(engine[Math.min(PlayerSetting.getEngine(), engine.length - 1)]);
        mBinding.renderText.setText(render[PlayerSetting.getRender()]);
    }

    /** 资源数组兜底：老包里 select_engine 可能只有 EXO，这里保证 MPV 一定出现在列表里。 */
    private static String[] engineNames() {
        String[] names = ResUtil.getStringArray(R.array.select_engine);
        return names != null && names.length >= 2 ? names : new String[]{"EXO", "MPV"};
    }

    private void onScale(View view) {
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.player_scale).setSingleChoiceItems(scale, PlayerSetting.getScale(), (dialog, which) -> {
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
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.player_background).setSingleChoiceItems(background, PlayerSetting.getBackground(), (dialog, which) -> {
            mBinding.backgroundText.setText(background[which]);
            PlayerSetting.putBackground(which);
            dialog.dismiss();
        }).show();
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
        DialogThemeHelper.builder(requireActivity()).setTitle(longPress ? R.string.speed_setting_long_press : R.string.speed_setting_current).setSingleChoiceItems(labels, SpeedSetting.indexOf(values, current), (dialog, which) -> {
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

    private void onFlag(View view) {
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.player_flag).setSingleChoiceItems(flag, Setting.getFlag(), (dialog, which) -> {
            mBinding.flagText.setText(flag[which]);
            Setting.putFlag(which);
            dialog.dismiss();
        }).show();
    }

    private void onPreload(View view) {
        ((HomeActivity) requireActivity()).change(4);
    }

    private void onDecode(View view) {
        ((HomeActivity) requireActivity()).change(5);
    }

    private void onUa(View view) {
        UaDialog.show(this);
    }

    private void onOsd(View view) {
        PlayerOsdDialog.show(requireActivity(), () -> mBinding.osdText.setText(getOsdText(osd)));
    }

    private String getOsdText(String[] items) {
        boolean[] checked = PlayerOsdDialog.getChecked();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < checked.length && i < items.length; i++) {
            if (!checked[i]) continue;
            if (builder.length() > 0) builder.append(" / ");
            builder.append(items[i]);
        }
        return builder.length() == 0 ? getString(R.string.setting_off) : builder.toString();
    }

    @Override
    public void setUa(String ua) {
        Setting.putUa(ua);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) initView();
    }
}
