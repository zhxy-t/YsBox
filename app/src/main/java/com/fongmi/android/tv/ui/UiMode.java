package com.fongmi.android.tv.ui;

import com.github.catvod.utils.Prefers;

/**
 * 运行时 UI 模式管理（统一 UI 方案的核心）。
 *
 * <p>背景：mobile / leanback 两套界面原先由 Gradle flavor 在编译期二选一
 * （{@code ProductFlavor "mode"}），因此必须分别打包两个 APK：手机版与电视版。
 * 本类把「使用哪套界面」从编译期常量改为运行时可切换的偏好，
 * 使同一个 APK 内可以同时容纳手机与电视两套界面，由用户自行切换。
 *
 * <p>兼容性保证：当用户未主动设置偏好时（{@link #AUTO}），
 * 行为完全回退到编译期 flavor（AUTO 默认按手机界面运行），
 * 因此既有 APK 的表现与改造前一致，不会影响存量用户。
 *
 * <p>注意：切换模式后需要重启 Activity（或整个进程）才能让资源与布局生效，
 * 因为布局资源的解析发生在 View 创建阶段。
 */
public final class UiMode {

    /** 跟随安装包默认（由编译期 flavor 决定） */
    public static final int AUTO = -1;

    /** 强制使用手机界面（触摸 / 竖屏优先） */
    public static final int MOBILE = 0;

    /** 强制使用电视界面（遥控器 / 横屏优先） */
    public static final int LEANBACK = 1;

    private static final String KEY = "ui_mode";

    /** 进程内缓存，避免高频读取 SharedPreferences */
    private static volatile Integer cache;

    private UiMode() {
    }

    /**
     * 读取当前 UI 模式。
     *
     * @return {@link #AUTO} / {@link #MOBILE} / {@link #LEANBACK}
     */
    public static int get() {
        Integer cached = cache;
        if (cached != null) return cached;
        int mode = Prefers.getInt(KEY, AUTO);
        if (mode != MOBILE && mode != LEANBACK) mode = AUTO;
        cache = mode;
        return mode;
    }

    /**
     * 设置 UI 模式并持久化。
     *
     * @param mode {@link #AUTO} / {@link #MOBILE} / {@link #LEANBACK}
     */
    public static void set(int mode) {
        if (mode != MOBILE && mode != LEANBACK) mode = AUTO;
        cache = mode;
        Prefers.put(KEY, mode);
    }

    /** 清除用户偏好，回到跟随安装包默认 */
    public static void reset() {
        cache = AUTO;
        Prefers.remove(KEY);
    }

    /** 单 APK 默认界面：手机（电视界面由用户在设置里切换） */
    public static boolean isDefaultLeanback() {
        return false;
    }

    /**
     * 当前是否应按电视界面运行。
     * 用户显式设置优先，否则回退到编译期 flavor。
     */
    public static boolean isLeanback() {
        int mode = get();
        if (mode == LEANBACK) return true;
        if (mode == MOBILE) return false;
        return isDefaultLeanback();
    }

    /** 当前是否应按手机界面运行 */
    public static boolean isMobile() {
        return !isLeanback();
    }
}