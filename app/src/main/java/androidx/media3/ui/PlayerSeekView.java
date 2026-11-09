package androidx.media3.ui;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.common.util.Util;
/**
 * Seek bar used by the VOD/Live control layouts.
 *
 * <p>Wraps a styled {@link DefaultTimeBar} (id {@code exo_progress}) with position/duration
 * labels at both ends, kept in sync with the bound {@link Player}. While the user is
 * scrubbing, the labels follow the thumb; after release they snap back to player time.
 * Live streams (no duration) only show the elapsed label.
 */
public class PlayerSeekView extends FrameLayout implements Player.Listener {
    private static final long UPDATE_INTERVAL_MS = 500L;
    private final DefaultTimeBar timeBar;
    private final TextView positionView;
    private final TextView durationView;
    private final StringBuilder formatBuilder = new StringBuilder();
    private final java.util.Formatter formatFormatter = new java.util.Formatter(formatBuilder, java.util.Locale.getDefault());
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean scrubbing;
    private long scrubPosition;
    private @Nullable Player player;
    private final Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            if (player != null && player.isPlaying()) handler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };
    public PlayerSeekView(Context context) {
        this(context, null);
    }
    public PlayerSeekView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public PlayerSeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        timeBar = new DefaultTimeBar(context, null, 0, attrs, R.style.ExoStyledControls_TimeBar);
        timeBar.setId(R.id.exo_progress);
        timeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(@NonNull TimeBar bar, long position) {
                scrubbing = true;
                scrubPosition = position;
                updateLabels();
            }
            @Override
            public void onScrubMove(@NonNull TimeBar bar, long position) {
                scrubbing = true;
                scrubPosition = position;
                updateLabels();
            }
            @Override
            public void onScrubStop(@NonNull TimeBar bar, long position, boolean canceled) {
                scrubbing = false;
                updateLabels();
            }
        });
        positionView = createTimeLabel(context);
        durationView = createTimeLabel(context);
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int gap = Math.round(context.getResources().getDisplayMetrics().density * 8);
        LinearLayout.LayoutParams startLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        startLp.gravity = Gravity.CENTER_VERTICAL;
        row.addView(positionView, startLp);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        barLp.setMarginStart(gap);
        barLp.setMarginEnd(gap);
        row.addView(timeBar, barLp);
        LinearLayout.LayoutParams endLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        endLp.gravity = Gravity.CENTER_VERTICAL;
        row.addView(durationView, endLp);
        addView(row, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
    private static TextView createTimeLabel(Context context) {
        TextView label = new TextView(context);
        label.setTextColor(0xFFFFFFFF);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        label.setFontFeatureSettings("tnum");
        label.setShadowLayer(8f, 0f, 1f, 0x99000000);
        label.setSingleLine(true);
        return label;
    }
    public void setPlayer(@Nullable Player player) {
        if (this.player == player) return;
        if (this.player != null) this.player.removeListener(this);
        this.player = player;
        if (player != null) player.addListener(this);
        restartUpdates();
    }
    public TimeBar getTimeBar() {
        return timeBar;
    }
    @Override
    public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
        restartUpdates();
    }
    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        restartUpdates();
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        restartUpdates();
    }
    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(updater);
        super.onDetachedFromWindow();
    }
    private void restartUpdates() {
        handler.removeCallbacks(updater);
        updateProgress();
        if (player != null && player.isPlaying()) handler.postDelayed(updater, UPDATE_INTERVAL_MS);
    }
    private void updateProgress() {
        Player p = player;
        if (p == null) {
            timeBar.setDuration(0);
            timeBar.setPosition(0);
            timeBar.setBufferedPosition(0);
            timeBar.setEnabled(false);
            updateLabels();
            return;
        }
        long duration = p.getDuration();
        boolean hasDuration = duration != C.TIME_UNSET && duration > 0;
        long position = Math.max(0L, p.getCurrentPosition());
        long buffered = Math.max(position, p.getBufferedPosition());
        timeBar.setDuration(hasDuration ? duration : 0);
        if (!scrubbing) timeBar.setPosition(hasDuration ? Math.min(position, duration) : 0);
        timeBar.setBufferedPosition(hasDuration ? Math.min(buffered, duration) : 0);
        timeBar.setEnabled(hasDuration && p.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM));
        updateLabels();
    }
    /** 两端时间：拖动时跟手，松手后回到播放器时间；无时长（直播）时隐藏右侧。 */
    private void updateLabels() {
        Player p = player;
        long duration = p == null ? C.TIME_UNSET : p.getDuration();
        long position = scrubbing ? Math.max(0L, scrubPosition) : (p == null ? 0L : Math.max(0L, p.getCurrentPosition()));
        positionView.setText(Util.getStringForTime(formatBuilder, formatFormatter, position));
        boolean hasDuration = duration != C.TIME_UNSET && duration > 0;
        durationView.setVisibility(hasDuration ? VISIBLE : GONE);
        if (hasDuration) durationView.setText(Util.getStringForTime(formatBuilder, formatFormatter, duration));
    }
}
