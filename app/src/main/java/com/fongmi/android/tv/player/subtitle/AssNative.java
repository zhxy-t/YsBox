package com.fongmi.android.tv.player.subtitle;

import com.fongmi.android.tv.App;

import is.xyz.mpv.MPVLib;

/** Small JNI adapter using the libass already exported by the bundled player. */
final class AssNative {
    static final boolean AVAILABLE = init();

    private static boolean init() {
        try {
            // 全应用只保留一条 native 加载路径：assets 里的拆分库版 libmpv.so。
            // 它导出与已删除的 jniLibs 单体版完全相同的 ass_* 符号，但不会与
            // MPV 主播放器争抢同名 SONAME（链接器按 SONAME 全局去重，谁先谁赢）。
            // 必须先 ensureLoaded，否则 fongmi_ass 里的 dlopen("libmpv.so") 命中不到已加载库。
            if (!MPVLib.ensureLoaded(App.get())) return false;
            System.loadLibrary("fongmi_ass");
            return available();
        } catch (LinkageError | RuntimeException unavailable) {
            return false;
        }
    }

    static native boolean available();

    static native long open(String config, String fonts, String family);

    static native long read(long context, byte[] script);

    static native long newTrack(long context, byte[] codecPrivate);

    static native void chunk(long track, byte[] data, long startMs, long durationMs, boolean reset);

    static native long endMs(long track);

    static native int[] render(long context, long track, long timeMs);

    static native void freeTrack(long track);

    static native void close(long context);
}
