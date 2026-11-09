package com.fongmi.android.tv.player.subtitle;

/**
 * libass JNI adapter.
 *
 * 该适配器依赖的 libfongmi_ass.so 并未随包提供，因此历史上这个静态块总会
 * 抛 LinkageError 并将 AVAILABLE 置为 false（ASS 字幕一直回退到 Media3 默认
 * 解码器）。但它仍会先执行 System.loadLibrary("mpv")：jniLibs/ 下的 libmpv.so
 * 与 assets/mpv-libs/ 下主播放器用的 libmpv.so 是两套不同构建（前者依赖
 * libffmpeg.so，后者依赖拆分库），SONAME 相同会被动态链接器全局去重，从而
 * 污染主播放器的 native 库状态，导致 MPV 起播失败 / 有声无画面。
 *
 * 因此这里直接禁用：不再加载任何 jniLibs 库，主播放器独占 assets 那套。
 */
final class AssNative {
    static final boolean AVAILABLE = false;

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
