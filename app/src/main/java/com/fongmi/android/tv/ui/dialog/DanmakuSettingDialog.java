package com.fongmi.android.tv.ui.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogDanmakuSettingBinding;
import com.fongmi.android.tv.player.PlayerManager;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Util;

public final class DanmakuSettingDialog {

    private PlayerManager player;

    public static DanmakuSettingDialog create() {
        return new DanmakuSettingDialog();
    }

    /** 文本样式名：与 DanmakuConfig 的 STYLE_* 常量一一对应，用已有字符串资源避免越界 */
    public static String nameOf(int style) {
        switch (style) {
            case androidx.media3.ui.danmaku.DanmakuConfig.STYLE_NONE:
                return ResUtil.getString(R.string.danmaku_style_none);
            case androidx.media3.ui.danmaku.DanmakuConfig.STYLE_SHADOW:
                return ResUtil.getString(R.string.danmaku_style_shadow);
            case androidx.media3.ui.danmaku.DanmakuConfig.STYLE_PROJECTION:
                return ResUtil.getString(R.string.danmaku_style_projection);
            case androidx.media3.ui.danmaku.DanmakuConfig.STYLE_STROKE:
            default:
                return ResUtil.getString(R.string.danmaku_style_stroke);
        }
    }

    private static DialogDanmakuSettingBinding inflate(LayoutInflater inflater, ViewGroup container) {
        return DialogDanmakuSettingBinding.inflate(inflater, container, false);
    }

    public DanmakuSettingDialog player(PlayerManager player) {
        this.player = player;
        return this;
    }

    public void show(FragmentActivity activity) {
        FragmentManager manager = activity.getSupportFragmentManager();
        for (Fragment fragment : manager.getFragments()) if (fragment instanceof BottomSheet || fragment instanceof SideSheet) return;
        if (Util.isFullscreenLand(activity) || Util.isLeanback()) new SideSheet(player).show(manager, null);
        else new BottomSheet(player).show(manager, null);
    }

    public static final class BottomSheet extends BaseBottomSheetDialog {

        private final PlayerManager player;
        private DialogDanmakuSettingBinding binding;
        private DanmakuSettingPanel panel;

        BottomSheet(PlayerManager player) {
            this.player = player;
        }

        @Override
        protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
            return binding = DanmakuSettingDialog.inflate(inflater, container);
        }

        @Override
        protected int getMaxHeight() {
            return ResUtil.getScreenHeight() / 2;
        }

        @Override
        protected void initView() {
            panel = new DanmakuSettingPanel(binding, player);
            panel.bind();
        }

        @Override
        public void onDestroyView() {
            if (panel != null) panel.release();
            panel = null;
            binding = null;
            super.onDestroyView();
        }
    }

    public static final class SideSheet extends BaseSideSheetDialog {

        private final PlayerManager player;
        private DialogDanmakuSettingBinding binding;
        private DanmakuSettingPanel panel;

        SideSheet(PlayerManager player) {
            this.player = player;
        }

        @Override
        protected int getWidth() {
            return Math.min(ResUtil.dp2px(420), ResUtil.getScreenWidth() / 2);
        }

        @Override
        protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
            return binding = DanmakuSettingDialog.inflate(inflater, container);
        }

        @Override
        protected void initView() {
            panel = new DanmakuSettingPanel(binding, player);
            panel.bind();
        }

        @Override
        public void onDestroyView() {
            if (panel != null) panel.release();
            panel = null;
            binding = null;
            super.onDestroyView();
        }
    }
}
