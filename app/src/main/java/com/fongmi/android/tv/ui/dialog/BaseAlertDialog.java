package com.fongmi.android.tv.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public abstract class BaseAlertDialog extends DialogFragment {

    protected abstract ViewBinding getBinding();

    protected abstract MaterialAlertDialogBuilder getBuilder();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = getBuilder().create();
        initView();
        initEvent();
        return dialog;
    }

    protected MaterialAlertDialogBuilder builder() {
        // 用带弹窗风格主题的 Context 建 builder：每个 Theme.App.Dialog* 已内联
        // 正确的 materialAlertDialogTheme，指向当前风格的真实 overlay（背景/圆角/语义色/按钮样式），
        // 这样所有 BaseAlertDialog 子类的 Material 弹窗都随弹窗风格统一变化。
        return new MaterialAlertDialogBuilder(DialogThemeHelper.wrap(requireActivity()));
    }

    protected void initView() {
    }

    protected void initEvent() {
    }

    protected void setWidth(float factor) {
        if (getDialog() == null || getDialog().getWindow() == null) return;
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = (int) (ResUtil.getScreenWidth() * factor);
        getDialog().getWindow().setAttributes(params);
    }
}
