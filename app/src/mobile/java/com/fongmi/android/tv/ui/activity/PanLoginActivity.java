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
import com.fongmi.android.tv.databinding.ActivityPanLoginBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Prefers;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PanLoginActivity extends BaseActivity {

    private ActivityPanLoginBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, PanLoginActivity.class));
    }

    public static String getSummary(Context context) {
        String url = Prefers.getString("pan_url");
        String user = Prefers.getString("pan_user");
        return TextUtils.isEmpty(url) ? context.getString(R.string.pan_login_none) : (TextUtils.isEmpty(user) ? url : user);
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityPanLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(view -> onBackInvoked());
        mBinding.url.setText(Prefers.getString("pan_url"));
        mBinding.user.setText(Prefers.getString("pan_user"));
        mBinding.pass.setText(Prefers.getString("pan_pass"));
        mBinding.status.setText(getSummary(this));
    }

    @Override
    protected void initEvent() {
        mBinding.save.setOnClickListener(view -> onSave());
        mBinding.test.setOnClickListener(view -> onTest());
        mBinding.logout.setOnClickListener(view -> onLogout());
    }

    private void onSave() {
        Prefers.put("pan_url", mBinding.url.getText().toString().trim());
        Prefers.put("pan_user", mBinding.user.getText().toString().trim());
        Prefers.put("pan_pass", mBinding.pass.getText().toString().trim());
        mBinding.status.setText(getSummary(this));
        toast(R.string.pan_login_saved);
    }

    private void onTest() {
        String url = mBinding.url.getText().toString().trim();
        String user = mBinding.user.getText().toString().trim();
        String pass = mBinding.pass.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            toast(R.string.pan_login_url_empty);
            return;
        }
        mBinding.status.setText(R.string.pan_login_testing);
        new Thread(() -> {
            String result;
            try (Response response = OkHttp.client().newCall(new Request.Builder().url(url).header("Authorization", Credentials.basic(user, pass)).head().build()).execute();
                 ResponseBody body = response.body()) {
                result = String.valueOf(response.code());
                result = (response.isSuccessful() || response.code() == 207) ? getString(R.string.pan_login_ok, result) : getString(R.string.pan_login_fail, result);
            } catch (Exception e) {
                result = getString(R.string.pan_login_fail, e.getMessage() == null ? "error" : e.getMessage());
            }
            String text = result;
            runOnUiThread(() -> {
                mBinding.status.setText(text);
                DialogThemeHelper.builder(this).setMessage(text).setPositiveButton(R.string.dialog_positive, null).show();
            });
        }).start();
    }

    private void onLogout() {
        Prefers.put("pan_url", "");
        Prefers.put("pan_user", "");
        Prefers.put("pan_pass", "");
        mBinding.url.setText("");
        mBinding.user.setText("");
        mBinding.pass.setText("");
        mBinding.status.setText(getSummary(this));
        toast(R.string.pan_login_logout_done);
    }

    private void toast(int res) {
        DialogThemeHelper.builder(this).setMessage(res).setPositiveButton(R.string.dialog_positive, null).show();
    }
}
