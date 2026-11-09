package com.fongmi.android.tv.ai.asr;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.media3.common.text.Cue;
import androidx.media3.ui.SubtitleView;

import java.util.Collections;
import java.util.Map;

/**
 * AI 本地字幕播放器控制器。
 *
 * 职责：
 * - 管理 AI 字幕开关状态（SharedPreferences）
 * - 协调 AiAsrAudioProcessor（音频采集）与 AiAsrEngine（识别）
 * - 将识别结果显示到播放器的 SubtitleView
 *
 * 与云端字幕翻译的区别：本地识别不消耗 API 额度，UI 上明确标注"本地"。
 */
public class AiAsrController {

    private static final String PREF_ASR_ENABLED = "xingchen.ai_asr_subtitle_enabled";

    private static volatile AiAsrController instance;

    public static AiAsrController get() {
        if (instance == null) {
            synchronized (AiAsrController.class) {
                if (instance == null) instance = new AiAsrController();
            }
        }
        return instance;
    }

    /** 全局单例 AudioProcessor（ExoUtil 注入到音频链） */
    private final AiAsrAudioProcessor audioProcessor = new AiAsrAudioProcessor();

    private Context appContext;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SubtitleView subtitleView;
    private boolean enabled = false;
    private boolean preparing = false;

    /** 字幕自动隐藏 */
    private final Runnable hideRunnable = () -> setSubtitleText("");

    private AiAsrController() {}

    public AiAsrAudioProcessor getAudioProcessor() {
        return audioProcessor;
    }

    /** 绑定播放器的 SubtitleView（VideoActivity 在播放时调用） */
    public void bindSubtitleView(SubtitleView view) {
        this.subtitleView = view;
    }

    public void unbind() {
        mainHandler.removeCallbacks(hideRunnable);
        this.subtitleView = null;
    }

    /** AI 字幕是否已开启 */
    public boolean isEnabled(Context context) {
        return context.getSharedPreferences("xingchen", Context.MODE_PRIVATE)
                .getBoolean(PREF_ASR_ENABLED, false);
    }

    private void setEnabled(Context context, boolean enabled) {
        context.getSharedPreferences("xingchen", Context.MODE_PRIVATE)
                .edit().putBoolean(PREF_ASR_ENABLED, enabled).apply();
    }

    /**
     * 切换 AI 字幕开关。
     * @return true=操作已接受（异步准备中或已开启），false=设备不支持
     */
    public boolean toggle(Context context, ToggleCallback callback) {
        if (!AiAsrPluginManager.isSupported()) {
            callback.onError("当前设备架构不支持本地语音识别");
            return false;
        }
        boolean target = !isEnabled(context);
        if (target) {
            enable(context, callback);
        } else {
            disable(context);
            callback.onDisabled();
        }
        return true;
    }

    /** 开启 */
    public void enable(Context context, ToggleCallback callback) {
        if (preparing) {
            callback.onError("正在准备中，请稍候");
            return;
        }
        preparing = true;
        callback.onPreparing("正在准备语音识别…");

        AiAsrEngine.get().prepare(context, new AiAsrEngine.PrepareCallback() {
            @Override
            public void onProgress(String stage, int percent) {
                mainHandler.post(() -> callback.onPreparing(stage + " " + percent + "%"));
            }

            @Override
            public void onReady() {
                preparing = false;
                setEnabled(context, true);
                appContext = context.getApplicationContext();
                AiAsrEngine.get().setListener(engineListener);
                mpvMode = false;
                audioProcessor.setCaptureEnabled(true);
                if (AiAsrEngine.get().start()) {
                    mainHandler.post(callback::onEnabled);
                } else {
                    // 同上回滚：采集与标志一并撤掉，与 disable() 的收尾口径一致。
                    audioProcessor.setCaptureEnabled(false);
                    stopMpvCapture();
                    mpvMode = false;
                    setEnabled(context, false);
                    mainHandler.post(() -> callback.onError("识别启动失败"));
                }
            }

            @Override
            public void onError(String msg) {
                preparing = false;
                mainHandler.post(() -> callback.onError(msg));
            }
        });
    }

    /** 关闭 */
    public void disable(Context context) {
        setEnabled(context, false);
        audioProcessor.setCaptureEnabled(false);
        AiAsrEngine.get().stop();
        mainHandler.removeCallbacks(hideRunnable);
        setSubtitleText("");
    }

    /** 当前是否为 MPV 模式（需要采集 MPV 音频） */
    private boolean isMpvActive() {
        try {
            return mpvInfoProvider != null && mpvInfoProvider.isMpvActive();
        } catch (Exception e) {
            return false;
        }
    }

    public void onPlayerReady(Context context) {
        if (isEnabled(context) && !AiAsrEngine.get().isRunning() && !preparing) {
            enable(context, new ToggleCallback() {
                @Override public void onPreparing(String msg) {}
                @Override public void onEnabled() {}
                @Override public void onDisabled() {}
                @Override public void onError(String msg) {}
            });
        } else if (isEnabled(context)) {
            audioProcessor.setCaptureEnabled(true);
        }
    }

    /** 播放器释放时调用 */
    public void onPlayerRelease(Context context) {
        audioProcessor.setCaptureEnabled(false);
        AiAsrEngine.get().stop();
        mainHandler.removeCallbacks(hideRunnable);
        setSubtitleText("");
    }

    /** 识别结果监听 */
    private final AiAsrEngine.Listener engineListener = new AiAsrEngine.Listener() {
        @Override
        public void onPartial(String text) {
            setSubtitleText(text);
        }

        @Override
        public void onFinal(String text) {
            setSubtitleText(text);
            // 一句话显示 4 秒后隐藏
            mainHandler.removeCallbacks(hideRunnable);
            mainHandler.postDelayed(hideRunnable, 4000);
        }

        @Override
        public void onError(String msg) {
            // 识别错误不打断播放，静默
        }
    };

    /** 在 SubtitleView 显示文本 */
    private void setSubtitleText(String text) {
        mainHandler.post(() -> {
            if (subtitleView == null) return;
            try {
                if (text == null || text.isEmpty()) {
                    subtitleView.setCues(Collections.emptyList());
                } else {
                    // 确保字幕 View 可见
                    if (subtitleView.getVisibility() != View.VISIBLE) {
                        subtitleView.setVisibility(View.VISIBLE);
                    }
                    Cue cue = new Cue.Builder().setText(text).build();
                    subtitleView.setCues(Collections.singletonList(cue));
                }
            } catch (Exception ignored) {}
        });
    }

    public interface ToggleCallback {
        void onPreparing(String msg);
        void onEnabled();
        void onDisabled();
        void onError(String msg);
    }
}
