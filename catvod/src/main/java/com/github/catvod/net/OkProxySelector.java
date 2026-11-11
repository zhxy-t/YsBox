package com.github.catvod.net;

import com.github.catvod.bean.Proxy;
import com.github.catvod.utils.Util;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OkProxySelector extends ProxySelector {

    private final List<Proxy> proxy;
    private final ProxySelector system;
    private volatile java.net.Proxy manual;
    private volatile String manualHost;
    private volatile String manualUserInfo;
    private boolean authSet;

    public OkProxySelector() {
        proxy = new CopyOnWriteArrayList<>();
        system = ProxySelector.getDefault();
        Authenticator.setDefault(new ProxyAuthenticator(this));
    }

    public synchronized void setProxy(String value) {
        manual = null;
        manualHost = null;
        manualUserInfo = null;
        if (value == null || value.trim().isEmpty()) return;
        try {
            URI uri = new URI(value.trim());
            if (uri.getHost() == null) return;
            int port = uri.getPort() > 0 ? uri.getPort() : 1080;
            String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
            java.net.Proxy.Type type = scheme.startsWith("socks") ? java.net.Proxy.Type.SOCKS : java.net.Proxy.Type.HTTP;
            manual = new java.net.Proxy(type, new InetSocketAddress(uri.getHost(), port));
            manualHost = uri.getHost();
            manualUserInfo = uri.getUserInfo();
            Authenticator.setDefault(new ProxyAuthenticator(this));
        } catch (Exception ignored) {
        }
    }

    public String getManualUserInfo(String host) {
        return host != null && host.equalsIgnoreCase(manualHost) ? manualUserInfo : null;
    }

    public synchronized void addAll(List<Proxy> items) {
        if (items.isEmpty()) return;
        items.forEach(Proxy::init);
        proxy.addAll(items);
        proxy.sort(null);
    }

    public synchronized void clear() {
        Authenticator.setDefault(null);
        proxy.clear();
    }

    public List<Proxy> getProxy() {
        return proxy;
    }

    private List<java.net.Proxy> fallback(URI uri) {
        return system != null ? system.select(uri) : List.of(java.net.Proxy.NO_PROXY);
    }

    @Override
    public List<java.net.Proxy> select(URI uri) {
        if (uri.getHost() == null || "127.0.0.1".equals(uri.getHost())) return fallback(uri);
        java.net.Proxy item = manual;
        if (item != null) return List.of(item);
        if (proxy.isEmpty()) return fallback(uri);
        for (Proxy current : proxy) for (String host : current.getHosts()) if (Util.containOrMatch(uri.getHost(), host)) return !current.getProxies().isEmpty() ? current.getProxies() : fallback(uri);
        return fallback(uri);
    }

    @Override
    public void connectFailed(URI uri, SocketAddress socketAddress, IOException e) {
        if (system != null) system.connectFailed(uri, socketAddress, e);
    }
}
