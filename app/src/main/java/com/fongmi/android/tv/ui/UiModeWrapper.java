package com.fongmi.android.tv.ui;

import android.content.Context;

/**
 * TV 模式运行时密度包装：
 * 仅当 UiMode 为 LEANBACK（TV 模式）时，把 Context 密度重定为「屏幕短边px / 1080」——
 * 与 1080p 电视屏占比完全一致；手机模式（MOBILE）不包装，保持手机原生观感。
 * 供 leanback 与 mobile 两侧 BaseActivity 共用（mobile 增强功能等页面在 TV 模式下打开时同样获得电视观感）。
 */
public final class UiModeWrapper {

    public static Context wrap(Context base) {
        try {
            if (!UiMode.isLeanback()) return base;
            android.util.DisplayMetrics sys = base.getResources().getDisplayMetrics();
            float shortSide = Math.min(sys.widthPixels, sys.heightPixels);
            float density = Math.max(0.75f, shortSide / 1080f);
            if (Math.abs(density - sys.density) <= 0.01f) return base;
            android.content.res.Configuration config = new android.content.res.Configuration(base.getResources().getConfiguration());
            config.densityDpi = Math.round(density * 160f);
            return base.createConfigurationContext(config);
        } catch (Exception e) {
            return base;
        }
    }
}
