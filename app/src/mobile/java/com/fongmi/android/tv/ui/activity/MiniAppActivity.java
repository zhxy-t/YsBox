package com.fongmi.android.tv.ui.activity;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivityMiniAppBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.github.catvod.utils.Prefers;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MiniAppActivity extends BaseActivity {

    private ActivityMiniAppBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, MiniAppActivity.class));
    }

    public static boolean isEnabled(Context context) {
        // 海阔小程序默认开启（可在增强功能里关掉）
        return Prefers.getBoolean("mini_app_enabled", true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        Prefers.put("mini_app_enabled", enabled);
    }

    public static String getUrl() {
        return Prefers.getString("mini_app_url");
    }

    public static void putUrl(String url) {
        Prefers.put("mini_app_url", url);
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityMiniAppBinding.inflate(getLayoutInflater());
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initView(Bundle savedInstanceState) {
        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(view -> onBackInvoked());
        WebSettings settings = mBinding.web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        mBinding.web.setWebChromeClient(new WebChromeClient());
        mBinding.web.setWebViewClient(new WebViewClient());
        String url = getUrl();
        if (TextUtils.isEmpty(url)) {
            showUrlDialog();
        } else {
            mBinding.web.loadUrl(url);
        }
    }

    private void showUrlDialog() {
        EditText edit = new EditText(this);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        edit.setPadding(padding, padding, padding, padding);
        edit.setHint(R.string.mini_app_url_hint);
        edit.setText(getUrl());
        edit.setSelection(edit.getText().length());
        DialogThemeHelper.builder(this).setTitle(R.string.setting_mini_app).setView(edit).setNegativeButton(R.string.dialog_negative, (dialog, which) -> finish()).setPositiveButton(R.string.dialog_positive, (dialog, which) -> {
            String url = edit.getText().toString().trim();
            if (TextUtils.isEmpty(url)) {
                finish();
                return;
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://" + url;
            putUrl(url);
            mBinding.web.loadUrl(url);
        }).show();
    }

    @Override
    protected void onBackInvoked() {
        if (mBinding != null && mBinding.web.canGoBack()) {
            mBinding.web.goBack();
        } else {
            super.onBackInvoked();
        }
    }

    @Override
    protected void onDestroy() {
        if (mBinding != null) {
            mBinding.web.stopLoading();
            mBinding.web.loadUrl("about:blank");
            mBinding.web.setWebViewClient(new WebViewClient());
            ViewGroup parent = (ViewGroup) mBinding.web.getParent();
            if (parent != null) parent.removeView(mBinding.web);
            mBinding.web.destroy();
        }
        super.onDestroy();
    }
}
