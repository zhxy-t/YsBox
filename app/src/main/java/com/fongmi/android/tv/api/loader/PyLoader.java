package com.fongmi.android.tv.api.loader;

import com.fongmi.android.tv.App;
import com.fongmi.chaquo.Loader;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PyLoader {

    private final ConcurrentHashMap<String, Spider> spiders;
    private final Loader loader;
    private volatile String recent;

    public PyLoader() {
        spiders = new ConcurrentHashMap<>();
        loader = new Loader();
    }

    public void clear() {
        spiders.values().forEach(Spider::destroy);
        spiders.clear();
        recent = null;
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    public void remove(String key) {
        Spider spider = spiders.remove(key);
        if (key != null && key.equals(recent)) recent = null;
        if (spider != null) {
            try {
                spider.destroy();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * 创建失败（含本次加载被取消）一律不写缓存：computeIfAbsent 会把返回的
     * SpiderNull 一起缓存下来，一次失败就毒化该脚本整个进程，首页永远空白。
     */
    public Spider getSpider(String key, String api, String ext) {
        Spider cached = spiders.get(key);
        if (cached != null) return cached;
        try {
            Spider spider = loader.spider(api);
            spider.siteKey = key;
            spider.init(App.get(), ext);
            Spider old = spiders.putIfAbsent(key, spider);
            if (old != null) {
                spider.destroy();
                return old;
            }
            return spider;
        } catch (Throwable e) {
            // 加载被取消时 Loader 会恢复中断标记；这里吞掉异常后必须清掉，
            // 否则该标记会残留在线程池线程上，拖垮后续所有任务。
            Thread.interrupted();
            e.printStackTrace();
            return new SpiderNull();
        }
    }

    public Object[] proxy(Map<String, String> params) throws Exception {
        if (recent == null) return null;
        Spider spider = spiders.get(recent);
        return spider != null ? spider.proxy(params) : null;
    }
}
