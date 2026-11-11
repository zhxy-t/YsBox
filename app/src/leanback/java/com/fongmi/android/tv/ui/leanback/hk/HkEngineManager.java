package com.fongmi.android.tv.ui.leanback.hk;

import com.fongmi.android.tv.api.hk.HkJsRuntime;
import com.fongmi.android.tv.api.hk.HkRule;

/**
 * TV(leanback) 变体的空实现：leanback 不构建海阔小程序能力。
 *
 * <p>共享源集 {@code api/hk} 在编译期需要这个接缝，故在此打桩；运行时拿不到常驻运行时
 * （{@link #runtimeFor} 恒返回 null），调用方（{@link com.fongmi.android.tv.api.hk.HkRouter}）
 * 会以明确的「仅支持手机版」异常退出，不会走到 hk 求值路径。</p>
 */
public final class HkEngineManager {

    private HkEngineManager() {
    }

    public static String ruleKey(HkRule rule) {
        return rule == null ? "" : String.valueOf(rule.getTitle());
    }

    public static HkJsRuntime runtimeFor(HkRule rule) {
        return null;
    }

    public static HkJsRuntime runtimeIfPresent(HkRule rule) {
        return null;
    }

    public static void ensureInit(HkJsRuntime rt) throws Exception {
    }

    public static HkRule currentRule() {
        return null;
    }

    public static void setCurrentRule(HkRule rule) {
    }
}
