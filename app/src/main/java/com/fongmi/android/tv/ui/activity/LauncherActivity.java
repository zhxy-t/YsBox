package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.fongmi.android.tv.ui.UiMode;

/**
 * 统一 UI 的唯一启动入口。
 *
 * <p>mobile 与 leanback 两套界面已合并进同一个 APK，由 {@link UiMode} 决定
 * 启动时进入哪一套首页。用户在设置里切换模式后重启进程即可生效。
 *
 * <p>使用反射而非直接引用，避免 main 源集对 flavor 源集类产生编译期耦合。
 */
public final class LauncherActivity extends Activity {

    private static final String MOBILE_HOME = "com.fongmi.android.tv.ui.activity.HomeActivity";
    private static final String LEANBACK_HOME = "com.fongmi.android.tv.ui.leanback.activity.HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String target = UiMode.isLeanback() ? LEANBACK_HOME : MOBILE_HOME;
        try {
            Class.forName(target);
        } catch (ClassNotFoundException ignored) {
            // 兜底：任一套首页缺失时退回另一套，保证 App 永远能启动
            target = MOBILE_HOME.equals(target) ? LEANBACK_HOME : MOBILE_HOME;
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(getPackageName(), target));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
