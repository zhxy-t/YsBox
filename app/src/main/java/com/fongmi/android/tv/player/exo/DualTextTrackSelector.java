package com.fongmi.android.tv.player.exo;

import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Timeline;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.RendererCapabilities;
import androidx.media3.exoplayer.RendererConfiguration;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.DecodeTrackSelector;
import androidx.media3.exoplayer.trackselection.ExoTrackSelection;
import androidx.media3.exoplayer.trackselection.FixedTrackSelection;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelectorResult;
import androidx.media3.exoplayer.upstream.BandwidthMeter;

/** Adds a second text selection while retaining the normal decoder and primary track policy. */
final class DualTextTrackSelector extends TrackSelector {
    private final DecodeTrackSelector delegate;
    private volatile TrackSelectionOverride secondary;
    private volatile boolean automatic;

    DualTextTrackSelector(DecodeTrackSelector delegate) {
        this.delegate = delegate;
    }

    void select(TrackSelectionOverride selection, boolean auto) {
        secondary = selection;
        automatic = auto;
        invalidate();
    }

    @Override
    public void init(InvalidationListener listener, BandwidthMeter meter) {
        super.init(listener, meter);
        delegate.init(listener, meter);
    }

    @Override
    public TrackSelectorResult selectTracks(RendererCapabilities[] capabilities, TrackGroupArray groups,
                                            MediaSource.MediaPeriodId id, Timeline timeline) throws ExoPlaybackException {
        TrackSelectorResult original = delegate.selectTracks(capabilities, groups, id, timeline);
        int first = -1, second = -1;
        for (int i = 0; i < capabilities.length; i++) {
            if (capabilities[i].getTrackType() == C.TRACK_TYPE_TEXT) {
                if (first == -1) first = i;
                else second = i;
            }
        }
        if (second < 0) return original;
        RendererConfiguration[] configs = original.rendererConfigurations.clone();
        ExoTrackSelection[] selections = original.selections.clone();
        // The normal selector assigns text to the first equal-capability renderer.
        if (selections[first] == null && selections[second] != null) {
            selections[first] = selections[second];
            configs[first] = configs[second];
        }
        selections[second] = null;
        configs[second] = null;
        ExoTrackSelection primary = selections[first];
        TrackSelectionOverride requested = secondary;
        if (primary != null && !getParameters().disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)) {
            outer:
            for (int g = 0; g < groups.length; g++) {
                TrackGroup group = groups.get(g);
                if (group.type != C.TRACK_TYPE_TEXT) continue;
                for (int t = 0; t < group.length; t++) {
                    if (group.equals(primary.getTrackGroup()) && primary.indexOf(t) != C.INDEX_UNSET) continue;
                    if (RendererCapabilities.getFormatSupport(capabilities[second].supportsFormat(group.getFormat(t))) != C.FORMAT_HANDLED) continue;
                    boolean explicit = requested != null && requested.mediaTrackGroup.equals(group) && requested.trackIndices.contains(t);
                    boolean auto = requested == null && automatic && !java.util.Objects.equals(
                            group.getFormat(t).language, primary.getSelectedFormat().language);
                    if (explicit || auto) {
                        configs[second] = RendererConfiguration.DEFAULT;
                        selections[second] = new FixedTrackSelection(group, t);
                        break outer;
                    }
                }
            }
        }
        // Public Tracks describes primary selection. Secondary has its own explicit UI state.
        return new TrackSelectorResult(configs, selections, original.tracks, original.info);
    }

    @Override
    public void onSelectionActivated(Object info) {
        delegate.onSelectionActivated(info);
    }

    @Override
    public TrackSelectionParameters getParameters() {
        return delegate.getParameters();
    }

    @Override
    public void setParameters(TrackSelectionParameters parameters) {
        delegate.setParameters(parameters);
    }

    @Override
    public boolean isSetParametersSupported() {
        return true;
    }

    @Override
    public void setAudioAttributes(AudioAttributes audioAttributes) {
        delegate.setAudioAttributes(audioAttributes);
    }

    @Override
    public RendererCapabilities.Listener getRendererCapabilitiesListener() {
        return delegate.getRendererCapabilitiesListener();
    }

    @Override
    public void release() {
        delegate.release();
        super.release();
    }
}
