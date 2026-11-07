package com.fongmi.android.tv.ui.activity;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivityDownloadBinding;
import com.fongmi.android.tv.setting.DownloadSetting;
import com.fongmi.android.tv.ui.adapter.DownloadTaskAdapter;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.utils.download.DownloadManager;
import com.fongmi.android.tv.utils.download.DownloadTask;
import com.fongmi.android.tv.utils.Notify;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DownloadActivity extends BaseActivity implements DownloadTaskAdapter.Listener, DownloadManager.Listener {

    private ActivityDownloadBinding mBinding;
    private DownloadTaskAdapter mAdapter;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, DownloadActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityDownloadBinding.inflate(getLayoutInflater());
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(view -> onBackInvoked());
        mBinding.recycler.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recycler.setAdapter(mAdapter = new DownloadTaskAdapter(this));
        mBinding.clear.setOnClickListener(view -> onClear());
        mBinding.threads.setOnClickListener(this::setThreads);
        mBinding.dir.setOnClickListener(this::editDir);
        mBinding.threadsText.setText(String.valueOf(DownloadSetting.getThreads()));
        mBinding.dirText.setText(DownloadSetting.getDir().getAbsolutePath());
        DownloadManager.get().addListener(this);
        refresh();
    }

    private void setThreads(View view) {
        String[] values = new String[DownloadSetting.MAX_THREADS];
        for (int i = 0; i < values.length; i++) values[i] = String.valueOf(i + 1);
        DialogThemeHelper.builder(this).setTitle(R.string.download_threads).setNegativeButton(R.string.dialog_negative, null).setSingleChoiceItems(values, DownloadSetting.getThreads() - 1, (dialog, which) -> {
            DownloadSetting.putThreads(which + 1);
            mBinding.threadsText.setText(String.valueOf(DownloadSetting.getThreads()));
            dialog.dismiss();
        }).show();
    }

    private void editDir(View view) {
        EditText edit = new EditText(this);
        edit.setText(DownloadSetting.getDirPath());
        edit.setHint(R.string.download_dir_hint);
        edit.setSingleLine(true);
        edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(this);
        container.setPadding(padding, padding / 2, padding, 0);
        container.addView(edit, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        DialogThemeHelper.builder(this)
                .setTitle(R.string.download_dir_edit)
                .setView(container)
                .setNegativeButton(R.string.dialog_negative, null)
                .setPositiveButton(R.string.dialog_positive, (dialog, which) -> {
                    String path = edit.getText().toString().trim();
                    DownloadSetting.putDirPath(path);
                    String actual = DownloadSetting.getDir().getAbsolutePath();
                    mBinding.dirText.setText(actual);
                    Notify.show(path.isEmpty() || actual.equals(path) ? R.string.download_dir_saved : R.string.download_dir_invalid);
                })
                .show();
    }

    private void onClear() {
        DialogThemeHelper.builder(this).setTitle(R.string.download_clear).setMessage(R.string.download_clear_confirm).setNegativeButton(R.string.dialog_negative, null).setPositiveButton(R.string.dialog_positive, (dialog, which) -> DownloadManager.get().clearFinished()).show();
    }

    private void refresh() {
        mAdapter.setItems(DownloadManager.get().getTasks());
        boolean empty = mAdapter.getItemCount() == 0;
        mBinding.empty.setVisibility(empty ? View.VISIBLE : View.GONE);
        mBinding.count.setText(getString(R.string.download_count, mAdapter.getItemCount()));
    }

    @Override
    public void onChanged() {
        refresh();
    }

    @Override
    protected void onDestroy() {
        DownloadManager.get().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onCancel(DownloadTask task) {
        DownloadManager.get().cancel(task.getId());
    }

    @Override
    public void onRetry(DownloadTask task) {
        DownloadManager.get().retry(task.getId());
    }

    @Override
    public void onDelete(DownloadTask task) {
        DownloadManager.get().remove(task.getId());
    }
}
