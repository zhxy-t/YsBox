package com.fongmi.android.tv.utils;

import static android.widget.ImageView.ScaleType.CENTER_CROP;
import static android.widget.ImageView.ScaleType.FIT_CENTER;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.impl.CustomTarget;
import com.fongmi.android.tv.server.Server;
import com.github.catvod.utils.Crypto;
import com.github.catvod.utils.Json;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.HttpHeaders;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import jahirfiquitiva.libs.textdrawable.TextDrawable;

public class ImgUtil {

    private static final int MAX_CACHE_SIZE = 16 * 1024 * 1024;
    private static final int MAX_DATA_URI_LENGTH = 8 * 1024 * 1024;
    private static final Pattern IMAGE_MIME = Pattern.compile("image/[a-z0-9.+-]+");
    private static final Set<String> failed = Collections.synchronizedSet(new HashSet<>());
    private static final Cache<String, Image> CACHE = CacheBuilder.newBuilder().maximumWeight(MAX_CACHE_SIZE).weigher((String key, Image value) -> value.data().length).build();

    public static void logo(ImageView view) {
        try {
            Glide.with(view).load(UrlUtil.convert(VodConfig.get().getConfig().getLogo())).circleCrop().override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).error(R.drawable.ic_logo).into(view);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void load(String url, CustomTarget<Bitmap> target) {
        try {
            Glide.with(App.get()).asBitmap().load(getUrl(url)).override(ResUtil.dp2px(96), ResUtil.dp2px(96)).error(R.drawable.artwork).into(target);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void load(Context context, String url, CustomTarget<Drawable> target) {
        try {
            Glide.with(context).load(getUrl(url)).override(ResUtil.getScreenWidth(), ResUtil.getScreenHeight()).error(R.drawable.artwork).into(target);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void load(String text, String url, ImageView view) {
        load(text, url, view, true);
    }

    public static void load(String text, String url, ImageView view, boolean vod) {
        load(text, url, view, vod, text);
    }

    /** 占位色按分类哈希：同类型影片缺图时占位底色一致（category 为空回退片名哈希）。 */
    public static void load(String text, String url, ImageView view, boolean vod, String category) {
        view.setScaleType(vod ? CENTER_CROP : FIT_CENTER);
        if (!vod) view.setVisibility(TextUtils.isEmpty(url) ? View.GONE : View.VISIBLE);
        if (TextUtils.isEmpty(url) || failed.contains(url)) view.setImageDrawable(getTextDrawable(text, vod, category));
        else try {
            RequestBuilder<Drawable> builder = Glide.with(view).load(getUrl(url)).listener(getListener(text, url, view, vod, category));
            if (vod) builder.centerCrop().into(view);
            else builder.fitCenter().into(view);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /** 小程序兼容重载：带 referer 参数（当前实现忽略 referer，与 xingchen 视觉一致）。 */
    public static void load(String text, String url, ImageView view, String referer) {
        load(text, url, view, true);
    }

    public static void load(String text, String url, ImageView view, boolean vod, int width, int height, String referer) {
        load(text, url, view, vod, width, height);
    }

    public static void loadCircle(String text, String url, ImageView view, String referer) {
        loadCircle(text, url, view);
    }

    public static void loadCircle(String text, String url, ImageView view) {
        view.setScaleType(CENTER_CROP);
        if (TextUtils.isEmpty(url)) view.setImageDrawable(getTextDrawable(text, true));
        else try {
            Glide.with(view).load(getUrl(url)).circleCrop().into(view);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void load(String text, String url, ImageView view, int width, int height) {
        load(text, url, view, true, width, height);
    }

    public static void load(String text, String url, ImageView view, boolean vod, int width, int height) {
        view.setScaleType(vod ? CENTER_CROP : FIT_CENTER);
        if (!vod) view.setVisibility(TextUtils.isEmpty(url) ? View.GONE : View.VISIBLE);
        if (TextUtils.isEmpty(url) || failed.contains(url)) view.setImageDrawable(getTextDrawable(text, vod));
        else try {
            RequestBuilder<Drawable> builder = Glide.with(view).load(getUrl(url)).listener(getListener(text, url, view, vod, text));
            if (width > 0 && height > 0) builder = builder.override(width, height);
            if (vod) builder.centerCrop().into(view);
            else builder.fitCenter().into(view);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 小程序规则图标：URL 图标加载，失败时用默认图标占位（对照 8.83 icon_mini_program_icon）。
     */
    public static void loadRuleIcon(String title, String url, ImageView view) {
        view.setScaleType(CENTER_CROP);
        view.setContentDescription(title);
        if (TextUtils.isEmpty(url)) {
            clearRuleIconRequest(view);
            view.setImageDrawable(defaultMiniIcon(view));
            return;
        }
        try {
            Object model = getUrl(url);
            if (model == null) {
                clearRuleIconRequest(view);
                view.setImageDrawable(defaultMiniIcon(view));
                return;
            }
            Glide.with(view).load(model).placeholder(defaultMiniIcon(view)).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    view.setImageDrawable(defaultMiniIcon(view));
                    return true;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).centerCrop().into(view);
        } catch (Throwable e) {
            e.printStackTrace();
            clearRuleIconRequest(view);
            view.setImageDrawable(defaultMiniIcon(view));
        }
    }

    private static Drawable defaultMiniIcon(ImageView view) {
        try {
            return androidx.appcompat.content.res.AppCompatResources.getDrawable(view.getContext(), R.drawable.icon_mini_program_icon);
        } catch (Throwable e) {
            return new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT);
        }
    }

    private static void clearRuleIconRequest(ImageView view) {
        try {
            Glide.with(view).clear(view);
        } catch (Throwable ignored) {
        }
    }

    public static Object getUrl(String url) {
        String param = null;
        url = UrlUtil.convert(url);
        if (url.startsWith("data:")) return url;
        LazyHeaders.Builder builder = new LazyHeaders.Builder();
        // 注意：参数名带在 URL 末尾（如 "...@Referer="）时，split 会丢掉尾部空串只剩 1 段，
        // 直接取 [1] 就是 AIOOBE length=1;index=1（真机 0.0.1.310 崩溃点）。
        // 统一用 limit=2 保留尾段，并跳过空值。
        if (url.contains("@Headers=")) {
            String[] p = url.split("@Headers=", 2);
            String v = p.length > 1 ? p[1].split("@")[0] : "";
            if (!TextUtils.isEmpty(v)) { addHeader(builder, param = v); }
        }
        if (url.contains("@Cookie=")) {
            String[] p = url.split("@Cookie=", 2);
            String v = p.length > 1 ? p[1].split("@")[0] : "";
            if (!TextUtils.isEmpty(v)) builder.addHeader(HttpHeaders.COOKIE, param = v);
        }
        if (url.contains("@Referer=")) {
            String[] p = url.split("@Referer=", 2);
            String v = p.length > 1 ? p[1].split("@")[0] : "";
            if (!TextUtils.isEmpty(v)) builder.addHeader(HttpHeaders.REFERER, param = v);
        }
        if (url.contains("@User-Agent=")) {
            String[] p = url.split("@User-Agent=", 2);
            String v = p.length > 1 ? p[1].split("@")[0] : "";
            if (!TextUtils.isEmpty(v)) builder.addHeader(HttpHeaders.USER_AGENT, param = v);
        }
        url = param == null ? url : url.split("@")[0];
        return TextUtils.isEmpty(url) ? null : new GlideUrl(url, builder.build());
    }

    public static Object getUrl(String url, String referer) {
        if (TextUtils.isEmpty(url)) return null;
        if (TextUtils.isEmpty(referer) || url.contains("@Referer=")) return getUrl(url);
        return getUrl(url + "@Referer=" + referer);
    }

    public static String cache(String url) {
        if (!isData(url)) return url;
        if (url.length() > MAX_DATA_URI_LENGTH) return "";
        String key = Crypto.md5(url);
        if (TextUtils.isEmpty(key)) return "";
        if (CACHE.asMap().computeIfAbsent(key, ignored -> decode(url)) == null) return "";
        String address = Server.get().getAddress("/image/" + key);
        failed.remove(address);
        return address;
    }

    private static boolean isData(String url) {
        return !TextUtils.isEmpty(url) && url.regionMatches(true, 0, "data:", 0, 5);
    }

    private static Image decode(String url) {
        try {
            int comma = url.indexOf(',');
            if (comma == -1) return null;
            String metadata = url.substring(5, comma).toLowerCase(Locale.ROOT);
            String mime = metadata.split(";", 2)[0];
            if (!IMAGE_MIME.matcher(mime).matches() || !metadata.endsWith(";base64")) return null;
            byte[] data = Base64.decode(url.substring(comma + 1), Base64.DEFAULT);
            return data.length == 0 ? null : new Image(mime, data);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Image getImage(String key) {
        return CACHE.getIfPresent(key);
    }

    public record Image(String mime, byte[] data) {
    }

    private static void addHeader(LazyHeaders.Builder builder, String header) {
        Map<String, String> map = Json.toMap(Json.parse(header));
        for (Map.Entry<String, String> entry : map.entrySet()) builder.addHeader(UrlUtil.fixHeader(entry.getKey()), entry.getValue());
    }

    private static Drawable getTextDrawable(String text, boolean vod) {
        return getTextDrawable(text, vod, null);
    }

    private static Drawable getTextDrawable(String text, boolean vod, String category) {
        TextDrawable.Builder builder = new TextDrawable.Builder();
        text = TextUtils.isEmpty(text) ? "！" : text.substring(0, 1);
        String key = TextUtils.isEmpty(category) ? text : category;
        if (vod) builder.buildRect(text, ColorGenerator.get400(key));
        return builder.buildRoundRect(text, ColorGenerator.get400(key), ResUtil.dp2px(4));
    }

    private static RequestListener<Drawable> getListener(String text, String url, ImageView view, boolean vod, String category) {
        return new RequestListener<>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                view.setImageDrawable(getTextDrawable(text, vod, category));
                failed.add(url);
                return true;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        };
    }
}
