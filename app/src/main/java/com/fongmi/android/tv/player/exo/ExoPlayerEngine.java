package com.fongmi.android.tv.player.exo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime;
import androidx.media3.exoplayer.audio.AudioSink;
import androidx.media3.exoplayer.source.MediaSource;

import com.fongmi.android.tv.player.effect.PlayerEffect;
import com.fongmi.android.tv.player.engine.PlayerEngine;
import com.fongmi.android.tv.player.media.MediaItemFactory;
import com.fongmi.android.tv.player.media.PlaySpec;

import java.util.Locale;

public class ExoPlayerEngine implements PlayerEngine, AnalyticsListener {

    private final ExoErrorMessageProvider provider;
    private final ExoPlayerSession session;
    private final ExoPlayerEffect effect;
    private final ExoDiskPreload preload;
    private final ExoPlayer player;
    private String videoDecoderName;
    private PlaySpec spec;

    public ExoPlayerEngine(int decode, Player.Listener listener) {
        this.effect = new ExoPlayerEffect();
        this.preload = new ExoDiskPreload();
        this.provider = new ExoErrorMessageProvider();
        this.session = new ExoPlayerSession(decode, listener, effect.getAudioProcessor());
        this.player = this.session.player();
        this.player.addAnalyticsListener(this);
        this.effect.setPlayer(player);
    }

    @Override
    public void onAudioTrackInitialized(@NonNull EventTime eventTime, @NonNull AudioSink.AudioTrackConfig audioTrackConfig) {
        effect.applyAudioEffect();
    }

    @Override
    public void onAudioTrackReleased(@NonNull EventTime eventTime, @NonNull AudioSink.AudioTrackConfig audioTrackConfig) {
        effect.applyAudioEffect();
    }

    @Override
    public void onTracksChanged(@NonNull EventTime eventTime, @NonNull Tracks tracks) {
        effect.applyVideoEffect();
        session.subtitles().onTracksChanged();
    }

    @Override
    public void onVideoDecoderInitialized(@NonNull EventTime eventTime, @NonNull String decoderName, long initializedTimestampMs, long initializationDurationMs) {
        this.videoDecoderName = decoderName;
    }

    @Override
    public VideoPlaybackDetails getVideoPlaybackDetails() {
        Format selected = selectedVideoFormat();
        Format runtime = player.getVideoFormat();
        int[] profileLevel = dolbyVisionProfileLevel(selected);
        boolean dolbyVision = profileLevel[0] != C.INDEX_UNSET;
        String sourceCodecs = selected == null || selected.codecs == null ? "" : selected.codecs;
        String decodedCodec = runtime == null ? "" : runtime.codecs != null && !runtime.codecs.trim().isEmpty() ? runtime.codecs : runtime.sampleMimeType == null ? "" : runtime.sampleMimeType;
        return new VideoPlaybackDetails(
                sourceCodecs,
                profileLevel[0],
                profileLevel[1],
                decodedCodec,
                videoDecoderName == null ? "" : videoDecoderName,
                "",
                runtime == null ? null : runtime.colorInfo,
                dolbyVision && isHdr10BaseLayer(runtime),
                dolbyVision && isP81Conversion(profileLevel[0], runtime));
    }

    /** 源轨道是 DV 而实际输出是 HEVC/AVC：MediaCodec 走了 HDR10 兼容层。 */
    private static boolean isHdr10BaseLayer(@Nullable Format runtime) {
        if (runtime == null || runtime.sampleMimeType == null) return false;
        if (MimeTypes.VIDEO_DOLBY_VISION.equals(runtime.sampleMimeType)) return false;
        return MimeTypes.VIDEO_H265.equals(runtime.sampleMimeType) || MimeTypes.VIDEO_H264.equals(runtime.sampleMimeType);
    }

    /** 源是 DV7，实际解码为 P8.1：解码器做了 7→8.1 转换。 */
    private static boolean isP81Conversion(int sourceProfile, @Nullable Format runtime) {
        if (sourceProfile != 7 || runtime == null) return false;
        if (!MimeTypes.VIDEO_DOLBY_VISION.equals(runtime.sampleMimeType)) return false;
        String codecs = runtime.codecs == null ? "" : runtime.codecs.toLowerCase(Locale.US);
        return codecs.startsWith("dvhe.81") || codecs.startsWith("dvh1.81");
    }

    private Format selectedVideoFormat() {
        Tracks tracks = player.getCurrentTracks();
        for (Tracks.Group group : tracks.getGroups()) {
            if (group.getType() != C.TRACK_TYPE_VIDEO) continue;
            for (int i = 0; i < group.length; i++) {
                if (group.isTrackSelected(i)) return group.getTrackFormat(i);
            }
        }
        return null;
    }

