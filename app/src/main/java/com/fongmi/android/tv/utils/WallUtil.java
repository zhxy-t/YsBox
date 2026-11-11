package com.fongmi.android.tv.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.event.ConfigEvent;
import com.fongmi.android.tv.setting.Setting;
import com.github.catvod.utils.Path;

import java.io.File;
import java.io.FileOutputStream;

public final class WallUtil {

    public static final int MIN_BUILT_IN = 1;
    public static final int MAX_BUILT_IN = 4;

    private WallUtil() {
    }

    public static void cycleBuiltIn() {
        int wall = Setting.getWall();
        Setting.putWall(wall < MIN_BUILT_IN || wall >= MAX_BUILT_IN ? MIN_BUILT_IN : wall + 1);
        Setting.putWallType(0);
        ConfigEvent.wall();
    }

    public static void applyLocal(Uri uri, Listener listener) {
        Task.submit(() -> {
            try {
                File file = Path.wall(0);
                FileUtil.copyAtomically(uri, file);
                apply(file);
                App.post(listener::success);
            } catch (Throwable e) {
                e.printStackTrace();
                App.post(listener::error);
            }
        });
    }

    public static void applyUrl(String url, Listener listener) {
        Download.create(url, Path.wall(0)).tag("wall_apply").start(new Download.Callback() {
            @Override
            public void error(String msg) {
                listener.error();
            }

            @Override
            public void success(File file) {
                Task.submit(() -> {
                    try {
                        apply(file);
                        App.post(listener::success);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        App.post(listener::error);
                    }
                });
            }
        });
    }

    private static void apply(File file) throws Throwable {
        Setting.putWallType(0);
        if (isGif(file)) Setting.putWallType(1);
        else if (isVideo(file)) Setting.putWallType(2);
        setSnapshot(file);
        Setting.putWall(0);
        App.post(ConfigEvent::wall);
    }

    private static void setSnapshot(File file) throws Throwable {
        Bitmap bitmap = Glide.with(App.get()).asBitmap().frame(0).load(file).override(ResUtil.getScreenWidth(), ResUtil.getScreenHeight()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).submit().get();
        try (FileOutputStream fos = new FileOutputStream(Path.wallCache())) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } finally {
            bitmap.recycle();
        }
    }

    private static boolean isVideo(File file) {
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(file.getAbsolutePath());
            return "yes".equalsIgnoreCase(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isGif(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            return "image/gif".equals(options.outMimeType);
        } catch (Exception e) {
            return false;
        }
    }

    public interface Listener {

        void success();

        void error();
    }
}
