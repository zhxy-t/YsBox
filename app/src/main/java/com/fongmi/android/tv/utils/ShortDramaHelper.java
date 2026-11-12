package com.fongmi.android.tv.utils;

import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Site;

/**
 * 短剧识别：全部自动判断，没有任何手动标识。
 * 1) 插件文件名含「短剧」或「[短]」；
 * 2) 分类名含 短剧/竖屏/微剧/micro；
 * 视频本体判定在起播后由 VideoActivity.maybePromoteShortDrama 按
 * 时长≤8分钟+≥3集自动升格，不再按封面/集名猜测。
 */
public class ShortDramaHelper {

    /** 插件（PY/JS 文件）是否短剧：只看文件名。 */
    public static boolean isPluginShortDrama(String fileName) {
        if (fileName == null || fileName.isEmpty()) return false;
        return fileName.contains("短剧") || fileName.contains("[短]");
    }

    /** 通过站点 key 找到其插件文件名，再判断。 */
    public static boolean isPluginShortDramaBySite(String siteKey) {
        try {
            if (siteKey == null || siteKey.isEmpty()) return false;
            Site site = VodConfig.get().getSite(siteKey);
            if (site == null) return false;
            String api = site.getApi();
            if (api == null || api.isEmpty()) return false;
            String name = api.substring(api.lastIndexOf('/') + 1);
            int q = name.indexOf('?');
            if (q > 0) name = name.substring(0, q);
            if (name.isEmpty()) return false;
            return isPluginShortDrama(name);
        } catch (Exception e) {
            return false;
        }
    }

    /** 分类名自动匹配：短剧/竖屏/微剧/micro（不区分大小写）。 */
    public static boolean isShortDramaCategoryName(String name) {
        if (name == null || name.isEmpty()) return false;
        String n = name.toLowerCase();
        return n.contains("短剧") || n.contains("竖屏") || n.contains("微剧") || n.contains("micro");
    }

    /** 点击分流：插件文件名或分类名命中短剧即直接进短剧播放。 */
    public static boolean shouldDirectPlay(String siteKey, String typeId, String typeName) {
        return isPluginShortDramaBySite(siteKey) || isShortDramaCategoryName(typeName);
    }


}
