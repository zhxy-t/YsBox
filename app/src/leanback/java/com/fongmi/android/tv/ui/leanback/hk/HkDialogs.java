package com.fongmi.android.tv.ui.leanback.hk;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

/**
 * TV(leanback) 变体的空实现：leanback 不构建海阔小程序能力。
 *
 * <p>共享源集 {@code api/hk}（HkAuthDialog / HkErrorUtil）在编译期需要该符号，
 * 这里沿用 {@link AlertDialog.Builder} 的标准行为，不套海阔的深色毛玻璃主题——
 * TV 上不会走到 hk 弹窗路径。</p>
 */
public class HkDialogs extends AlertDialog.Builder {

    public HkDialogs(@NonNull Context context) {
        super(context);
    }

    public static boolean isHostGone(Context context) {
        Context ctx = context;
        while (ctx instanceof ContextWrapper) {
            if (ctx instanceof Activity) {
                Activity activity = (Activity) ctx;
                return activity.isFinishing() || activity.isDestroyed();
            }
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        return false;
    }

    public static void clearWindowBackground(@Nullable AlertDialog dialog) {
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(null);
        }
    }
}
