package com.fongmi.android.tv.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.AnimRes;
import androidx.annotation.ArrayRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.fongmi.android.tv.App;

public class ResUtil {

    public static DisplayMetrics getDisplayMetrics() {
        return getDisplayMetrics(App.get());
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    public static int getScreenWidth() {
        return getScreenWidth(App.get());
    }

    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return getDisplayMetrics(context).widthPixels;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect rect = windowManager.getCurrentWindowMetrics().getBounds();
            return isLand(context) ? Math.max(rect.width(), rect.height()) : Math.min(rect.width(), rect.height());
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
    }

    public static int getScreenHeight() {
        return getScreenHeight(App.get());
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return getDisplayMetrics(context).heightPixels;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect rect = windowManager.getCurrentWindowMetrics().getBounds();
            return isLand(context) ? Math.min(rect.width(), rect.height()) : Math.max(rect.width(), rect.height());
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        }
    }

    public static boolean isEdge(Context context, MotionEvent e, int edge) {
        return e.getRawX() < edge || e.getRawX() > getScreenWidth(context) - edge || e.getRawY() < edge || e.getRawY() > getScreenHeight(context) - edge;
    }

    public static boolean isLand(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean isPad() {
        return isPad(App.get());
    }

    public static boolean isPad(Context context) {
        return context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    /**
     * TV（LEANBACK）模式下把 Application 原生密度折算到 1080p 短边基准，
     * 与 Activity XML 侧 UiModeWrapper 的 createConfigurationContext 包装保持同一密度体系：
     * XML dp 与 Java ResUtil.dp2px/sp2px 得到一致的屏占比（短边px/1080）。
     * 1080p/4K 电视 target==原生密度，零变化；手机 TV 模式 2.75 -> 1.15。
     */
    private static DisplayMetrics tvScaled() {
        DisplayMetrics dm = getDisplayMetrics();
        if (!com.fongmi.android.tv.ui.UiMode.isLeanback()) return dm;
        float shortSide = Math.min(dm.widthPixels, dm.heightPixels);
        float target = Math.max(0.75f, shortSide / 1080f);
        if (Math.abs(target - dm.density) <= 0.01f) return dm;
        DisplayMetrics copy = new DisplayMetrics();
        copy.setTo(dm);
        copy.density = target;
        copy.densityDpi = Math.round(target * 160f);
        copy.scaledDensity = target * (dm.scaledDensity / dm.density);
        return copy;
    }

    public static int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, tvScaled());
    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, tvScaled());
    }

    public static String getString(@StringRes int resId) {
        return App.get().getResources().getString(resId);
    }

    public static String getString(@StringRes int resId, Object... formatArgs) {
        return App.get().getResources().getString(resId, formatArgs);
    }

    public static String[] getStringArray(@ArrayRes int resId) {
        return App.get().getResources().getStringArray(resId);
    }

    public static TypedArray getTypedArray(@ArrayRes int resId) {
        return App.get().getResources().obtainTypedArray(resId);
    }

    public static Drawable getDrawable(@DrawableRes int resId) {
        return ContextCompat.getDrawable(App.get(), resId);
    }

    public static int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(App.get(), resId);
    }

    public static Animation getAnim(@AnimRes int resId) {
        return AnimationUtils.loadAnimation(App.get(), resId);
    }

    public static Display getDisplay(Context context) {
        return ContextCompat.getDisplayOrDefault(context);
    }

    public static int getTextWidth(String content, int size) {
        Paint paint = new Paint();
        paint.setTextSize(sp2px(size));
        return (int) paint.measureText(content);
    }
}
