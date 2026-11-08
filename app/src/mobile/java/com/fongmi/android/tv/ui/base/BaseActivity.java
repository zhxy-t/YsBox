package com.fongmi.android.tv.ui.base;
import android.content.res.Configuration;
import android.content.res.Resources;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.custom.CustomWallView;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract ViewBinding getBinding();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Setting.wrapLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyDialogTheme();
        enableEdgeToEdge();
        enableDynamicColor();
        super.onCreate(savedInstanceState);
        setContentView(getBinding().getRoot());
        EventBus.getDefault().register(this);
        initView(savedInstanceState);
        setBackCallback();
        initEvent();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        if (!customWall()) return;
        ((ViewGroup) findViewById(android.R.id.content)).addView(new CustomWallView(this, null), 0, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    protected FragmentActivity getActivity() {
        return this;
    }

    protected boolean customWall() {
        return true;
    }

    protected void initView(Bundle savedInstanceState) {
    }

    protected void initEvent() {
    }

    protected boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    protected boolean isGone(View view) {
        return view.getVisibility() == View.GONE;
    }

    protected void setPadding(ViewGroup layout) {
        setPadding(layout, false);
    }

    protected void setPadding(ViewGroup layout, boolean leftOnly) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;
        DisplayCutout cutout = ResUtil.getDisplay(this).getCutout();
        if (cutout == null) return;
        int top = cutout.getSafeInsetTop();
        int left = cutout.getSafeInsetLeft();
        int right = cutout.getSafeInsetRight();
        int bottom = cutout.getSafeInsetBottom();
        int padding = left | right | top | bottom;
        layout.setPadding(padding, 0, leftOnly ? 0 : padding, 0);
    }

    protected void noPadding(ViewGroup layout) {
        layout.setPadding(0, 0, 0, 0);
    }

    private void setBackCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackInvoked();
            }
        });
    }

    private void enableEdgeToEdge() {
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT), SystemBarStyle.dark(Color.TRANSPARENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setStatusBarContrastEnforced(false);
            getWindow().setNavigationBarContrastEnforced(false);
        }
    }

    private void enableDynamicColor() {
        int color = Setting.getDynamicColor();
        if (color != 0) DynamicColors.applyToActivityIfAvailable(this, new DynamicColorsOptions.Builder().setContentBasedSource(color).build());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscribe(RefreshEvent event) {
        if (event.getType() == RefreshEvent.Type.THEME) {
            recreate();
        }
    }

    protected void onBackInvoked() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            com.fongmi.android.tv.ai.AiFloatManager.get().onResume(this);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onPause() {
        try {
            com.fongmi.android.tv.ai.AiFloatManager.get().onPause();
        } catch (Exception ignored) {
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /** 按用户选择的弹窗风格应用主题（默认 iOS），所有弹窗含软件弹窗统一跟随。 */
    protected void applyDialogTheme() {
        switch (com.fongmi.android.tv.setting.Setting.getDialogStyle()) {
            case 1: setTheme(R.style.Theme_App_DialogWhite); break;
            case 2: setTheme(R.style.Theme_App_DialogAmoled); break;
            case 4: setTheme(R.style.Theme_App_DialogNeon); break;
            case 5: setTheme(R.style.Theme_App); break;
            case 0: setTheme(R.style.Theme_App_DialogClassic); break;
            case 3:
            default: setTheme(R.style.Theme_App_DialogIOS); break;
        }
    }

    /** 应用全局字体缩放：影响本 Activity 及所有子 View 的 sp 字号（-1 = 跟随系统） */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        float scale = com.fongmi.android.tv.setting.Setting.getFontScale();
        if (scale <= 0) scale = Resources.getSystem().getConfiguration().fontScale;
        Configuration config = new Configuration(res.getConfiguration());
        config.fontScale = scale;
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
}
