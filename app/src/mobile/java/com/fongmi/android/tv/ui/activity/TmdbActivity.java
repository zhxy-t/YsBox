package com.fongmi.android.tv.ui.activity;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ActivityTmdbSettingsBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.github.catvod.utils.Prefers;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class TmdbActivity extends BaseActivity {

    private ActivityTmdbSettingsBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, TmdbActivity.class));
    }

    public static String getSummary(Context context) {
        String key = Prefers.getString("tmdb_api_key");
        String language = Prefers.getString("tmdb_language");
        if (TextUtils.isEmpty(key)) return context.getString(R.string.tmdb_unconfigured);
        return TextUtils.isEmpty(language) ? context.getString(R.string.tmdb_configured) : language;
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityTmdbSettingsBinding.inflate(getLayoutInflater());
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(view -> onBackInvoked());
        mBinding.apiKey.setText(Prefers.getString("tmdb_api_key"));
        String language = Prefers.getString("tmdb_language");
        mBinding.language.setText(TextUtils.isEmpty(language) ? "zh-CN" : language);
    }

    @Override
    protected void initEvent() {
        mBinding.save.setOnClickListener(view -> onSave());
    }

    private void onSave() {
        Prefers.put("tmdb_api_key", mBinding.apiKey.getText().toString().trim());
        Prefers.put("tmdb_language", mBinding.language.getText().toString().trim());
        DialogThemeHelper.builder(this).setMessage(R.string.tmdb_saved).setPositiveButton(R.string.dialog_positive, null).show();
    }
}
