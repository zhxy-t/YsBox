package com.fongmi.android.tv.player.engine;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.ColorInfo;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.text.CueGroup;
import androidx.media3.ui.PlayerView;

import com.fongmi.android.tv.bean.Sub;
import com.fongmi.android.tv.player.effect.PlayerEffect;
import com.fongmi.android.tv.player.media.PlaySpec;

import java.util.List;
import java.util.function.Consumer;

public interface PlayerEngine {

    int SOFT = 0;
    int HARD = 1;

    Type getType();

    Player getPlayer();

    int getAudioChannelCount();

    void release();

    void setDecode(int decode);

    default PlayerEffect getEffect() {
        return PlayerEffect.NONE;
    }

    void start(PlaySpec spec, long startPositionMs);

    default void preload(PlaySpec spec, long startPositionMs) {
    }

    default void clearPreload() {
    }

    /** 下一集预下时长（毫秒）。短剧传整集时长，默认实现忽略。 */
    default void setNextEpisodePreloadDuration(long ms) {
    }

    default void bindPlayerView(PlayerView playerView) {
    }

    void stop();

    default void setSubtitleStyle() {
    }

    default void applySubtitleStyle() {
        setSubtitleStyle();
    }

    default void setSecondarySubtitleOutput(Consumer<CueGroup> output) {
    }

    default SecondarySubtitleState getSecondarySubtitleState() {
        return SecondarySubtitleState.EMPTY;
    }

    default void setSecondarySubtitleSelection(@Nullable TrackSelectionOverride selection) {
    }

    default boolean addSubtitle(Sub sub) {
        return false;
    }

    String getErrorMessage(PlaybackException e);

    ErrorAction handleError(PlaybackException e);

    /** 当前选中视频轨的源信息与实际解码/输出运行时事实。 */
    default VideoPlaybackDetails getVideoPlaybackDetails() {
        return VideoPlaybackDetails.empty();
    }

    record VideoPlaybackDetails(
            String sourceCodecs,
            int dolbyVisionProfile,
            int dolbyVisionLevel,
            String decodedCodec,
            String decoderName,
            String hwdecCurrent,
            @Nullable ColorInfo outputColorInfo,
            boolean dolbyVisionHdr10Fallback,
            boolean dolbyVisionP81Conversion) {

        public VideoPlaybackDetails(
                String sourceCodecs,
                int dolbyVisionProfile,
                int dolbyVisionLevel,
                String decodedCodec,
                String decoderName,
                String hwdecCurrent,
                ColorInfo outputColorInfo,
                boolean dolbyVisionHdr10Fallback) {
            this(sourceCodecs, dolbyVisionProfile, dolbyVisionLevel, decodedCodec,
                    decoderName, hwdecCurrent, outputColorInfo, dolbyVisionHdr10Fallback, false);
        }

        public VideoPlaybackDetails {
            sourceCodecs = sourceCodecs == null ? "" : sourceCodecs;
            decodedCodec = decodedCodec == null ? "" : decodedCodec;
            decoderName = decoderName == null ? "" : decoderName;
            hwdecCurrent = hwdecCurrent == null ? "" : hwdecCurrent;
        }

        public boolean hasDolbyVisionSource() {
            return dolbyVisionProfile > 0;
        }

        public static VideoPlaybackDetails empty() {
            return new VideoPlaybackDetails("", C.INDEX_UNSET, C.INDEX_UNSET,
                    "", "", "", null, false);
        }
    }

    enum ErrorAction {
        RECOVERED,
        DECODE,
        FATAL
    }

    enum Type {
        EXO
    }

    record SecondarySubtitleState(@Nullable TrackSelectionOverride primarySelection,
                                  @Nullable TrackSelectionOverride explicitSelection,
                                  List<TrackSelectionOverride> secondaryCandidates,
                                  boolean secondaryPromotedToPrimary) {

        public static final SecondarySubtitleState EMPTY = new SecondarySubtitleState(null, null, List.of(), false);

        public SecondarySubtitleState {
            secondaryCandidates = List.copyOf(secondaryCandidates);
        }
    }
}
