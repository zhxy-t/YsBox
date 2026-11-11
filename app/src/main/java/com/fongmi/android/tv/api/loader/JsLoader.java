package com.fongmi.android.tv.api.loader;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.quickjs.crawler.Loader;
import com.fongmi.quickjs.utils.Module;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsLoader {

    private static final String BUILTIN_DRPY2 = "assets://js/drpy/drpy2.min.js";

    private final ConcurrentHashMap<String, Spider> spiders;
    private final Loader loader;
    private volatile String recent;

    public JsLoader() {
        spiders = new ConcurrentHashMap<>();
        loader = new Loader();
    }

    public void clear() {
        spiders.values().forEach(Spider::destroy);
        Module.get().clear();
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

    /** 同 PyLoader：失败/取消的 SpiderNull 不许写进缓存，否则一次失败毒化该脚本整个进程。 */
    public Spider getSpider(String key, String api, String ext, String jar) {
        Spider cached = spiders.get(key);
        if (cached != null) return cached;
        try {
            Spider spider = loader.spider(resolveApi(api), BaseLoader.get().dex(jar));
            spider.siteKey = key;
            spider.init(App.get(), ext);
            Spider old = spiders.putIfAbsent(key, spider);
            if (old != null) {
                spider.destroy();
                return old;
            }
            return spider;
        } catch (Throwable e) {
            Thread.interrupted();
            e.printStackTrace();
            return new SpiderNull();
        }
    }

    private static String resolveApi(String api) {
        return Setting.isDrpyBuiltin() && (isDrpy2(api) || isDrpy2Engine(api)) ? BUILTIN_DRPY2 : api;
    }

    /** 名称判定：最后一段路径（去掉 ?query/#fragment）里含 drpy2，如 drpy2、drpy2.js、drpy2.min.js。 */
    static boolean isDrpy2(String api) {
        if (api == null || api.isEmpty()) return false;
        String name = api.toLowerCase(Locale.ROOT);
        int end = name.length();
        int q = name.indexOf('?');
        if (q >= 0) end = q;
        int h = name.indexOf('#');
        if (h >= 0 && h < end) end = h;
        int slash = name.lastIndexOf('/', end - 1);
        return name.substring(slash + 1, end).contains("drpy2");
    }

    /** 内容判定：名称随便改，只要文件里带 drpy2 的引擎特征，也走内置引擎。 */
    private static boolean isDrpy2Engine(String api) {
        if (api == null || !api.contains(".js")) return false;
        try {
            String content = Module.get().fetch(api);
            return content != null && content.contains("drpy2") && content.contains("runMain") && content.contains("getRule");
        } catch (Throwable e) {
            return false;
        }
    }

    public Object[] proxy(Map<String, String> params) throws Exception {
        if (recent == null) return null;
        Spider spider = spiders.get(recent);
        return spider != null ? spider.proxy(params) : null;
    }
}
