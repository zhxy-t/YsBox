package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.databinding.ActivityPanDriveLoginBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;

public class PanDriveLoginActivity extends BaseActivity {

    private ActivityPanDriveLoginBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, PanDriveLoginActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityPanDriveLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(view -> onBackInvoked());
    }
}
