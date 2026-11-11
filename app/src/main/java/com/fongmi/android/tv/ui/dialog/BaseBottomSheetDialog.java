package com.fongmi.android.tv.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.activity.ComponentDialog;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public abstract class BaseBottomSheetDialog extends BottomSheetDialogFragment {

    private final OnBackPressedCallback backCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            onBackInvoked();
        }
    };

    protected abstract ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> setSheet(dialog));
        Window window = dialog.getWindow();
        if (window == null) return dialog;
        // 弹窗主题的 android:windowBackground 是「卡片」drawable，而 BottomSheet 窗口是全屏的，
        // 直接沿用会把卡片铺满整个屏幕（描边贴屏幕边、上方留一大块空白）。改为透明窗口，
        // 卡片背景在 setBehavior() 里画到实际的 sheet 上，让外观只包住内容。
        window.setBackgroundDrawableResource(android.R.color.transparent);
        if (stableOverlay()) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND | WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setDimAmount(0f);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
            WindowCompat.setDecorFitsSystemWindows(window, true);
        } else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            if (Util.isFullscreen(getActivity())) window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getBinding(inflater, container).getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupBackDispatcher();
        initView();
        initEvent();
    }

    protected void initView() {
    }

    protected void initEvent() {
    }

    protected void onBackInvoked() {
    }

    protected final void setBackCallbackEnabled(boolean enabled) {
        backCallback.setEnabled(enabled);
    }

    protected int getMaxHeight() {
        return 0;
    }

    protected boolean transparent() {
        return false;
    }

    protected boolean stableOverlay() {
        return false;
    }

    private void setSheet(BottomSheetDialog dialog) {
        if (!isAdded() || getContext() == null) return;
        setBehavior(dialog);
    }

    @Override
    public int getTheme() {
        // BottomSheetDialogFragment 的主题必须携带 bottomSheetStyle（白底/圆角），
        // MaterialAlertDialog 系 overlay 缺少该属性，会导致 sheet 无背景（弹窗透明）。
        return com.fongmi.android.tv.ui.dialog.DialogThemeHelper.getSheetOverlay();
    }

    protected void setBehavior(BottomSheetDialog dialog) {
        FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) return;
        setHeight(sheet, getMaxHeight());
        applySheetBehavior(sheet);
        if (transparent()) {
            int color = ResUtil.getColor(R.color.transparent);
            sheet.setBackgroundColor(color);
            if (sheet.getParent() instanceof View) ((View) sheet.getParent()).setBackgroundColor(color);
            Window window = dialog.getWindow();
            if (window != null) window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        else {
            // 按当前弹窗风格渲染 sheet 表面：解析主题的 dialogSurface（9 种风格各自的
            // 背景 drawable，渐变风格即渐变描边卡），与裸弹窗（切换线路等）外观完全同源。
            // transparent() 子类（QuickSearch 等）自绘表面，不在此覆盖。
            try {
                TypedValue tv = new TypedValue();
                if (requireContext().getTheme().resolveAttribute(R.attr.dialogSurface, tv, true)) {
                    android.graphics.drawable.Drawable surface = androidx.core.content.ContextCompat.getDrawable(requireContext(), tv.resourceId != 0 ? tv.resourceId : tv.data);
                    if (surface != null) sheet.setBackground(surface);
                }
            } catch (Exception e) {
                // 兜底：主题缺 attr 时保持 getTheme() 的 bottomSheetStyle 底色，不出透明弹窗
            }
        }
    }

    private void setHeight(FrameLayout sheet, int maxHeight) {
        if (maxHeight <= 0) return;
        ViewGroup.LayoutParams params = sheet.getLayoutParams();
        params.height = maxHeight;
        sheet.setLayoutParams(params);
    }

    private void applySheetBehavior(FrameLayout sheet) {
        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(sheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
    }

    private void setupBackDispatcher() {
        if (requireDialog() instanceof ComponentDialog dialog) dialog.getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backCallback);
    }

    @Override
    public void onDestroyView() {
        backCallback.setEnabled(false);
        super.onDestroyView();
    }
}
