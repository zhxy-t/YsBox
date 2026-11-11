package com.fongmi.android.tv.emby;

import android.content.Context;
import android.text.TextUtils;

import java.net.URI;
import java.util.Locale;

/**
 * trae 版没有 Emby 服务栈，这里保留宿主接口形状：URL host 解析照旧，
 * 节点归属/代理一律不命中，MPV 走直连。
 */
public final class EmbyProxyGate {

    private EmbyProxyGate() {
    }

    public static void invalidate() {
    }

    public static String hostOfUrl(String url) {
        try {
            if (TextUtils.isEmpty(url)) return "";
            String host = new URI(url.trim()).getHost();
            return host == null ? "" : host.toLowerCase(Locale.ROOT);
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static String serverIdForHost(String host) {
        return null;
    }

    public static String claim(String host, Context app) {
        return null;
    }

    public static String proxyUrlForHost(String host, Context app) {
        return null;
    }
}