    private static int[] dolbyVisionProfileLevel(@Nullable Format format) {
        if (format == null) return new int[]{C.INDEX_UNSET, C.INDEX_UNSET};
        String codecs = format.codecs == null ? "" : format.codecs.trim();
        int comma = codecs.indexOf(',');
        if (comma >= 0) codecs = codecs.substring(0, comma).trim();
        String lower = codecs.toLowerCase(Locale.US);
        boolean dolbyVision = MimeTypes.VIDEO_DOLBY_VISION.equals(format.sampleMimeType)
                || lower.startsWith("dvhe.") || lower.startsWith("dvh1.")
                || lower.startsWith("dvav.") || lower.startsWith("dva1.");
        if (!dolbyVision) return new int[]{C.INDEX_UNSET, C.INDEX_UNSET};
        String[] parts = lower.split("\\.");
        return new int[]{parseDolbyVisionPart(parts, 1), parseDolbyVisionPart(parts, 2)};
    }

    private static int parseDolbyVisionPart(String[] parts, int index) {
        if (parts == null || index < 0 || index >= parts.length) return C.INDEX_UNSET;
        try {
            return Integer.parseInt(parts[index]);
        } catch (NumberFormatException ignored) {
            return C.INDEX_UNSET;
        }
    }

    @Override
    public Type getType() {
        return Type.EXO;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public int getAudioChannelCount() {
        Format format = player.getAudioFormat();
        return format == null ? Format.NO_VALUE : format.channelCount;
    }

    @Override
    public PlayerEffect getEffect() {
        return effect;
    }

    @Override
    public void release() {
        player.removeAnalyticsListener(this);
        preload.release();
        effect.release();
        session.release();
    }

    @Override
    public void setDecode(int decode) {
        session.setDecode(decode);
    }

    @Override
    public void setSubtitleStyle() {
        applySubtitleStyle();
    }

    @Override
    public void applySubtitleStyle() {
        session.subtitles().applySubtitleStyle();
    }

    @Override
    public SecondarySubtitleState getSecondarySubtitleState() {
        return session.subtitles().getSecondarySubtitleState();
    }

    @Override
    public void setSecondarySubtitleSelection(@Nullable TrackSelectionOverride selection) {
        session.subtitles().setSecondarySubtitleSelection(selection);
    }

    @Override
    public void setSecondarySubtitleOutput(java.util.function.Consumer<androidx.media3.common.text.CueGroup> output) {
        session.subtitles().setOutput(output);
    }

    @Override
    public void setNextEpisodePreloadDuration(long ms) {
        ExoPlayerSession.setPreloadDurationMs(ms);
    }

    @Override
    public void start(PlaySpec spec, long startPositionMs) {
        session.subtitles().clear();
        session.subtitles().applySubtitleStyle();
        this.spec = spec;
        startInternal(startPositionMs);
    }

    @Override
    public void preload(PlaySpec spec, long startPositionMs) {
        MediaItem item = MediaItemFactory.from(spec);
        Bundle extras = new Bundle();
        if (item.mediaMetadata.extras != null) extras.putAll(item.mediaMetadata.extras);
        extras.putBoolean(ExoMediaSourceFactory.PRELOAD_CACHE_KEY, true);
        MediaMetadata metadata = item.mediaMetadata.buildUpon().setExtras(extras).build();
        session.preload(item.buildUpon().setMediaMetadata(metadata).build(), startPositionMs);
    }

    @Override
    public void clearPreload() {
        session.clearPreload();
    }

    @Override
    public void stop() {
        preload.stop();
        player.stop();
    }

    @Override
    public String getErrorMessage(PlaybackException e) {
        return provider.get(e);
    }

    @Override
    public ErrorAction handleError(PlaybackException e) {
        return switch (e.errorCode) {
            case PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> seekToDefaultPosition();
            case PlaybackException.ERROR_CODE_DECODER_INIT_FAILED, PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED, PlaybackException.ERROR_CODE_DECODING_FAILED -> ErrorAction.DECODE;
            case PlaybackException.ERROR_CODE_IO_UNSPECIFIED, PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED, PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED, PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED, PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED -> retryFormat(e.errorCode);
            default -> ErrorAction.FATAL;
        };
    }

    private void startInternal(long position) {
        MediaItem item = MediaItemFactory.from(spec);
        MediaSource source = session.usePreloadedMediaSource(item);
        effect.clearAudioEffect();
        if (source == null) player.setMediaItem(item, position);
        else player.setMediaSource(source, position);
        preload.start(player, item);
        prepareAndPlay();
    }

    private void prepareAndPlay() {
        player.prepare();
        player.play();
    }

    private ErrorAction seekToDefaultPosition() {
        player.seekToDefaultPosition();
        player.prepare();
        return ErrorAction.RECOVERED;
    }

    private ErrorAction retryFormat(int errorCode) {
        spec.setFormat(ExoUtil.getMimeType(errorCode));
        startInternal(player.getCurrentPosition());
        return ErrorAction.RECOVERED;
    }
}
