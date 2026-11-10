package com.fongmi.android.tv.ui.leanback.base;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.custom.CustomWallView;
import com.fongmi.android.tv.utils.Util;
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
        enableDynamicColor();
        super.onCreate(savedInstanceState);
        setContentView(getBinding().getRoot());
        EventBus.getDefault().register(this);
        initView(savedInstanceState);
        Util.hideSystemUI(this);
        setBackCallback();
        initEvent();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        if (!customWall()) return;
        ((ViewGroup) findViewById(android.R.id.content)).addView(new CustomWallView(this, null), 0, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    /** 按用户选择的弹窗风格应用主题（电视端与手机端统一） */
    protected void applyDialogTheme() {
        switch (com.fongmi.android.tv.setting.Setting.getDialogStyle()) {
            case 0: setTheme(R.style.Theme_App_DialogClassic); break;
            case 1: setTheme(R.style.Theme_App_DialogWhite); break;
            case 2: setTheme(R.style.Theme_App_DialogAmoled); break;
            case 4: setTheme(R.style.Theme_App_DialogNeon); break;
            case 5: setTheme(R.style.Theme_App_DialogOriginal); break;
            case 6: setTheme(R.style.Theme_App_DialogFrost); break;
            case 7: setTheme(R.style.Theme_App_DialogViolet); break;
            case 8: setTheme(R.style.Theme_App_DialogOriginalDark); break;
            case 3:
            default: setTheme(R.style.Theme_App_DialogIOS); break;
        }
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

    protected void notifyItemChanged(RecyclerView view, RecyclerView.Adapter<?> adapter) {
        view.post(() -> adapter.notifyDataSetChanged());
    }

    private void enableDynamicColor() {
        int color = Setting.getDynamicColor();
        if (color != 0) DynamicColors.applyToActivityIfAvailable(this, new DynamicColorsOptions.Builder().setContentBasedSource(color).build());
    }

    private void setBackCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackInvoked();
            }
        });
    }

    /** TV 页面统一按「屏占比」渲染：
     *  TV 布局的 dp/sp 是按 1080p 电视（短边 1080px、density=1）设计的，
     *  在手机高密度屏（density≈2.75）上同样 24sp 标题的屏占比会被放大约 2.8 倍，观感巨大。
     *  这里把密度重定为 短边px/1080 —— 同一元素在所有设备上的屏占比与 1080p 电视完全一致：
     *    1080p 电视 -> 1.0（不变）；4K 电视 -> 2.0（不变，原生即此值）；
     *    手机横持（短边约 1240px）-> 1.15（对比原生 2.75，整页缩至电视观感）。 */
    private Resources hackResources(Resources resources) {
        try {
            android.util.DisplayMetrics dm = resources.getDisplayMetrics();
            float shortSide = Math.min(dm.widthPixels, dm.heightPixels);
            float density = Math.max(0.75f, shortSide / 1080f);
            if (Math.abs(density - dm.density) > 0.01f) {
                dm.density = density;
                dm.scaledDensity = density;
                dm.densityDpi = (int) (density * 160);
            }
            return resources;
        } catch (Exception e) {
            return resources;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscribe(Object o) {
    }

    private float appliedFontScale = -1f;

    @Override
    public Resources getResources() {
        Resources res = hackResources(super.getResources());
        float scale = Setting.getFontScale();
        if (scale <= 0) scale = Resources.getSystem().getConfiguration().fontScale;
        // getResources() 调用极频繁，updateConfiguration 会重建资源缓存：只在缩放变化时执行一次
        if (scale != appliedFontScale) {
            appliedFontScale = scale;
            Configuration config = new Configuration(res.getConfiguration());
            if (config.fontScale != scale) {
                config.fontScale = scale;
                res.updateConfiguration(config, res.getDisplayMetrics());
            }
        }
        return res;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Util.hideSystemUI(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) Util.hideSystemUI(this);
    }

    protected void onBackInvoked() {
        finish();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
