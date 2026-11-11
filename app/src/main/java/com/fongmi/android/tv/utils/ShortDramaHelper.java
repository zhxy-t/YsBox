package com.fongmi.android.tv.utils;

import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Site;

/**
 * 短剧识别：全部自动判断，没有任何手动标识。
 * 1) 插件文件名含「短剧」或「[短]」；
 * 2) 分类名含 短剧/竖屏/微剧/micro；
 * 3) 封面竖图（高宽比 ≥ 1.6，见 CoverRatioCache）。
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

    /**
     * 选集标题是否呈短剧特征：≥3 集，且前 10 集里至少 min(2, count/2) 条带
     * 「第X集 / EP.X / E123 / S01E01」形态。
     * 与海阔详情页 playDetailEpisode / playContentDirect 的判定口径一致。
     */
    public static boolean isEpisodeListShortDrama(java.util.List<String> titles) {
        if (titles == null || titles.size() < 3) return false;
        int count = titles.size();
        int dramaLike = 0;
        for (int i = 0; i < Math.min(count, 10); i++) {
            String name = titles.get(i);
            if (name != null && name.matches(".*(第[0-9一二三四五六七八九十百零]+集|EP[.\\s]?[0-9]+|E[0-9]{1,3}|S[0-9]+E[0-9]+).*")) {
                dramaLike++;
            }
        }
        return dramaLike >= Math.min(2, count / 2);
    }

    /** 封面图已就绪时用真实尺寸现算高宽比（占位图无内在尺寸，不会误判）。 */
    public static boolean isLiveShortDramaCover(android.view.View coverView, String pic) {
        if (!(coverView instanceof android.widget.ImageView imageView)) return false;
        android.graphics.drawable.Drawable drawable = imageView.getDrawable();
        if (drawable == null) return false;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0 || height <= 0) return false;
        if (pic != null && !pic.isEmpty()) CoverRatioCache.put(pic, width, height);
        return (float) height / (float) width >= CoverRatioCache.SHORT_DRAMA_THRESHOLD;
    }
}
