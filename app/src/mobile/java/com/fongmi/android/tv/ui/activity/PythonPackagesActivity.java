package com.fongmi.android.tv.ui.activity;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.loader.BaseLoader;
import com.fongmi.android.tv.databinding.ActivityPythonPackagesBinding;
import com.fongmi.android.tv.databinding.ItemPythonPackageBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.python.PythonPackage;
import com.fongmi.android.tv.utils.python.PythonPackageManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PythonPackagesActivity extends BaseActivity {

    private ActivityPythonPackagesBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, PythonPackagesActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityPythonPackagesBinding.inflate(getLayoutInflater());
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
        mBinding.install.setOnClickListener(view -> onInstall());
        mBinding.reload.setOnClickListener(view -> onReload());
    }

    private void onInstall() {
        String spec = mBinding.input.getText() == null ? "" : mBinding.input.getText().toString().trim();
        if (TextUtils.isEmpty(spec)) {
            Notify.show(R.string.python_pkg_name_empty);
            return;
        }
        mBinding.install.setEnabled(false);
        mBinding.status.setVisibility(View.VISIBLE);
        mBinding.status.setText(getString(R.string.python_pkg_installing, spec));
        new Thread(() -> {
            String result;
            boolean ok;
            try {
                PythonPackageManager.install(spec, message -> runOnUiThread(() -> {
                    if (!isFinishing()) mBinding.status.setText(message);
                }));
                result = PythonPackageManager.applyRuntime() ? getString(R.string.python_pkg_success, spec) : getString(R.string.python_pkg_pending);
                ok = true;
            } catch (Exception e) {
                String message = e.getMessage();
                result = getString(R.string.python_pkg_failed, TextUtils.isEmpty(message) ? e.getClass().getSimpleName() : message);
                ok = false;
            }
            String text = result;
            boolean success = ok;
            runOnUiThread(() -> {
                if (isFinishing()) return;
                mBinding.install.setEnabled(true);
                mBinding.status.setText(text);
                if (success) Notify.show(text);
                refresh();
            });
        }).start();
    }

    private void onReload() {
        BaseLoader.get().clear();
        boolean started = PythonPackageManager.applyRuntime();
        Notify.show(started ? R.string.python_pkg_reloaded : R.string.python_pkg_pending);
    }

    private void refresh() {
        mBinding.list.removeAllViews();
        java.util.List<PythonPackage> items = PythonPackageManager.getInstalled();
        mBinding.empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        for (PythonPackage item : items) {
            ItemPythonPackageBinding binding = ItemPythonPackageBinding.inflate(LayoutInflater.from(this), mBinding.list, false);
            binding.name.setText(item.getName());
            String version = TextUtils.isEmpty(item.getVersion()) ? item.getDir().getName() : item.getVersion() + " · " + item.getDir().getName();
            binding.version.setText(version);
            binding.remove.setOnClickListener(view -> onRemove(item));
            mBinding.list.addView(binding.getRoot());
        }
    }

    private void onRemove(PythonPackage item) {
        DialogThemeHelper.builder(this).setMessage(getString(R.string.python_pkg_remove_confirm, item.getName())).setNegativeButton(R.string.dialog_negative, null).setPositiveButton(R.string.dialog_positive, (dialog, which) -> {
            try {
                PythonPackageManager.uninstall(item);
                BaseLoader.get().clear();
                Notify.show(getString(R.string.python_pkg_removed, item.getName()));
                refresh();
            } catch (Exception e) {
                Notify.show(getString(R.string.python_pkg_failed, e.getMessage()));
            }
        }).show();
    }
}
