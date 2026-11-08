package com.fongmi.android.tv.player.engine;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.mpvplayer.MpvPlayer;
import androidx.media3.mpvplayer.MpvPlayerConfig;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.player.media.MediaItemFactory;
import com.fongmi.android.tv.player.media.PlaySpec;
import com.fongmi.android.tv.player.mpv.MpvConfigStore;
import com.fongmi.android.tv.setting.DecodeSetting;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.setting.SubtitleSetting;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.crawler.SpiderDebug;


import is.xyz.mpv.MPVLib;

/**
 * xingchen 分支 MPV 内核的宿主适配层：直接复用 androidx.media3.mpvplayer.MpvPlayer
 * （自带 HLS 代理、整集缓存、DV7/DV8、字幕样式、轨道刷新），只把 trae 的
 * PlayerEngine 语义翻译过去。
 */
@UnstableApi
public class MpvPlayerEngine implements PlayerEngine {

    private static final String HWDEC_SOFTWARE_FALLBACK = "hwdec-software-fallback";
    private static final java.util.concurrent.ExecutorService PRELOAD_EXECUTOR =
            java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r, "mpv-preload");
                thread.setDaemon(true);
                return thread;
            });

    private final String hardwareDecode;
    private MpvPlayer player;
    private PlaySpec spec;
    private int decode;
    private boolean released;
    private long nextEpisodePreloadDurationMs;

    public MpvPlayerEngine(int decode, Player.Listener listener) {
        this.decode = decode;
        this.hardwareDecode = decode == HARD ? "mediacodec" : "no";
        this.player = buildPlayer(listener);
    }

    public static boolean isAvailable() {
        try {
            return MPVLib.ensureLoaded(App.get());
        } catch (Throwable e) {
            SpiderDebug.log("mpv", "native unavailable: %s", e.getMessage());
            return false;
        }
    }

    private MpvPlayer buildPlayer(Player.Listener listener) {
        MpvConfigStore.ensureReady();
        MpvPlayer player = new MpvPlayer(App.get(), buildConfig());
        player.addListener(listener);
        return player;
    }

    private MpvPlayerConfig buildConfig() {
        int cacheSeconds = Math.clamp(PlayerSetting.getBuffer() * 3, 15, 60);
        String ua = Setting.getUa();
        if (ua == null || ua.isBlank()) ua = MediaItemFactory.getDefaultUserAgent();
        MpvPlayerConfig.Builder builder = MpvPlayerConfig.builder(App.get())
                .configDir(MpvConfigStore.configDir())
                .userAgent(ua)
                .hwdec(hardwareDecode)
                .vo("gpu-next")
                .gpuContext("android")
                .gpuApi("opengl")
                .openglEs(true)
                .audioSpdif(resolveAudioSpdifCodecs())
                .multichannelPcm(false)
                .logLevel("all=warn")
                .cache(true)
                .demuxerMaxBytes(PlayerSetting.getPlayCacheSize())
                .demuxerMaxBackBytes(0)
                .cacheSeconds(cacheSeconds)
                .option(HWDEC_SOFTWARE_FALLBACK, decode == HARD ? "no" : "yes");
        return builder.build();
    }

    private static String resolveAudioSpdifCodecs() {
        if (DecodeSetting.isAudioPassThrough()) return MpvAudioCapabilities.getAudioSpdifCodecs(App.get());
        return String.join(",", MpvAudioCapabilities.getAudioCompressedCodecs(App.get()));
    }

    @Override
    public Type getType() {
        return Type.MPV;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public int getAudioChannelCount() {
        Tracks tracks = player.getCurrentTracksSnapshot();
        for (Tracks.Group group : tracks.getGroups()) {
            if (group.getType() != C.TRACK_TYPE_AUDIO) continue;
            for (int i = 0; i < group.length; i++) {
                if (group.isTrackSelected(i)) return group.getTrackFormat(i).channelCount;
            }
        }
        return Format.NO_VALUE;
    }

    @Override
    public void release() {
        if (released) return;
        released = true;
        player.release();
    }

    @Override
    public void setDecode(int decode) {
        this.decode = decode;
        player.setHardwareDecodeEnabled(decode == HARD);
    }

    @Override
    public void start(PlaySpec spec, long startPositionMs) {
        this.spec = spec;
        MediaItem item = MediaItemFactory.from(spec);
        if (startPositionMs > 0) player.setMediaItem(item, startPositionMs);
        else player.setMediaItem(item);
        player.prepare();
        player.play();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public void preload(PlaySpec spec, long startPositionMs) {
        if (spec == null || released) return;
        MediaItem item = MediaItemFactory.from(spec);
        int seconds = nextEpisodePreloadDurationMs > 0
                ? (int) Math.min(300, Math.max(15, nextEpisodePreloadDurationMs / 1000)) : 30;
        int segments = Math.max(2, Math.min(30, seconds / 6));
        MpvPlayer target = player;
        PRELOAD_EXECUTOR.execute(() -> {
            if (released) return;
            try {
                target.prefetchMediaItem(item, segments);
            } catch (Throwable ignored) {
            }
        });
    }

    @Override
    public void clearPreload() {
        nextEpisodePreloadDurationMs = 0;
    }

    @Override
    public void setNextEpisodePreloadDuration(long ms) {
        nextEpisodePreloadDurationMs = Math.max(0, ms);
    }

    @Override
    public void setSubtitleStyle() {
        applySubtitleStyle();
    }

    @Override
    public void applySubtitleStyle() {
        player.setSubtitleStyle(SubtitleSetting.getScale(), SubtitleSetting.getPosition());
    }

    @Override
    public void setSecondarySubtitleSelection(@Nullable TrackSelectionOverride selection) {
        player.setSecondarySubtitleTrackSelection(mpvIdOf(selection));
    }

    @Override
    public boolean addSubtitle(com.fongmi.android.tv.bean.Sub sub) {
        // MPV 的内嵌字幕走 MediaItem 的 SubtitleConfiguration：返回 false 让
        // PlayerManager 用新 spec 重新起播，subs 会随 MediaItem 一起进 MPV。
        return false;
    }

    @Override
    public String getErrorMessage(PlaybackException e) {
        String message = e.getMessage();
        if (message == null) return "";
        if (message.startsWith(MpvPlayer.ERROR_HLS_PLAYBACK_FAILED)) return ResUtil.getString(R.string.error_play_mpv_hls_unsupported);
        if (message.startsWith(MpvPlayer.ERROR_LOAD_FAILED)) return ResUtil.getString(R.string.error_play_mpv_load_failed);
        if (message.startsWith(MpvPlayer.ERROR_NETWORK_FAILED)) return ResUtil.getString(R.string.error_play_mpv_network_failed);
        if (message.startsWith(MpvPlayer.ERROR_DRM_UNSUPPORTED)) return ResUtil.getString(R.string.error_play_mpv_drm_unsupported);
        if (message.startsWith(MpvPlayer.ERROR_UNEXPECTED_IMAGE)) return ResUtil.getString(R.string.error_play_mpv_unexpected_image);
        if (message.startsWith(MpvPlayer.ERROR_NO_AV_DATA)) return ResUtil.getString(R.string.error_play_mpv_no_av);
        if (message.startsWith(MpvPlayer.ERROR_INVALID_MEDIA_DATA)) return ResUtil.getString(R.string.error_play_mpv_invalid_data);
        if (message.startsWith(MpvPlayer.ERROR_DECODE_FAILED)) return ResUtil.getString(R.string.error_play_mpv_decode_failed);
        if (message.startsWith(MpvPlayer.ERROR_VIDEO_OUTPUT_FAILED)) return ResUtil.getString(R.string.error_play_mpv_video_output);
        return message;
    }

    @Override
    public ErrorAction handleError(PlaybackException e) {
        if (isDecodeFailure(e)) return ErrorAction.DECODE;
        if (shouldRetryFormat(e)) return retryFormat();
        return ErrorAction.FATAL;
    }

    private boolean isDecodeFailure(PlaybackException e) {
        return e.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED
                || e.errorCode == PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED
                || e.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED
                || e.errorCode == PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED
                || e.errorCode == PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES
                || startsWith(e.getMessage(), MpvPlayer.ERROR_DECODE_FAILED);
    }

    private boolean shouldRetryFormat(PlaybackException e) {
        if (spec == null || spec.getFormat() != null) return false;
        if (isTerminalError(e.getMessage())) return false;
        return e.errorCode == PlaybackException.ERROR_CODE_IO_UNSPECIFIED
                || e.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED
                || e.errorCode == PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED
                || e.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED
                || e.errorCode == PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED;
    }

    private ErrorAction retryFormat() {
        spec.setFormat(MimeTypes.APPLICATION_M3U8);
        start(spec, Math.max(0, player.getCurrentPosition()));
        return ErrorAction.RECOVERED;
    }

    private static boolean isTerminalError(String message) {
        return startsWith(message, MpvPlayer.ERROR_HLS_PLAYBACK_FAILED)
                || startsWith(message, MpvPlayer.ERROR_NETWORK_FAILED)
                || startsWith(message, MpvPlayer.ERROR_DRM_UNSUPPORTED)
                || startsWith(message, MpvPlayer.ERROR_UNEXPECTED_IMAGE)
                || startsWith(message, MpvPlayer.ERROR_NO_AV_DATA)
                || startsWith(message, MpvPlayer.ERROR_INVALID_MEDIA_DATA)
                || startsWith(message, MpvPlayer.ERROR_DECODE_FAILED)
                || startsWith(message, MpvPlayer.ERROR_VIDEO_OUTPUT_FAILED);
    }

    private static boolean startsWith(@Nullable String message, String prefix) {
        return message != null && message.startsWith(prefix);
    }

    @Override
    public VideoPlaybackDetails getVideoPlaybackDetails() {
        MpvPlayer.VideoTrackDiagnostics details = player.getSelectedVideoTrackDiagnostics();
        if (details == null) return VideoPlaybackDetails.empty();
        int sourceProfile = details.sourceDolbyVisionProfile() > 0
                ? details.sourceDolbyVisionProfile() : details.dolbyVisionProfile();
        int sourceLevel = details.sourceDolbyVisionLevel() > 0
                ? details.sourceDolbyVisionLevel() : details.dolbyVisionLevel();
        int reportedProfile = details.dolbyVisionProfile() > 0
                ? details.dolbyVisionProfile() : sourceProfile;
        int reportedLevel = details.dolbyVisionLevel() > 0
                ? details.dolbyVisionLevel() : sourceLevel;
        String currentVo = player.getObservedCurrentVideoOutput();
        boolean hdr10Fallback = sourceProfile == 7 && currentVo != null
                && (currentVo.equals("gpu") || currentVo.startsWith("gpu-next"));
        boolean p81 = sourceProfile == 7 && reportedProfile == 8;
        return new VideoPlaybackDetails(
                details.sourceCodecs(),
                reportedProfile,
                reportedLevel,
                details.decodedCodec(),
                details.decoderName(),
                player.getObservedHwdecCurrent(),
                details.outputColorInfo(),
                hdr10Fallback,
                p81);
    }

    private static String mpvIdOf(@Nullable TrackSelectionOverride selection) {
        if (selection == null || selection.trackIndices.isEmpty()) return "no";
        int index = selection.trackIndices.get(0);
        if (index < 0 || index >= selection.mediaTrackGroup.length) return "no";
        Format format = selection.mediaTrackGroup.getFormat(index);
        if (format == null || format.id == null) return "no";
        int colon = format.id.indexOf(':');
        return colon >= 0 ? format.id.substring(colon + 1) : format.id;
    }
}
