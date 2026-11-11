package com.fongmi.android.tv.setting;

import com.github.catvod.utils.Prefers;

/** MPV 帧率策略：trae 未做完整性能面板，只保留 MPV 播放器内部读取的两项。 */
public final class MpvPerformanceSetting {

    public static final int FRAME_RATE_OFF = 0;
    public static final int FRAME_RATE_SEAMLESS = 1;
    private static final String KEY_FRAME_RATE = "perf_mpv_frame_rate";

    private MpvPerformanceSetting() {
    }

    public static int getFrameRateMode() {
        return Math.clamp(Prefers.getInt(KEY_FRAME_RATE, FRAME_RATE_SEAMLESS), FRAME_RATE_OFF, FRAME_RATE_SEAMLESS);
    }

    public static String getFrameRateText() {
        return getFrameRateMode() == FRAME_RATE_OFF ? "关闭" : "仅无缝";
    }
}
