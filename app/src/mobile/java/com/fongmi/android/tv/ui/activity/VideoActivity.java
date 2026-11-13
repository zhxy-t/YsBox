package com.fongmi.android.tv.ui.activity;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.content.pm.ActivityInfo;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.C;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.ui.PlayerSeekView;
import androidx.media3.ui.PlayerView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.request.transition.Transition;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.DanmakuApi;
import com.fongmi.android.tv.api.SiteApi;
import com.fongmi.android.tv.api.hk.HkDetailBridge;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Danmaku;
import com.fongmi.android.tv.bean.Episode;
import com.fongmi.android.tv.bean.Flag;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.bean.Keep;
import com.fongmi.android.tv.bean.Parse;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Sub;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.ActivityVideoBinding;
import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.event.CastEvent;
import com.fongmi.android.tv.event.PlaySourceUpdateEvent;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.exception.ExtractException;
import com.fongmi.android.tv.impl.CustomTarget;
import com.fongmi.android.tv.model.VideoViewModel;
import com.fongmi.android.tv.playback.PlaybackAction;
import com.fongmi.android.tv.playback.PlaybackIntent;
import com.fongmi.android.tv.playback.PlaybackOrientation;
import com.fongmi.android.tv.playback.PlaybackReset;
import com.fongmi.android.tv.playback.PlaybackResult;
import com.fongmi.android.tv.playback.vod.VodDetailResult;
import com.fongmi.android.tv.playback.vod.VodPlayRequest;
import com.fongmi.android.tv.playback.vod.VodPlaybackController;
import com.fongmi.android.tv.playback.vod.VodPlaybackHost;
import com.fongmi.android.tv.playback.vod.VodPlaybackMedia;
import com.fongmi.android.tv.player.PlayerManager;
import com.fongmi.android.tv.player.media.PlaySpec;
import com.fongmi.android.tv.service.PlaybackService;
import com.fongmi.android.tv.setting.DanmakuSetting;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.setting.SpeedSetting;
import com.fongmi.android.tv.ui.adapter.EpisodeAdapter;
import com.fongmi.android.tv.ui.adapter.FlagAdapter;
import com.fongmi.android.tv.ui.adapter.QualityAdapter;
import com.fongmi.android.tv.ui.adapter.QuickAdapter;
import com.fongmi.android.tv.ui.base.ViewType;
import com.fongmi.android.tv.ui.custom.CustomKeyDown;
import com.fongmi.android.tv.ui.custom.CustomMovement;
import com.fongmi.android.tv.ui.custom.SpaceItemDecoration;
import com.fongmi.android.tv.ui.dialog.CastDialog;
import com.fongmi.android.tv.ui.dialog.ChapterDialog;
import com.fongmi.android.tv.ui.dialog.ControlDialog;
import com.fongmi.android.tv.ui.dialog.DanmakuDialog;
import com.fongmi.android.tv.ui.dialog.EditionDialog;
import com.fongmi.android.tv.ui.dialog.EpisodeGridDialog;
import com.fongmi.android.tv.ui.dialog.EpisodeListDialog;
import com.fongmi.android.tv.ui.dialog.InfoDialog;
import com.fongmi.android.tv.ui.dialog.ParseDialog;
import com.fongmi.android.tv.ui.dialog.QuickSearchDialog;
import com.fongmi.android.tv.ui.dialog.PlayerEngineDialog;
import com.fongmi.android.tv.ui.dialog.ReceiveDialog;
import com.fongmi.android.tv.ui.dialog.SpeedSettingDialog;
import com.fongmi.android.tv.ui.dialog.TrackDialog;
import com.fongmi.android.tv.utils.Clock;
import com.fongmi.android.tv.utils.FileChooser;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.PiP;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Sniffer;
import com.fongmi.android.tv.utils.Timer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.fongmi.android.tv.utils.Task;
import com.fongmi.android.tv.utils.Traffic;
import com.fongmi.android.tv.utils.UrlUtil;
import com.fongmi.android.tv.utils.Util;
import com.fongmi.android.tv.utils.download.DownloadManager;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.MoreExecutors;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VideoActivity extends PlaybackActivity implements Clock.Callback, CustomKeyDown.Listener, ControlDialog.Listener, ParseDialog.Listener, VodPlaybackHost, FlagAdapter.OnClickListener, EpisodeAdapter.OnClickListener, QualityAdapter.OnClickListener, QuickAdapter.OnClickListener, CastDialog.Listener, InfoDialog.Listener {

    private ActivityVideoBinding mBinding;
    private VideoViewModel mViewModel;
    private VodPlaybackController mVod;
    private FlagAdapter mFlagAdapter;
    private EpisodeAdapter mEpisodeAdapter;
    private QualityAdapter mQualityAdapter;
    private QuickAdapter mQuickAdapter;
    private QuickSearchDialog mQuickSearchDialog;
    private String mQuickSearchKeyword;
    private ViewGroup.LayoutParams mFrameParams;
    private ValueAnimator mAnimator;
    private CustomKeyDown mKeyDown;
    private Clock mClock;
    private PiP mPiP;
    private Runnable mR1;
    private Runnable mR2;
    private Runnable mR3;
    private Runnable mR4;
    private History mHistory;
    private boolean fullscreen;
    /** 短剧模式：进页即全屏，详情到达后自动起播（与普通视频同一套播放链，仅省掉详情停留）。 */
    private boolean mShortDramaMode;
    /** 选集直达在 Service 未就绪时挂起，连接完成后再跑（对齐 xingchen 的 deferPlayRequest）。 */
    private boolean mPendingHkDirect;

    /**
     * 播放器是否存活（对齐海阔 8.83 PlayerChooser.hasPlayer）：为 true 时小程序点播放
     * 走 EventBus 原地换源、不新开页面；多实例交叠时只有属主实例的 onDestroy 会清标志。
     */
    public static volatile boolean hasPlayer = false;
    private static volatile VideoActivity sPlayerOwner;

    /** 超大选集串的进程内停放仓（Intent 只带 ref，避免 Binder 事务超限）。 */
    private static final int HK_EPISODES_INLINE_MAX = 100_000;
    private static final java.util.concurrent.atomic.AtomicInteger HK_EPISODES_SEQ = new java.util.concurrent.atomic.AtomicInteger();
    private static final java.util.Map<String, String> HK_EPISODES_STORE =
            java.util.Collections.synchronizedMap(new java.util.LinkedHashMap<String, String>(8, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, String> eldest) {
                    return size() > 4;
                }
            });

    private static void parkHkEpisodes(String ref, String json) {
        HK_EPISODES_STORE.put(ref, json);
    }

    // ===== 短剧专用播放 UI（抖音式竖屏播放器）=====

    /** 短剧 UI 根层；非短剧模式恒为 null，所有短剧逻辑以它为开关。 */
    private View mShortUi;
    private View mSdBottom;
    private android.widget.ImageView mSdPrev;
    private android.widget.ImageView mSdNext;
    private TextView mSdPrevLabel;
    private TextView mSdNextLabel;
    private TextView mSdTitle;
    private TextView mSdEpisode;
    private TextView mSdSpeed;
    private TextView mSdEpisodes;
    private TextView mSdShare;
    private TextView mSdPlayHint;
    private android.widget.ProgressBar mSdProgress;
    private android.widget.ImageView mSdKeep;
    private android.widget.ImageView mSdLock;
    /** 短剧倍速档位：1.0 → 1.5 → 2.0 → 1.0。 */
    private static final float[] SD_SPEEDS = {1.0f, 1.5f, 2.0f};
    /** 短剧单集时长上限：短剧单集普遍 1~3 分钟，放宽到 8 分钟以覆盖横屏短剧。 */
    private static final long SHORT_DRAMA_MAX_EPISODE_MS = 8 * 60 * 1000L;
    /** 横屏短片要判成短剧，至少要有这么多集（防预告片/花絮被误套短剧 UI）。 */
    private int mSdSpeedIndex;
    private boolean useParse;
    private boolean rotate;
    private final java.text.SimpleDateFormat mOsdClock = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault());
    private long mOsdRxBytes;
    private long mOsdRxTime;
    private String mOsdSpeed = "0 B/s";

    public static void push(FragmentActivity activity, String text) {
        Uri uri = UrlUtil.uri(text);
        if (FileChooser.isValid(activity, uri)) file(activity, FileChooser.getPathFromUri(uri));
        else start(activity, Sniffer.getUrl(text));
    }

    public static void file(FragmentActivity activity, String path) {
        if (TextUtils.isEmpty(path)) return;
        String name = new File(path).getName();
        start(activity, SiteApi.PUSH, "file://" + path, name);
    }

    /** Compatibility overload for dialogs/callers that still pass a Uri. */
    public static void file(FragmentActivity activity, Uri fileUri) {
        if (fileUri == null || activity.isFinishing() || activity.isDestroyed()) return;
        start(activity, SiteApi.PUSH, fileUri.toString(), FileUtil.getDisplayName(fileUri));
    }

    public static void cast(Activity activity, History history) {
        start(activity, history.getSiteKey(), history.getVodId(), history.getVodName(), history.getVodPic());
    }

    public static void collect(Activity activity, String key, String id, String name, String pic) {
        start(activity, key, id, name, pic, null, true);
    }

    public static void collect(Activity activity, String key, String id, String name, String pic, String wallPic) {
        start(activity, key, id, name, pic, null, true);
    }

    public static void start(Activity activity, String url) {
        start(activity, SiteApi.PUSH, url, url);
    }

    public static void start(Activity activity, String key, String id, String name) {
        start(activity, key, id, name, null);
    }

    public static void start(Activity activity, String key, String id, String name, String pic) {
        start(activity, key, id, name, pic, null);
    }

    public static void start(Activity activity, String key, String id, String name, String pic, String mark) {
        start(activity, key, id, name, pic, mark, false);
    }

    public static void start(Activity activity, String key, String id, String name, String pic, String mark, boolean collect) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra("collect", collect);
        intent.putExtra("mark", mark);
        intent.putExtra("name", name);
        intent.putExtra("pic", pic);
        intent.putExtra("key", key);
        intent.putExtra("id", id);
        activity.startActivity(intent);
    }

    /** 短剧入口：进播放器后直接全屏播放（不闪详情页），识别规则见 ShortDramaHelper。 */
    public static void startShortDrama(Activity activity, String key, String id, String name, String pic) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("pic", pic);
        intent.putExtra("key", key);
        intent.putExtra("id", id);
        intent.putExtra("short_drama", true);
        activity.startActivity(intent);
    }

    /** 海阔分支入口：单页点条目打开详情页（key 命名空间 hk_&lt;规则名&gt;）。 */
    public static void startHk(Activity activity, String ruleTitle, String itemUrl, String name, String pic) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra("key", HkDetailBridge.siteKey(ruleTitle));
        intent.putExtra("id", itemUrl);
        intent.putExtra("name", name);
        intent.putExtra("pic", pic);
        intent.putExtra("hk_rule", ruleTitle);
        intent.putExtra("hk_item_url", itemUrl);
        activity.startActivity(intent);
    }

    /** 海阔选集直达播放：跳过详情页，播放链走 SiteApi.playerContent 的 hk_ 拦截。 */
    public static void startHkPlay(Activity activity, String ruleTitle, String lineName, String episodeUrl, String episodeName, String name, String pic) {
        startHkPlay(activity, ruleTitle, lineName, episodeUrl, episodeName, name, pic, "", 0);
    }

    public static void startHkPlay(Activity activity, String ruleTitle, String lineName, String episodeUrl, String episodeName, String name, String pic, String episodesJson, int selIdx) {
        startHkPlay(activity, ruleTitle, lineName, episodeUrl, episodeName, name, pic, episodesJson, selIdx, false);
    }

    /** 海阔选集直达播放，额外带短剧模式标记。 */
    public static void startHkPlay(Activity activity, String ruleTitle, String lineName, String episodeUrl, String episodeName, String name, String pic, String episodesJson, int selIdx, boolean shortDrama) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra("key", HkDetailBridge.siteKey(ruleTitle));
        intent.putExtra("id", episodeUrl);
        intent.putExtra("name", name);
        intent.putExtra("pic", pic);
        intent.putExtra("hk_rule", ruleTitle);
        intent.putExtra("hk_line", lineName);
        intent.putExtra("hk_episode", episodeName);
        intent.putExtra("hk_direct_play", true);
        intent.putExtra("short_drama", shortDrama);
        // 选集串过大会把 startActivity 的 Binder 事务撑爆：大的先停进程内仓、Intent 只带凭据。
        if (episodesJson != null && episodesJson.length() > HK_EPISODES_INLINE_MAX) {
            String ref = "e" + HK_EPISODES_SEQ.incrementAndGet();
            parkHkEpisodes(ref, episodesJson);
            intent.putExtra("hk_episodes", "");
            intent.putExtra("hk_episodes_ref", ref);
        } else {
            intent.putExtra("hk_episodes", episodesJson == null ? "" : episodesJson);
        }
        intent.putExtra("hk_episode_idx", selIdx);
        // 播放器已存活：原地换源，不新开页面（对齐海阔 8.83 PlayerChooser.hasPlayer）
        if (hasPlayer) {
            PlaySourceUpdateEvent.post(activity, intent);
            return;
        }
        activity.startActivity(intent);
    }

    private String getName() {
        return Objects.toString(getIntent().getStringExtra("name"), "");
    }

    private String getPic() {
        return Objects.toString(getIntent().getStringExtra("pic"), "");
    }

    private String getMark() {
        return Objects.toString(getIntent().getStringExtra("mark"), "");
    }

    private String getKey() {
        return Objects.toString(getIntent().getStringExtra("key"), "");
    }

    private String getId() {
        return Objects.toString(getIntent().getStringExtra("id"), "");
    }

    private boolean isSameVideo(Intent intent) {
        String key = Objects.toString(intent.getStringExtra("key"), "");
        String id = Objects.toString(intent.getStringExtra("id"), "");
        return TextUtils.equals(key, getKey()) && TextUtils.equals(id, getId());
    }

    @Override
    public String getHistoryKey() {
        return getKey().concat(AppDatabase.SYMBOL).concat(getId()).concat(AppDatabase.SYMBOL) + VodConfig.getCid();
    }

    private Site getSite() {
        return VodConfig.get().getSite(getKey());
    }

    private int getScale() {
        return mHistory != null && mHistory.getScale() != -1 ? mHistory.getScale() : PlayerSetting.getScale();
    }

    private void setScale(int scale) {
        mVod.setScale(scale);
        mBinding.player.setResizeMode(scale);
        mBinding.control.action.scale.setText(ResUtil.getStringArray(R.array.select_scale)[scale]);
    }

    @Override
    public boolean isFromCollect() {
        return getIntent().getBooleanExtra("collect", false);
    }

    private boolean isAutoRotate() {
        return Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
    }

    private boolean isLand() {
        return mBinding.getRoot().getTag().equals("land");
    }

    private boolean isPort() {
        return mBinding.getRoot().getTag().equals("port");
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityVideoBinding.inflate(getLayoutInflater());
    }

    /** 详情卡片与线路胶囊同受「胶囊背景」设置控制：同源底色/描边/圆角，
        切换设置后 RefreshEvent.theme() 重建 Activity 即自动刷新。 */
    private void applyCardSurface() {
        int normal = com.fongmi.android.tv.ui.dialog.ChipThemeHelper.cardColor();
        int stroke = com.fongmi.android.tv.ui.dialog.ChipThemeHelper.cardStroke();
        int px = Math.max(1, Math.round(getResources().getDisplayMetrics().density));
        mBinding.hero.setCardBackgroundColor(normal);
        mBinding.hero.setStrokeColor(stroke);
        mBinding.hero.setStrokeWidth(px);
        mBinding.funcBar.setBackground(com.fongmi.android.tv.ui.dialog.ChipThemeHelper.background(this, 16f));
    }

    @Override
    protected PlaybackService.NavigationCallback getNavigationCallback() {
        return mNavigationCallback;
    }

    @Override
    protected PlayerView getPlayerView() {
        return mBinding.player;
    }

    @Override
    protected PlayerSeekView getSeekView() {
        return mBinding.control.seek;
    }

    @Override
    protected void onServiceConnected() {
        mVod.onPlaybackServiceReady();
        checkLand();
        if (mShortDramaMode && !isFullscreen()) enterFullscreen();
        // onCreate 阶段 player 可能还没建，预下配置要到服务就绪后再落一次
        applyShortDramaPreload();
        if (mPendingHkDirect) {
            mPendingHkDirect = false;
            playHkDirect();
        }
    }

    /** 播放器已存活时的原地换源：收到小程序播放请求就直接在本实例重跑播放链。 */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaySourceUpdated(PlaySourceUpdateEvent event) {
        if (isFinishing() || isDestroyed()) return;
        Intent intent = event == null ? null : event.intent();
        if (intent == null) return;
        if (!TextUtils.equals(intent.getStringExtra("id"), getIntent().getStringExtra("id"))) {
            saveHistory(true);
            mVod.reset();
        }
        setIntent(intent);
        updateNavigationKey();
        checkControl();
        setOrient();
        checkId();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (TextUtils.isEmpty(intent.getStringExtra("id")) || isSameVideo(intent)) return;
        mBinding.swipeLayout.setRefreshing(true);
        saveHistory(true);
        mVod.reset();
        setIntent(intent);
        updateNavigationKey();
        checkControl();
        setOrient();
        checkId();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> setStatusBar(insets));
        mKeyDown = CustomKeyDown.create(this, mBinding.player);
        mFrameParams = mBinding.video.getLayoutParams();
        mBinding.progressLayout.showProgress();
        mBinding.swipeLayout.setEnabled(false);
        mClock = Clock.create();
        mR1 = this::hideControl;
        mR2 = this::setTraffic;
        mR3 = this::setOrient;
        mR4 = this::showEmpty;
        mPiP = new PiP();
        checkDanmakuImg();
        setRecyclerView();
        setVideoView();
        applyCardSurface();
        setViewModel();
        showProgress();
        setAnimator();
        checkId();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initEvent() {
        mBinding.name.setOnClickListener(view -> onName());
        mBinding.name.setSelected(true);
        mBinding.download.setOnClickListener(view -> onDownload());
        mBinding.more.setOnClickListener(view -> onMore());
        mBinding.actor.setOnClickListener(view -> onActor());
        mBinding.content.setOnClickListener(view -> onContent());
        mBinding.remark.setOnClickListener(view -> mBinding.remark.setMaxLines(mBinding.remark.getMaxLines() == 1 ? Integer.MAX_VALUE : 1));
        mBinding.reverse.setOnClickListener(view -> onReverse());
        mBinding.director.setOnClickListener(view -> onDirector());
        mBinding.name.setOnLongClickListener(view -> onChange());
        mBinding.content.setOnLongClickListener(view -> onCopy());
        mBinding.control.back.setOnClickListener(view -> onBack());
        mBinding.cast.setOnClickListener(view -> onCast());
        mBinding.funcAudio.setTag("1");
        mBinding.funcAudio.setOnClickListener(this::onTrack);
        mBinding.funcRotate.setOnClickListener(view -> onRotate());
        mBinding.info.setOnClickListener(view -> onInfo());
        mBinding.keep.setOnClickListener(view -> onKeep());
        mBinding.control.play.setOnClickListener(view -> checkPlay());
        mBinding.control.next.setOnClickListener(view -> checkNext());
        mBinding.control.prev.setOnClickListener(view -> checkPrev());
        mBinding.setting.setOnClickListener(view -> onSetting());
        mBinding.control.title.setOnLongClickListener(view -> onChange());
        mBinding.control.title.setSelected(true);
        mBinding.control.right.lock.setOnClickListener(view -> onLock());
        mBinding.control.right.rotate.setOnClickListener(view -> onRotate());
        mBinding.danmakuToggle.setOnClickListener(view -> onDanmakuShow());
        mBinding.control.action.text.setOnClickListener(this::onTrack);
        mBinding.control.action.audio.setOnClickListener(this::onTrack);
        mBinding.control.action.video.setOnClickListener(this::onTrack);
        mBinding.control.action.scale.setOnClickListener(view -> onScale());
        mBinding.control.action.speed.setOnClickListener(view -> onSpeed());
        mBinding.control.action.reset.setOnClickListener(view -> onReset());
        mBinding.control.action.replay.setOnClickListener(view -> onReplay());
        mBinding.control.action.parse.setOnClickListener(view -> onParse());
        mBinding.control.action.player.setOnClickListener(view -> onPlayer());
        mBinding.control.action.decode.setOnClickListener(view -> onDecode());
        mBinding.control.action.ending.setOnClickListener(view -> onEnding());
        mBinding.control.action.repeat.setOnClickListener(view -> onRepeat());
        mBinding.control.action.opening.setOnClickListener(view -> onOpening());
        mBinding.control.action.danmaku.setOnClickListener(view -> onDanmaku());
        mBinding.control.action.edition.setOnClickListener(view -> onEdition());
        mBinding.control.action.chapter.setOnClickListener(view -> onChapter());
        mBinding.control.action.episodes.setOnClickListener(view -> onEpisodes());
        mBinding.control.action.speed.setOnLongClickListener(view -> onSpeedLong());
        mBinding.control.action.ending.setOnLongClickListener(view -> onEndingReset());
        mBinding.control.action.opening.setOnLongClickListener(view -> onOpeningReset());
        mBinding.video.setOnTouchListener((view, event) -> mKeyDown.onTouchEvent(event));
        mBinding.control.action.getRoot().setOnTouchListener(this::onActionTouch);
        mBinding.swipeLayout.setOnRefreshListener(this::onSwipeRefresh);
    }

    private WindowInsetsCompat setStatusBar(WindowInsetsCompat insets) {
        int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
        ViewGroup.LayoutParams lp = mBinding.statusBar.getLayoutParams();
        lp.height = top;
        mBinding.statusBar.setLayoutParams(lp);
        return insets;
    }

    private void setRecyclerView() {
        mBinding.flag.setHasFixedSize(true);
        mBinding.flag.setItemAnimator(null);
        mBinding.flag.addItemDecoration(new SpaceItemDecoration(8));
        mBinding.flag.setAdapter(mFlagAdapter = new FlagAdapter(this).detailMode());
        mBinding.quick.setAdapter(mQuickAdapter = new QuickAdapter(this));
        // 选集：3 列网格，胶囊宽度按文字自适应并格内居中（长度随文字变化，超长跑马灯）
        mBinding.episode.setHasFixedSize(false);
        mBinding.episode.setItemAnimator(null);
        com.google.android.flexbox.FlexboxLayoutManager flm = new com.google.android.flexbox.FlexboxLayoutManager(this);
        flm.setFlexDirection(com.google.android.flexbox.FlexDirection.ROW);
        flm.setFlexWrap(com.google.android.flexbox.FlexWrap.WRAP);
        flm.setJustifyContent(com.google.android.flexbox.JustifyContent.FLEX_START);
        flm.setAlignItems(com.google.android.flexbox.AlignItems.CENTER);
        mBinding.episode.setLayoutManager(flm);
        mBinding.episode.setAdapter(mEpisodeAdapter = new EpisodeAdapter(this, ViewType.GRID).detailMode());
        mBinding.quality.setHasFixedSize(true);
        mBinding.quality.setItemAnimator(null);
        mBinding.quality.addItemDecoration(new SpaceItemDecoration(8));
        mBinding.quality.setAdapter(mQualityAdapter = new QualityAdapter(this).detailMode());
    }

    private void setVideoView() {
        PlayerEngineDialog.setText(mBinding.control.action.player);
        mBinding.control.action.danmaku.setVisibility(DanmakuSetting.isLoad() ? View.VISIBLE : View.GONE);
        mBinding.video.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> mPiP.update(this, view));
    }

    private void setVideoView(boolean isInPictureInPictureMode) {
        if (isInPictureInPictureMode) {
            mBinding.osd.setVisibility(View.GONE);
            mBinding.video.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        } else {
            mBinding.video.setLayoutParams(mFrameParams);
        }
    }

    private void setAnimator() {
        mAnimator = new ValueAnimator();
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addUpdateListener(animation -> {
            if (isLand() || isFullscreen() || isInPictureInPictureMode()) return;
            mFrameParams.height = (int) animation.getAnimatedValue();
            mBinding.video.setLayoutParams(mFrameParams);
        });
    }

    private void setPlaybackMode() {
        PlaybackAction.setPlaybackMode(player(), mBinding.control.action.player, mBinding.control.action.decode);
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        mVod = mViewModel.createPlaybackController(this);
        observeWhenServiceReady(mViewModel.getDetail(), this::onDetailObserved);
        observeWhenServiceReady(mViewModel.getSearch(), this::onSearchObserved);
        observeWhenServiceReady(mViewModel.getPreload(), this::onPreloadObserved);
        observeWhenServiceReady(mViewModel.getPlayback(), this::onPlaybackObserved);
    }

    private void onDetailObserved(VodDetailResult result) {
        mVod.onDetailResult(result);
    }

    private void onPlaybackObserved(PlaybackResult<VodPlayRequest> result) {
        mVod.onPlaybackResult(result);
    }

    private void onPreloadObserved(PlaybackResult<VodPlayRequest> result) {
        mVod.onPreloadResult(result);
    }

    private void onSearchObserved(Result result) {
        mVod.onSearchResult(result);
    }

    @Override
    public String getVodKey() {
        return getKey();
    }

    @Override
    public String getVodId() {
        return getId();
    }

    @Override
    public void setVodId(String id) {
        getIntent().putExtra("id", id);
    }

    @Override
    public String getVodName() {
        String name = mBinding.name.getText().toString();
        return name.isEmpty() ? getName() : name;
    }

    @Override
    public String getVodPic() {
        return getPic();
    }

    @Override
    public String getVodMark() {
        return getMark();
    }

    @Override
    public boolean isSiteChangeable() {
        return getSite().isChangeable();
    }

    @Override
    public boolean isHostFinishing() {
        return isFinishing() || isDestroyed();
    }

    @Override
    public boolean isPlayerEmpty() {
        PlayerManager player = player();
        return player == null || player.isEmpty();
    }

    @Override
    public boolean hasPlaybackSession() {
        return service() != null && isOwner() && player().hasPlaySpec();
    }

    @Override
    public boolean isFullscreenForPlayback() {
        return isFullscreen();
    }

    @Override
    public boolean isLivePlayback() {
        return service() != null && isOwner() && player().isLive();
    }

    @Override
    public boolean canTrackPlaybackProgress() {
        return service() != null && isOwner() && player().isVod();
    }

    @Override
    public boolean canPreloadNext() {
        if (service() == null || !isOwner()) return false;
        // 短剧模式强制开启下一集预加载（短集整集预下，收益极大）；
        // Prefers 开关绕开，引擎类型由 PlayerManager.preload() 内部二次守卫。
        if (mShortDramaMode) return player() != null;
        return player().canPreloadNext();
    }

    // ===== 短剧专用播放器：竖滑翻页（上一集/下一集）=====

    @Override
    public boolean isPageSwipeEnabled() {
        return mShortDramaMode && isFullscreen();
    }

    @Override
    public boolean canPageUp() {
        // 上划 = 下一集（抖音式）：需要存在下一集
        return mEpisodeAdapter != null
                && mEpisodeAdapter.getPosition() < mEpisodeAdapter.getItemCount() - 1;
    }

    @Override
    public boolean canPageDown() {
        // 下划 = 上一集（抖音式）：需要存在上一集
        return mEpisodeAdapter != null && mEpisodeAdapter.getPosition() > 0;
    }

    /** 短剧翻页：上划（up=true）下一集，下划（up=false）上一集（抖音式）。 */
    @Override
    public void onPageCommit(boolean up) {
        if (up) mVod.nextEpisode(true);
        else mVod.prevEpisode(true);
        if (mBinding != null) mBinding.player.setTranslationY(0f);
    }

    /** 翻页拖动：播放器跟手位移，上下同时露出相邻集封面预览。 */
    @Override
    public void onPageDrag(float dy) {
        if (mShortUi == null) return;
        int height = mBinding.video.getHeight();
        if (height <= 0) return;
        mBinding.player.setTranslationY(dy);
        float alpha = Math.min(0.92f, Math.abs(dy) / (float) height);
        boolean down = dy > 0;
        // 下划 = 下一集（canPageDown = 有下一集）；上划 = 上一集（canPageUp = 有上一集）
        if (down && canPageDown()) {
            mSdPrev.setVisibility(View.VISIBLE);
            mSdPrev.setTranslationY(dy - height);
            mSdPrev.setAlpha(alpha);
            showSdPreviewLabel(mSdPrevLabel, shortAdjacentName(1), alpha);
        } else {
            mSdPrev.setVisibility(View.GONE);
            hideSdPreviewLabel(mSdPrevLabel);
        }
        if (!down && canPageUp()) {
            mSdNext.setVisibility(View.VISIBLE);
            mSdNext.setTranslationY(dy + height);
            mSdNext.setAlpha(alpha);
            showSdPreviewLabel(mSdNextLabel, shortAdjacentName(-1), alpha);
        } else {
            mSdNext.setVisibility(View.GONE);
            hideSdPreviewLabel(mSdNextLabel);
        }
    }

    private void showSdPreviewLabel(TextView label, String name, float alpha) {
        if (label == null) return;
        if (TextUtils.isEmpty(name)) {
            label.setVisibility(View.GONE);
            return;
        }
        label.setText(name);
        label.setVisibility(View.VISIBLE);
        label.setAlpha(Math.min(1f, alpha * 1.4f));
    }

    private void hideSdPreviewLabel(TextView label) {
        if (label != null) label.setVisibility(View.GONE);
    }

    /** 翻页手势未切集时回弹到 0，宿主播放器 translationY 复位。 */
    @Override
    public void onPageDragEnd(boolean committed) {
        if (mBinding != null) mBinding.player.setTranslationY(0f);
        if (mSdPrev != null) mSdPrev.setVisibility(View.GONE);
        if (mSdNext != null) mSdNext.setVisibility(View.GONE);
        hideSdPreviewLabel(mSdPrevLabel);
        hideSdPreviewLabel(mSdNextLabel);
    }

    // ===== 短剧专用 UI：抖音式竖屏播放器 =====

    /** 挂载短剧专用 UI（只做一次）；非短剧模式直接返回。 */
    private void setupShortDramaOverlay() {
        if (!mShortDramaMode || mShortUi != null || mBinding == null) return;
        mShortUi = getLayoutInflater().inflate(R.layout.view_short_drama_ui, mBinding.video, false);
        int index = Math.min(1, mBinding.video.getChildCount());
        mBinding.video.addView(mShortUi, index, new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mSdBottom = mShortUi.findViewById(R.id.sdBottom);
        mSdPrev = mShortUi.findViewById(R.id.sdPreviewPrev);
        mSdNext = mShortUi.findViewById(R.id.sdPreviewNext);
        mSdPrevLabel = mShortUi.findViewById(R.id.sdPreviewPrevLabel);
        mSdNextLabel = mShortUi.findViewById(R.id.sdPreviewNextLabel);
        mSdTitle = mShortUi.findViewById(R.id.sdTitle);
        mSdEpisode = mShortUi.findViewById(R.id.sdEpisode);
        mSdSpeed = mShortUi.findViewById(R.id.sdSpeed);
        mSdEpisodes = mShortUi.findViewById(R.id.sdEpisodes);
        mSdShare = mShortUi.findViewById(R.id.sdShare);
        mSdPlayHint = mShortUi.findViewById(R.id.sdPlayHint);
        mSdProgress = mShortUi.findViewById(R.id.sdProgress);
        mSdKeep = mShortUi.findViewById(R.id.sdKeep);
        mSdLock = mShortUi.findViewById(R.id.sdLock);
        mSdEpisodes.setOnClickListener(v -> onMore());
        mSdSpeed.setOnClickListener(v -> cycleShortSpeed());
        mSdKeep.setOnClickListener(v -> onKeep());
        mSdShare.setOnClickListener(v -> shareShortDrama());
        mSdLock.setOnClickListener(v -> onLock());
        // 横屏那套控制层在短剧模式永不出现
        mBinding.control.getRoot().setVisibility(View.GONE);
        // 短剧整集预下：下一集按整集时长预加载，切集不再等转圈
        applyShortDramaPreload();
        checkKeepImg();
        updateShortDramaLockUi();
        refreshShortDramaInfo();
    }

    /**
     * 下一集整集预缓存：短剧单集通常 1~3 分钟，按整集时长预下（30 分钟足够覆盖），
     * 切集直接命中缓存不转圈。onCreate 阶段播放服务可能未就绪（player 为 null），
     * 由 onServiceConnected 再落一次，保证配置不丢。
     */
    private void applyShortDramaPreload() {
        if (!mShortDramaMode) return;
        PlayerManager player = player();
        if (player != null) player.setNextEpisodePreloadDuration(30 * 60 * 1000L);
    }

    /** 短剧分享：直接分享当前集地址。 */
    private void shareShortDrama() {
        PlayerManager player = player();
        if (player == null || TextUtils.isEmpty(player.getUrl())) return;
        onShare(getName(), player.getUrl(), player.getHeaders());
    }

    /** 锁定态：只留解锁按钮，其余短剧 UI 全部收起（抖音式防误触）。 */
    private void updateShortDramaLockUi() {
        if (mShortUi == null) return;
        boolean locked = isLock();
        if (mSdBottom != null) mSdBottom.setVisibility(locked ? View.GONE : View.VISIBLE);
        if (mSdEpisodes != null) mSdEpisodes.setVisibility(locked ? View.GONE : View.VISIBLE);
        if (mSdSpeed != null) mSdSpeed.setVisibility(locked ? View.GONE : View.VISIBLE);
        if (mSdKeep != null) mSdKeep.setVisibility(locked ? View.GONE : View.VISIBLE);
        if (mSdShare != null) mSdShare.setVisibility(locked ? View.GONE : View.VISIBLE);
        if (mSdLock != null) {
            mSdLock.setVisibility(View.VISIBLE);
            mSdLock.setImageResource(locked ? R.drawable.ic_control_lock_on : R.drawable.ic_control_lock_off);
        }
    }

    /** 相邻集名称（offset=-1 上一集，+1 下一集），越界返回空串。 */
    private String shortAdjacentName(int offset) {
        if (mEpisodeAdapter == null) return "";
        java.util.List<Episode> items = mEpisodeAdapter.getItems();
        int position = mEpisodeAdapter.getPosition() + offset;
        if (position < 0 || position >= items.size()) return "";
        String name = items.get(position).getName();
        return TextUtils.isEmpty(name) ? "" : name;
    }

    /** 短剧细进度条：跟随播放位置刷新。 */
    private void updateShortProgress() {
        PlayerManager player = player();
        if (mSdProgress == null || player == null) return;
        long position = Math.max(0, player.getPosition());
        long duration = player.getDuration();
        mSdProgress.setProgress(duration <= 0 ? 0 : (int) Math.min(1000, position * 1000 / duration));
    }

    /** 倍速档位轮转 1.0x → 1.5x → 2.0x。 */
    private void cycleShortSpeed() {
        if (player() == null || player().isEmpty()) return;
        mSdSpeedIndex = (mSdSpeedIndex + 1) % SD_SPEEDS.length;
        float speed = SD_SPEEDS[mSdSpeedIndex];
        player().setSpeed(speed);
        mSdSpeed.setText(String.format(java.util.Locale.ROOT, "%.1fx", speed));
    }

    /** 标题、集数、预览封面随选集变化刷新。 */
    private void refreshShortDramaInfo() {
        if (mShortUi == null) return;
        String title = getName();
        if (TextUtils.isEmpty(title) && mBinding.name.getText() != null) {
            title = mBinding.name.getText().toString();
        }
        mSdTitle.setText(title);
        int count = mEpisodeAdapter == null ? 0 : mEpisodeAdapter.getItemCount();
        int pos = mEpisodeAdapter == null ? -1 : mEpisodeAdapter.getPosition();
        StringBuilder line = new StringBuilder();
        if (count > 0 && pos >= 0) {
            line.append("第 ").append(pos + 1).append(" 集 · 共 ").append(count).append(" 集");
        }
        mSdEpisode.setText(line);
        mSdSpeed.setText(String.format(java.util.Locale.ROOT, "%.1fx", SD_SPEEDS[mSdSpeedIndex]));
        updateShortProgress();
        String pic = getPic();
        if (!TextUtils.isEmpty(pic)) {
            try {
                com.bumptech.glide.Glide.with(this).load(pic).into(mSdPrev);
                com.bumptech.glide.Glide.with(this).load(pic).into(mSdNext);
            } catch (Throwable ignored) {
            }
        }
    }

    /** 点按画面：播放/暂停，并闪一下对应图标（抖音同款交互）。 */
    private void toggleShortPlayback() {
        if (player() == null) return;
        if (player().isEmpty()) {
            checkPlay();
            return;
        }
        if (player().isPlaying()) {
            player().pause();
            flashShortPlayHint("▶");
        } else {
            player().play();
            flashShortPlayHint("❚❚");
        }
    }

    private void flashShortPlayHint(String icon) {
        if (mSdPlayHint == null) return;
        mSdPlayHint.setText(icon);
        mSdPlayHint.animate().cancel();
        mSdPlayHint.setAlpha(1f);
        mSdPlayHint.animate().alpha(0f).setDuration(700).setStartDelay(200).start();
    }

    @Override
    public long getPlayerPosition() {
        PlayerManager player = player();
        return player == null ? 0 : player.getPosition();
    }

    @Override
    public long getPlayerDuration() {
        PlayerManager player = player();
        return player == null ? 0 : player.getDuration();
    }

    @Override
    public void usePushId(String id) {
        getIntent().putExtra("key", SiteApi.PUSH).putExtra("id", id);
    }

    @Override
    public void requestDetail(String key, String id) {
        mViewModel.detailContent(key, id);
    }

    @Override
    public void requestPlayer(VodPlayRequest request) {
        mViewModel.playerContent(request);
        showProgress();
    }

    @Override
    public void requestPreload(VodPlayRequest request) {
        mViewModel.preloadContent(request);
    }

    @Override
    public void requestSearch(List<Site> sites, String keyword) {
        mQuickAdapter.clear();
        mViewModel.searchContent(sites, keyword, true);
    }

    @Override
    public void prepareSource(Vod item) {
        getIntent().putExtra("key", item.getSiteKey());
        getIntent().putExtra("pic", item.getPic());
        getIntent().putExtra("id", item.getId());
        mBinding.swipeLayout.setRefreshing(true);
        mBinding.swipeLayout.setEnabled(false);
        mBinding.scroll.scrollTo(0, 0);
        mClock.setCallback(null);
        updateNavigationKey();
        PlayerManager player = player();
        if (player != null) {
            player.reset();
            player.stop();
        }
        checkControl();
    }

    @Override
    public void stopPlaybackForRefresh() {
        PlayerManager player = player();
        if (player != null) {
            player.stop();
            player.clear();
        }
        mClock.setCallback(null);
        checkControl();
    }

    @Override
    public void resetPlaybackForError(String msg) {
        mBinding.swipeLayout.setEnabled(true);
        PlayerManager player = player();
        if (player != null) PlaybackReset.afterError(player, () -> mClock.setCallback(null));
        showError(msg);
    }

    @Override
    public void replay(long position) {
        PlayerManager player = player();
        if (player != null) player.replay(position);
    }

    @Override
    public void startPlayback(Result result, boolean useParse, long startPositionMs, MediaMetadata metadata) {
        startPlayer(getHistoryKey(), result, useParse, getSite().getTimeout(), startPositionMs, metadata);
    }

    @Override
    public boolean preloadPlayback(Result result, long startPositionMs, MediaMetadata metadata) {
        PlayerManager player = player();
        return player != null && player.preload(PlaySpec.from(result, getHistoryKey(), metadata), startPositionMs);
    }

    @Override
    public void clearPreload() {
        if (service() != null && isOwner()) player().clearPreload();
    }

    @Override
    public void loadDanmaku(Result result, History history, Episode episode) {
        PlayerManager player = player();
        if (player == null) return;
        VodPlaybackMedia.searchDanmaku(result, history, episode, player::setDanmaku, player::addDanmaku);
    }

    @Override
    public void renderDetail(Vod item, History history) {
        mHistory = history;
        mBinding.swipeLayout.setRefreshing(false);
        mBinding.progressLayout.showContent();
        mBinding.name.setText(item.getName());
        ImgUtil.load(item.getName(), item.getPic(), mBinding.poster, true, item.getTypeName());
        refreshPlaying();
        App.removeCallbacks(mR4);
        setArtwork(item.getPic());
        checkKeepImg();
        setText(item);
        updateKeep();
    }

    @Override
    public void renderVodUpdate(Vod item) {
        if (!item.getId().isEmpty()) updateNavigationKey();
        renderVodMetadata(item.getName(), item.getPic());
        setText(item);
    }

    private void renderVodMetadata(String name, String pic) {
        if (name.isEmpty() && pic.isEmpty()) return;
        if (!name.isEmpty()) mBinding.name.setText(name);
        if (!pic.isEmpty()) setArtwork();
        updateKeep();
    }

    @Override
    public void renderEmptyDetail() {
        showEmpty();
    }

    @Override
    public void renderFallbackName(String name) {
        mBinding.name.setText(name);
    }

    @Override
    public void renderFlags(List<Flag> items) {
        mBinding.flag.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        mFlagAdapter.addAll(items);
    }

    @Override
    public void renderEpisodes(List<Episode> items) {
        setEpisodeAdapter(items);
        refreshShortDramaInfo();
    }

    @Override
    public void renderFlagSelection(Flag item) {
        notifyItemChanged(mBinding.flag, mFlagAdapter);
        scrollToPosition(mBinding.flag, mFlagAdapter.getPosition());
    }

    @Override
    public void renderEpisodeSelection(Episode item) {
        notifyItemChanged(mBinding.episode, mEpisodeAdapter);
        scrollToPosition(mBinding.episode, mEpisodeAdapter.getPosition());
        refreshShortDramaInfo();
        refreshPlaying();
    }

    @Override
    public void renderReverseEpisodes(List<Episode> items, boolean scroll) {
        setEpisodeAdapter(items);
        if (scroll) scrollToPosition(mBinding.episode, mEpisodeAdapter.getPosition());
    }

    @Override
    public void renderQuality(Result result, boolean visible) {
        mBinding.swipeLayout.setRefreshing(false);
        mQualityAdapter.addAll(result);
        setQualityVisible(visible);
    }

    @Override
    public void renderQualityVisible(boolean visible) {
        setQualityVisible(visible);
    }

    @Override
    public void renderSources(List<Vod> items) {
        mQuickAdapter.addAll(items);
        mBinding.quick.setVisibility(mQuickAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        if (isQuickSearchVisible()) mQuickSearchDialog.addAll(items);
    }

    @Override
    public void renderHistory(History history) {
        mHistory = history;
        mBinding.control.action.opening.setText(history.getOpening() <= 0 ? getString(R.string.play_op) : Util.timeMs(history.getOpening()));
        mBinding.control.action.ending.setText(history.getEnding() <= 0 ? getString(R.string.play_ed) : Util.timeMs(history.getEnding()));
        PlayerManager player = player();
        if (player != null) player.setSpeed(SpeedSetting.getPlayback());
        setScale(getScale());
    }

    @Override
    public void renderUseParse(boolean useParse) {
        setUseParse(useParse);
        mBinding.control.action.parse.setVisibility(isUseParse() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void renderArtwork(String url) {
        setArtwork(url);
    }

    @Override
    public void renderDescription(String desc) {
        setText(mBinding.content, 0, desc);
    }

    @Override
    public void renderPlaybackMetadata(MediaMetadata metadata) {
        if (service() != null && isOwner()) player().setMetadata(metadata);
        // 顶栏标题/分辨率统一由 updateOsd 同步（与屏显同源），元数据回调不再写入
    }

    @Override
    public void onDetailFallbackScheduled() {
        App.post(mR4, 10000);
    }

    @Override
    public void onDetailFallbackCancelled() {
        App.removeCallbacks(mR4);
    }

    @Override
    public void onSearchStarted(String keyword) {
    }

    @Override
    public void onSearchResult() {
        App.removeCallbacks(mR4);
    }

    @Override
    public void showDetailMessage(String msg) {
        Notify.show(msg);
    }

    @Override
    public void showSwitchLine(Flag flag) {
        Notify.show(getString(R.string.play_switch_flag, flag.getFlag()));
    }

    @Override
    public void showSwitchSource(Vod item) {
        Notify.show(getString(R.string.play_switch_site, item.getSiteName()));
    }

    @Override
    public void showEpisodeReady(Episode item) {
        // 短剧刷集不提示
        if (mShortDramaMode) return;
        Notify.show(getString(R.string.play_ready, item.getName()));
    }

    @Override
    public void showNoNext(boolean reversed) {
        if (mShortDramaMode) return;
        Notify.show(reversed ? R.string.error_play_prev : R.string.error_play_next);
    }

    @Override
    public void showNoPrev(boolean reversed) {
        if (mShortDramaMode) return;
        Notify.show(reversed ? R.string.error_play_next : R.string.error_play_prev);
    }

    @Override
    public void finishVod() {
        finish();
    }

    private void checkId() {
        mShortDramaMode = getIntent().getBooleanExtra("short_drama", false)
                || (!getKey().startsWith("hk_") && com.fongmi.android.tv.utils.ShortDramaHelper.isPluginShortDramaBySite(getKey()));
        if (mShortDramaMode) {
            getIntent().putExtra("short_drama", true);
            // 短剧统一竖屏：横屏视频也套进竖屏播放器
            setRequestedOrientation(PlaybackOrientation.getEnterFullscreenOrientation(true));
        }
        setupShortDramaOverlay();
        if (getIntent().getBooleanExtra("hk_direct_play", false)) {
            playHkDirect();
            return;
        }
        mVod.checkId();
    }

    /**
     * 海阔选集直达：用 intent 里的选集列表合成详情，随即播放选中集。
     * 支持两种载荷（与海阔 PlayerChooser 同口径）：
     * 扁平 [{name,url,pic}] 单线路；{flags:[{name,episodes:[...]}]} 多线路（按线路名选中）。
     */
    private void playHkDirect() {
        showProgress();
        // 播放器服务未就绪时先挂起：与 xingchen playDirect 同口径，避免在 onCreate 里对空 player 跑详情/选集链
        if (service() == null) {
            mPendingHkDirect = true;
            return;
        }
        mPendingHkDirect = false;
        // 代理形态的加密内容先预热（其它规则内部 no-op）
        com.fongmi.android.tv.api.hk.HkProxyPrewarm.prewarm(getKey(), getId());
        String lineName = Objects.toString(getIntent().getStringExtra("hk_line"), "");
        int idx = getIntent().getIntExtra("hk_episode_idx", 0);
        java.util.List<Flag> flags = new java.util.ArrayList<>();
        int selectedFlag = 0;
        try {
            String json = Objects.toString(getIntent().getStringExtra("hk_episodes"), "");
            String ref = Objects.toString(getIntent().getStringExtra("hk_episodes_ref"), "");
            if (!ref.isEmpty()) {
                String parked = HK_EPISODES_STORE.get(ref);
                if (parked != null) json = parked;
                else if (TextUtils.isEmpty(json)) {
                    // 仓里取不到（进程重启/已被淘汰）不许静默无集
                    android.widget.Toast.makeText(this, "选集数据已失效，请返回重新打开", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            String t = json == null ? "" : json.trim();
            if (t.startsWith("{")) {
                org.json.JSONObject root = new org.json.JSONObject(t);
                org.json.JSONArray arr = root.optJSONArray("flags");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject fo = arr.optJSONObject(i);
                        if (fo == null) continue;
                        String fname = fo.optString("name", "线路" + (i + 1));
                        Flag flag = new Flag(fname);
                        org.json.JSONArray eps = fo.optJSONArray("episodes");
                        if (eps != null) {
                            for (int j = 0; j < eps.length(); j++) {
                                org.json.JSONObject o = eps.optJSONObject(j);
                                if (o == null) continue;
                                String url = o.optString("url", "");
                                if (TextUtils.isEmpty(url)) continue;
                                String nm = o.optString("name", "");
                                boolean fallbackName = TextUtils.isEmpty(nm);
                                if (fallbackName) nm = String.format(java.util.Locale.getDefault(), "%02d", flag.getEpisodes().size() + 1);
                                Episode ep = Episode.create(nm, url);
                                if (fallbackName) ep.setNumberName(true);
                                flag.getEpisodes().add(ep);
                            }
                        }
                        if (flag.getEpisodes().isEmpty()) continue;
                        flags.add(flag);
                        if (TextUtils.equals(fname, lineName)) selectedFlag = i;
                    }
                }
            } else {
                Flag flag = new Flag(TextUtils.isEmpty(lineName) ? "默认" : lineName);
                org.json.JSONArray arr = new org.json.JSONArray(TextUtils.isEmpty(t) ? "[]" : t);
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject o = arr.optJSONObject(i);
                    if (o == null) continue;
                    String url = o.optString("url", "");
                    if (TextUtils.isEmpty(url)) continue;
                    String nm = o.optString("name", "");
                    boolean fallbackName = TextUtils.isEmpty(nm);
                    if (fallbackName) nm = String.format(java.util.Locale.getDefault(), "%02d", flag.getEpisodes().size() + 1);
                    Episode ep = Episode.create(nm, url);
                    if (fallbackName) ep.setNumberName(true);
                    flag.getEpisodes().add(ep);
                }
                if (!flag.getEpisodes().isEmpty()) flags.add(flag);
            }
        } catch (Throwable ignored) {
        }
        if (flags.isEmpty()) {
            Flag flag = new Flag(TextUtils.isEmpty(lineName) ? "默认" : lineName);
            flag.getEpisodes().add(Episode.create(getName(), getId()));
            flags.add(flag);
        }
        if (selectedFlag < 0 || selectedFlag >= flags.size()) selectedFlag = 0;
        Flag active = flags.get(selectedFlag);
        if (active.getEpisodes().isEmpty()) active.getEpisodes().add(Episode.create(getName(), getId()));
        Vod vod = new Vod();
        vod.setId(getId());
        vod.setName(getName());
        vod.setPic(getPic());
        vod.setFlags(flags);
        // 直连会话：只画详情/选集，不跑 restoreDetail/seamless 自动点火，全程只发一次解析请求
        // 注意：detailLoaded 的 directSession 分支已修复，会主动渲染第一路选集
        mVod.beginDirectSession();
        try {
            mVod.onDetailResult(new VodDetailResult(getKey(), getId(), Result.vod(vod)));
            // 多线路或选中的不是第一路时，显式切到目标线路
            if (flags.size() > 1 || selectedFlag > 0) mVod.selectFlag(active);
            // 直连固定播 intent 里的 getId()：按 URL 命中选集，命中不了才退回 idx
            int target = -1;
            for (int i = 0; i < active.getEpisodes().size(); i++) {
                if (TextUtils.equals(active.getEpisodes().get(i).getUrl(), getId())) {
                    target = i;
                    break;
                }
            }
            if (target < 0) target = idx >= 0 && idx < active.getEpisodes().size() ? idx : 0;
            VodPlayRequest direct = mVod.selectDirectEpisode(active.getEpisodes().get(target));
            if (direct != null) playDirect(direct);
        } finally {
            mVod.endDirectSession();
        }
    }

    /** 与 xingchen playDirect 同款：直连解析并投递，不经 ViewModel 的取消/覆盖调度。 */
    private void playDirect(VodPlayRequest request) {
        Task.execute(() -> {
            ListenableFutureTask<Result> task = ListenableFutureTask.create(
                    () -> SiteApi.playerContent(request.getKey(), request.getFlag(), request.getId()));
            FluentFuture<Result> future = FluentFuture.from(task)
                    .withTimeout(Constant.TIMEOUT_VOD, TimeUnit.MILLISECONDS, Task.scheduler());
            future.addCallback(Task.<Result>callback(
                    result -> App.post(() -> deliverDirectResult(request, result)),
                    error -> App.post(() -> deliverDirectResult(request, toErrorResult(error)))),
                    MoreExecutors.directExecutor());
            task.run();
        });
    }

    private static Result toErrorResult(Throwable error) {
        if (error instanceof ExtractException) return Result.error(error.getMessage());
        return Result.error(error.getMessage() != null ? error.getMessage() : "播放失败，请重试");
    }

    private void deliverDirectResult(VodPlayRequest request, Result result) {
        if (isFinishing() || isDestroyed() || service() == null) return;
        mVod.onPlaybackResult(new PlaybackResult<>(request, result));
    }

    private void checkLand() {
        if (isPort() && ResUtil.isLand(this)) enterFullscreen();
    }

    private void showEmpty() {
        showError(getString(R.string.error_detail));
        mBinding.swipeLayout.setEnabled(true);
        mBinding.progressLayout.showEmpty();
    }

    private void setText(Vod item) {
        setText(mBinding.site, 0, getSite().getName());
        setText(mBinding.director, R.string.detail_director, value(item.getDirector()));
        setText(mBinding.actor, R.string.detail_actor, value(item.getActor()));
        setText(mBinding.content, R.string.detail_content, value(item.getContent()));
        // 备注 / 年份 / 地区 / 类型 合并为一行：全部带前缀，年份只出现一次，空数据显示「暂无」
        String year = "0".equals(item.getYear()) ? "" : item.getYear();
        String remark = getString(R.string.detail_remark, value(item.getRemarks()))
                + " · " + getString(R.string.detail_year, value(year))
                + " · " + getString(R.string.detail_area, value(item.getArea()))
                + " · " + getString(R.string.detail_type, value(item.getTypeName()));
        mBinding.remark.setText(remark);
        mBinding.other.setVisibility(View.GONE);
    }
    /** 空数据显示「暂无」 */
    private String value(String text) {
        return TextUtils.isEmpty(text) ? getString(R.string.detail_none) : text;
    }

    private void setText(TextView view, int resId, String text) {
        if (TextUtils.isEmpty(text) && !TextUtils.isEmpty(view.getText())) return;
        view.setText(Sniffer.buildClickable(resId > 0 ? getString(resId, text) : text, this::clickableSpan), TextView.BufferType.SPANNABLE);
        view.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
        if (view == mBinding.content) setContentVisible();
        view.setLinkTextColor(Color.WHITE);
        CustomMovement.bind(view);
    }

    private void setContentVisible() {
        mBinding.contentLayout.setVisibility(mBinding.content.getVisibility());
    }

    private ClickableSpan clickableSpan(Result result) {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                FolderActivity.start(getActivity(), getKey(), result);
                ((TextView) view).setMaxLines(Integer.MAX_VALUE);
                setRedirect(true);
            }
        };
    }

    private void setOther(TextView view, Vod item) {
        StringBuilder sb = new StringBuilder();
        java.util.List<String> parts = new java.util.ArrayList<>();
        if (!item.getArea().isEmpty()) parts.add(item.getArea());
        if (!item.getTypeName().isEmpty()) parts.add(item.getTypeName());
        sb.append(android.text.TextUtils.join(" · ", parts));
        view.setVisibility(sb.length() == 0 ? View.GONE : View.VISIBLE);
        view.setText(sb.toString());
    }

    @Override
    public void onItemClick(Flag item) {
        mVod.selectFlag(item);
    }

    @Override
    public void onItemClick(Episode item) {
        if (shouldEnterFullscreen(item)) return;
        mVod.selectEpisode(item);
    }

    @Override
    public void onItemClick(Result result) {
        mVod.selectQuality(result);
    }

    @Override
    public void onItemClick(Vod item) {
        mVod.selectSource(item);
    }

    private void setEpisodeAdapter(List<Episode> items) {
        mBinding.control.action.episodes.setVisibility(items.size() < 2 ? View.GONE : View.VISIBLE);
        mBinding.control.next.setVisibility(items.size() < 2 ? View.GONE : View.VISIBLE);
        mBinding.control.prev.setVisibility(items.size() < 2 ? View.GONE : View.VISIBLE);
        mBinding.reverse.setVisibility(items.size() < 2 ? View.GONE : View.VISIBLE);
        mBinding.episode.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        mBinding.download.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        mBinding.more.setVisibility(items.size() < 10 ? View.GONE : View.VISIBLE);
        mEpisodeAdapter.addAll(items);
        refreshShortDramaInfo();
        refreshPlaying();
        // 选集到位后补一次短剧判定（onSizeChanged 可能先于详情到达，时长/集数当时还取不到）
        maybePromoteShortDrama();
    }

    private void refreshPlaying() {
        if (mEpisodeAdapter == null || mEpisodeAdapter.isEmpty()) {
            mBinding.playing.setVisibility(View.GONE);
            return;
        }
        mBinding.playing.setVisibility(View.VISIBLE);
        mBinding.playing.setText(getString(R.string.detail_playing, mEpisodeAdapter.getPosition() + 1));
    }

    private void setQualityVisible(boolean visible) {
        mBinding.qualityTitle.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBinding.quality.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void onName() {
        String name = mBinding.name.getText().toString();
        Notify.show(getString(R.string.detail_search, name));
        showQuickSearch(name);
        mVod.search(name);
    }

    private void showQuickSearch(String keyword) {
        mQuickSearchKeyword = TextUtils.isEmpty(mQuickSearchKeyword) ? keyword : mQuickSearchKeyword;
        mQuickSearchDialog = QuickSearchDialog.create()
                .title(getString(R.string.detail_search, mQuickSearchKeyword))
                .keyword(mQuickSearchKeyword)
                .listener(this)
                .searchListener(this::onQuickSearch)
                .items(mQuickAdapter.getItems());
        mQuickSearchDialog.show(this);
    }

    private void onQuickSearch(String keyword) {
        mQuickSearchKeyword = keyword;
        mVod.search(keyword);
    }

    private boolean isQuickSearchVisible() {
        return mQuickSearchDialog != null && mQuickSearchDialog.isActive();
    }

    private void onDownload() {
        String url = service() != null && isOwner() ? player().getUrl() : null;
        if (TextUtils.isEmpty(url)) {
            Notify.show(R.string.detail_download_play_first);
            return;
        }
        if (!DownloadManager.isSupported(url)) {
            Notify.show(R.string.detail_download_unsupported);
            return;
        }
        Map<String, String> headers = player().getHeaders();
        String name = getVodName();
        if (mEpisodeAdapter.getItemCount() > 0) {
            Episode episode = mEpisodeAdapter.getActivated();
            if (!episode.getName().isEmpty()) name += " - " + episode.getName();
        }
        final String link = url;
        final String fileName = name;
        final Map<String, String> requestHeaders = headers;
        DialogThemeHelper.builder(this).setTitle(R.string.detail_download).setMessage(getString(R.string.detail_download_confirm, fileName)).setNegativeButton(R.string.dialog_negative, null).setPositiveButton(R.string.dialog_positive, (dialog, which) -> {
            DownloadManager.get().add(link, fileName, requestHeaders);
            Notify.show(R.string.detail_download_added);
        }).show();
    }

    private void onMore() {
        EpisodeGridDialog.create().reverse(mHistory.isRevSort()).shortDrama(mShortDramaMode).episodes(mEpisodeAdapter.getItems()).show(this);
    }

    private void onActor() {
        mBinding.actor.setMaxLines(mBinding.actor.getMaxLines() == 1 ? Integer.MAX_VALUE : 1);
    }

    private void onDirector() {
        mBinding.director.setMaxLines(mBinding.director.getMaxLines() == 1 ? Integer.MAX_VALUE : 1);
    }

    private void onContent() {
        mBinding.content.setMaxLines(mBinding.content.getMaxLines() == 1 ? Integer.MAX_VALUE : 1);
    }

    private void onReverse() {
        mVod.setRevSort(!mHistory.isRevSort());
        mVod.reverseEpisode(false);
    }

    private boolean onChange() {
        mVod.manualSwitchSource();
        return true;
    }

    private boolean onCopy() {
        Util.copy(mBinding.content.getText().toString());
        return true;
    }

    private void onBack() {
        if (isFullscreen()) exitFullscreen();
        else finish();
    }

    private void onCast() {
        CastDialog.create(player()).history(mHistory).show(this);
    }

    private void onInfo() {
        InfoDialog.create(player()).show(this);
    }

    private void onKeep() {
        Keep keep = Keep.find(getHistoryKey());
        Notify.show(keep != null ? R.string.keep_del : R.string.keep_add);
        if (keep != null) keep.delete();
        else createKeep();
        checkKeepImg();
    }

    private void checkPlay() {
        setR1Callback();
        if (player().isPlaying()) onPaused();
        else if (player().isEmpty()) onRefresh();
        else onPlay();
    }

    private void checkNext() {
        checkNext(true);
    }

    private void checkNext(boolean notify) {
        setR1Callback();
        mVod.nextEpisode(notify);
    }

    private void checkPrev() {
        setR1Callback();
        mVod.prevEpisode(true);
    }

    private void onSetting() {
        ControlDialog.create().parent(mBinding).parse(isUseParse()).player(player()).show(this);
    }

    private void onLock() {
        setLock(!isLock());
        setRequestedOrientation(getLockOrient());
        mKeyDown.setLock(isLock());
        checkLockImg();
        if (mShortDramaMode) updateShortDramaLockUi();
        else showControl();
    }

    private void onRotate() {
        setR1Callback();
        // 功能栏旋转 = 进入短剧式竖屏播放器（复用短剧整套竖屏 UI/预下集/竖屏锁定）
        if (mShortDramaMode) {
            setRequestedOrientation(PlaybackOrientation.getEnterFullscreenOrientation(true));
            return;
        }
        mShortDramaMode = true;
        getIntent().putExtra("short_drama", true);
        setupShortDramaOverlay();
        enterShortDramaFullscreen();
    }

    /** 短剧模式全屏播放（对齐原生短剧进入路径：全屏容器 + 竖屏方向锁定）。 */
    private void enterShortDramaFullscreen() {
        if (player() == null || player().isEmpty()) return;
        if (!isFullscreen()) enterFullscreen();
        if (mShortUi != null) mShortUi.setVisibility(View.VISIBLE);
        setRequestedOrientation(PlaybackOrientation.getEnterFullscreenOrientation(true));
    }

    private void onTrack(View view) {
        TrackDialog.create().type(Integer.parseInt(view.getTag().toString())).player(player()).view(mBinding.player.getSubtitleView()).show(this);
        hideControl();
    }

    private void onEdition() {
        EditionDialog.create().player(player()).show(this);
        hideControl();
    }

    private void onChapter() {
        ChapterDialog.create().player(player()).show(this);
        hideControl();
    }

    private void onDanmaku() {
        DanmakuDialog.create().player(player()).show(this);
        hideControl();
    }

    private void onDanmakuShow() {
        DanmakuSetting.putShow(!DanmakuSetting.isShow());
        checkDanmakuImg();
        syncDanmakuEnabled();
    }

    private void onRepeat() {
        player().setRepeatOne(!player().isRepeatOne());
        mBinding.control.action.repeat.setSelected(player().isRepeatOne());
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        mBinding.control.action.repeat.setSelected(player().isRepeatOne());
    }

    private void onScale() {
        String[] array = ResUtil.getStringArray(R.array.select_scale);
        if (mKeyDown.getScale() != 1.0f) mKeyDown.resetScale();
        DialogThemeHelper.builder(this).setTitle(R.string.player_scale).setSingleChoiceItems(array, getScale(), (dialog, which) -> {
            setScale(which);
            setR1Callback();
            dialog.dismiss();
        }).show();
    }

    private void onSpeed() {
        SpeedSettingDialog.create().player(player()).save(true).show(this);
        hideControl();
    }

    private boolean onSpeedLong() {
        SpeedSetting.putPlayback(PlaybackAction.toggleSpeed(player(), mBinding.widget.message));
        setR1Callback();
        return true;
    }

    private void onReset() {
        onRefresh();
    }

    private void onParse() {
        ParseDialog.create().show(this);
        hideControl();
    }

    private void onReplay() {
        mVod.replay();
    }

    private void onRefresh() {
        mVod.refresh();
    }

    private void onDecode() {
        mClock.setCallback(null);
        player().toggleDecode();
        setR1Callback();
    }

    private void onEnding() {
        long position = player().getPosition();
        long duration = player().getDuration();
        if (player().canSetEnding(position, duration)) setEnding(duration - position);
        setR1Callback();
    }

    private boolean onEndingReset() {
        setR1Callback();
        setEnding(0);
        return true;
    }

    private void setEnding(long ending) {
        mVod.setEnding(ending);
        mBinding.control.action.ending.setText(ending <= 0 ? getString(R.string.play_ed) : Util.timeMs(mHistory.getEnding()));
    }

    private void onOpening() {
        long position = player().getPosition();
        long duration = player().getDuration();
        if (player().canSetOpening(position, duration)) setOpening(position);
        setR1Callback();
    }

    private boolean onOpeningReset() {
        setR1Callback();
        setOpening(0);
        return true;
    }

    private void setOpening(long opening) {
        mVod.setOpening(opening);
        mBinding.control.action.opening.setText(opening <= 0 ? getString(R.string.play_op) : Util.timeMs(mHistory.getOpening()));
    }

    private void onEpisodes() {
        EpisodeListDialog.create().flags(mFlagAdapter.getItems()).reverse(mHistory.isRevSort()).show(this);
    }

    private void onPlayer() {
        PlayerEngineDialog.show(this, mBinding.control.action.player, player());
        hideControl();
    }

    private boolean onActionTouch(View v, MotionEvent e) {
        setR1Callback();
        return false;
    }

    private void onSwipeRefresh() {
        if (mBinding.progressLayout.isEmpty()) mVod.requestDetail();
        else mVod.refresh();
    }

    private boolean shouldEnterFullscreen(Episode item) {
        boolean enter = !isFullscreen() && item.isSelected();
        if (enter) enterFullscreen();
        return enter;
    }

    private void enterFullscreen() {
        if (isFullscreen()) return;
        setFullscreen(true);
        // 短剧统一竖屏：抖音式竖屏播放器，横屏拍摄的短剧也套进竖屏容器（不等播放器尺寸）
        boolean portrait = mShortDramaMode || player().isPortrait();
        if (isLand() && !player().isPortrait()) setTransition();
        mBinding.video.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        setRequestedOrientation(PlaybackOrientation.getEnterFullscreenOrientation(portrait));
        mBinding.control.title.setVisibility(View.INVISIBLE);
        mBinding.control.size.setVisibility(View.GONE);
        setRotate(portrait);
        mKeyDown.resetScale();
        App.post(mR3, 2000);
        hideControl();
    }

    private void exitFullscreen() {
        if (!isFullscreen()) return;
        setFullscreen(false);
        if (isLand() && !player().isPortrait()) setTransition();
        setRequestedOrientation(PlaybackOrientation.getExitFullscreenOrientation(isPort()));
        mBinding.episode.postDelayed(() -> mBinding.episode.scrollToPosition(mEpisodeAdapter.getPosition()), 100);
        mBinding.control.title.setVisibility(View.INVISIBLE);
        mBinding.control.size.setVisibility(View.GONE);
        mBinding.video.setLayoutParams(mFrameParams);
        mKeyDown.resetScale();
        App.post(mR3, 2000);
        setRotate(false);
        hideControl();
    }

    private void setTransition() {
        ChangeBounds transition = new ChangeBounds();
        transition.setDuration(150);
        ViewGroup parent = (ViewGroup) mBinding.video.getParent();
        TransitionManager.beginDelayedTransition(parent, transition);
    }

    private int getLockOrient() {
        return PlaybackOrientation.getLockOrientation(this, isLock(), isRotate(), isPort() && isAutoRotate());
    }

    private void showProgress() {
        mBinding.progress.getRoot().setVisibility(View.VISIBLE);
        App.post(mR2, 0);
        hideError();
    }

    private void hideProgress() {
        mBinding.progress.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(mR2);
        Traffic.reset();
    }

    private void showError(String text) {
        PlaybackAction.hideSpeedHint(mBinding.widget.message);
        mBinding.widget.error.setVisibility(View.VISIBLE);
        mBinding.widget.error.setText(text);
        hideProgress();
    }

    private void hideError() {
        mBinding.widget.error.setVisibility(View.GONE);
        mBinding.widget.error.setText("");
    }

    private void syncDanmakuEnabled() {
        player().setDanmakuEnabled(DanmakuSetting.isShow());
    }

    private void hideDanmaku() {
        player().setDanmakuEnabled(false);
    }

    private void showControl() {
        // 短剧模式用专用 UI，横屏控制层永不出现
        if (mShortUi != null) return;
        if (service() == null || isInPictureInPictureMode()) return;
        mBinding.danmakuToggle.setVisibility(isLock() || !player().haveDanmaku() ? View.GONE : View.VISIBLE);
        mBinding.setting.setVisibility(mHistory == null || isFullscreen() ? View.GONE : View.VISIBLE);
        mBinding.control.right.rotate.setVisibility(isFullscreen() && !isLock() ? View.VISIBLE : View.GONE);
        mBinding.keep.setVisibility(mHistory == null || isFullscreen() ? View.GONE : View.VISIBLE);
        mBinding.control.action.getRoot().setVisibility(isFullscreen() ? View.VISIBLE : View.GONE);
        mBinding.control.right.lock.setVisibility(isFullscreen() ? View.VISIBLE : View.GONE);
        mBinding.control.action.parse.setVisibility(isUseParse() ? View.VISIBLE : View.GONE);
        mBinding.info.setVisibility(player().isEmpty() ? View.GONE : View.VISIBLE);
        mBinding.cast.setVisibility(player().isEmpty() ? View.GONE : View.VISIBLE);
        mBinding.control.center.setVisibility(isLock() ? View.GONE : View.VISIBLE);
        mBinding.control.bottom.setVisibility(isLock() ? View.GONE : View.VISIBLE);
        // 返回箭头取消：详情页用左滑/系统返回，控制层不再显示
        mBinding.control.back.setVisibility(View.GONE);
        mBinding.control.top.setVisibility(isLock() ? View.GONE : View.VISIBLE);
        mBinding.control.getRoot().setVisibility(View.VISIBLE);
        setR1Callback();
        updateOsd();
    }

    private void hideControl() {
        mBinding.control.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(mR1);
        updateOsd();
    }

    private void dismissDialogs() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) if (fragment instanceof DialogFragment dialog) dialog.dismiss();
    }

    private void setTraffic() {
        Traffic.setSpeed(mBinding.progress.traffic);
        App.post(mR2, 1000);
    }

    private void setOrient() {
        if (isPort() && isAutoRotate()) setRequestedOrientation(PlaybackOrientation.getPortAutoRotateOrientation());
        if (isLand() && isAutoRotate()) setRequestedOrientation(PlaybackOrientation.getLandAutoRotateOrientation());
    }

    private void setR1Callback() {
        if (isScrubbing()) return;
        App.post(mR1, Constant.INTERVAL_HIDE);
    }

    @Override
    protected void onScrubbingChanged(boolean scrubbing) {
        if (scrubbing) App.removeCallbacks(mR1);
        else if (isVisible(mBinding.control.getRoot())) setR1Callback();
    }

    private void setArtwork(String url) {
        mHistory.setVodPic(url);
        setArtwork();
    }


    private void setArtwork() {
        ImgUtil.load(this, mHistory.getVodPic(), new CustomTarget<>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                mBinding.player.setDefaultArtwork(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                mBinding.player.setDefaultArtwork(errorDrawable);
            }
        });
    }

    private void saveHistory(boolean exit) {
        PlaybackService service = service();
        boolean owner = service != null && getPlaybackKey().equals(service.player().getKey());
        long position = owner ? service.player().getPosition() : C.TIME_UNSET;
        long duration = owner ? service.player().getDuration() : C.TIME_UNSET;
        if (mVod != null) mVod.saveHistory(exit, System.currentTimeMillis(), position, duration);
    }

    private void checkControl() {
        if (isVisible(mBinding.control.getRoot())) showControl();
    }

    private void checkKeepImg() {
        // funcBar（浅色底）用深色版图标；短剧 mSdKeep（深色底）用原白色图标
        int icon = Keep.find(getHistoryKey()) == null ? R.drawable.ic_func_keep_off_w : R.drawable.ic_func_keep_on_w;
        mBinding.keepImg.setImageResource(icon);
        if (mSdKeep != null) mSdKeep.setImageResource(Keep.find(getHistoryKey()) == null ? R.drawable.ic_control_keep_off : R.drawable.ic_control_keep_on);
    }

    private void checkLockImg() {
        mBinding.control.right.lock.setImageResource(isLock() ? R.drawable.ic_control_lock_on : R.drawable.ic_control_lock_off);
    }

    private void checkDanmakuImg() {
        mBinding.danmakuToggleImg.setImageResource(DanmakuSetting.isShow() ? R.drawable.ic_func_danmaku_on_w : R.drawable.ic_func_danmaku_off_w);
    }

    private void createKeep() {
        Keep keep = new Keep();
        keep.setKey(getHistoryKey());
        keep.setCid(VodConfig.getCid());
        keep.setVodPic(mHistory.getVodPic());
        keep.setVodName(mHistory.getVodName());
        keep.setSiteName(getSite().getName());
        keep.setCreateTime(System.currentTimeMillis());
        keep.save();
    }

    private void updateKeep() {
        Keep keep = Keep.find(getHistoryKey());
        if (keep != null) {
            keep.setVodName(mHistory.getVodName());
            keep.setVodPic(mHistory.getVodPic());
            keep.save();
        }
    }

    private final PlaybackService.NavigationCallback mNavigationCallback = new PlaybackService.NavigationCallback() {
        @Override
        public void onNext() {
            checkNext();
        }

        @Override
        public void onPrev() {
            checkPrev();
        }

        @Override
        public void onStop() {
            finish();
        }

        @Override
        public void onReplay() {
            VideoActivity.this.onReplay();
        }

        @Override
        public void onAudio() {
            Util.moveToBackground(VideoActivity.this);
            setAudioOnly(true);
        }
    };

    @Override
    protected String getPlaybackKey() {
        return getHistoryKey();
    }

    @Override
    protected void onPrepare() {
        setPlaybackMode();
        checkControl();
    }

    @Override
    protected void onDecodeChanged() {
        setPlaybackMode();
    }

    @Override
    protected void onTracksChanged() {
        setTrackVisible();
    }

    @Override
    protected void onMediaOptionsChanged() {
        setMediaOptionVisible();
    }

    @Override
    protected void onError(String msg) {
        mVod.playbackError(msg);
    }

    @Override
    protected void onReclaim() {
        mVod.refresh();
    }

    @Override
    protected void onStateChanged(int state) {
        switch (state) {
            case Player.STATE_BUFFERING:
                showProgress();
                mClock.setCallback(null);
                break;
            case Player.STATE_READY:
                hideProgress();
                checkControl();
                player().reset();
                mClock.setCallback(this);
                break;
            case Player.STATE_ENDED:
                hideProgress();
                setR1Callback();
                mVod.playbackEnded();
                updatePlayControl(false);
                mClock.setCallback(null);
                mBinding.osd.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onPlayingChanged(boolean isPlaying) {
        if (isPlaying || isPaused()) updatePlayControl(isPlaying);
        // 暂停/恢复瞬间屏显立即重算：控制层已弹时点暂停（不经过 showControl/hideControl），
        // 否则屏显进度行/标题保持播放中状态直到下一次秒级刷新
        updateOsd();
    }

    private void updatePlayControl(boolean isPlaying) {
        mBinding.control.play.setImageResource(isPlaying ? androidx.media3.ui.R.drawable.exo_icon_pause : androidx.media3.ui.R.drawable.exo_icon_play);
        mPiP.update(this, isPlaying);
    }

    @Override
    protected void onSizeChanged(VideoSize size) {
        changeHeight();
        checkOrientation();
        maybePromoteShortDrama();
        updateOsd();
    }

    /**
     * 起播后按视频本身判定短剧（点播与小程序推送共用）：单集 ≤8 分钟、≥3 集、
     * 且画面为竖屏（播放器真实解码宽高）→ 短剧 UI。升格不重启播放：只挂 UI + 全屏 + 整集预下。
     */
    private void maybePromoteShortDrama() {
        if (mShortDramaMode) return;
        PlayerManager pm = player();
        if (pm == null || pm.isEmpty()) return;
        long duration = pm.getDuration();
        if (duration <= 0 || duration > SHORT_DRAMA_MAX_EPISODE_MS) return;   // 单集不够短，不是短剧
        int count = mEpisodeAdapter == null ? 0 : mEpisodeAdapter.getItemCount();
        if (count < 3) return;                                               // 单条播放不是剧
        if (!pm.isPortrait()) return;                                        // 横屏画面不是短剧
        mShortDramaMode = true;
        getIntent().putExtra("short_drama", true);
        if (!isFullscreen()) enterFullscreen();
        setupShortDramaOverlay();
        applyShortDramaPreload();
        refreshShortDramaInfo();
    }


    @Override
    public void onTimeChanged(long time) {
        if (!isOwner() || !player().isVod()) return;
        long position = player().getPosition();
        long duration = player().getDuration();
        if (position < 0 || duration <= 0) return;
        mVod.onTimeChanged(time, position, duration);
        if (mSdProgress != null) {
            mSdProgress.setProgress((int) Math.min(1000, position * 1000 / duration));
        }
        updateOsd();
    }

    /** 屏显标题：标题 + 当前集数；集数名与标题相同（含集名即片名）时不再追加。 */
    private String getOsdTitle() {
        String title = mBinding.name.getText() == null ? "" : mBinding.name.getText().toString();
        if (mEpisodeAdapter == null || mEpisodeAdapter.getItemCount() <= 0) return title;
        Episode ep = mEpisodeAdapter.getActivated();
        String epName = ep == null ? "" : ep.getName();
        if (TextUtils.isEmpty(epName)) return title;
        // 集名与标题相同或互相包含时只显示一个，避免「标题 · 标题-4K」
        if (TextUtils.isEmpty(title) || epName.equalsIgnoreCase(title) || epName.contains(title) || title.contains(epName)) {
            return TextUtils.isEmpty(title) ? epName : title;
        }
        return title + " · " + epName;
    }

    private void updateOsd() {
        if (isInPictureInPictureMode() || mShortDramaMode) {
            mBinding.osd.setVisibility(View.GONE);
            return;
        }
        boolean controlUp = mBinding.control.getRoot().getVisibility() == View.VISIBLE;
        boolean masterOn = PlayerSetting.isOsdEnabled();
        if (!controlUp && !masterOn) {
            mBinding.osd.setVisibility(View.GONE);
            return;
        }
        long position = Math.max(0, player().getPosition());
        long duration = player().getDuration();
        String osdTitle = getOsdTitle();
        int videoWidth = player().getVideoWidth();
        int videoHeight = player().getVideoHeight();
        int percent = duration > 0 ? (int) Math.min(100, position * 100 / duration) : 0;
        updateTraffic();
        // 控制层弹起：标题+分辨率固定显示在屏显位置（无视开关，避免顶栏叠影）；
        // 控制层收起：按各开关显示
        StringBuilder info = new StringBuilder();
        boolean showRes = videoWidth > 0 && videoHeight > 0 && (controlUp || (masterOn && PlayerSetting.isOsdResolution()));
        if (showRes) info.append(getString(R.string.osd_resolution, videoWidth, videoHeight));
        if (masterOn && PlayerSetting.isOsdDiagnostics()) {
            String engine = PlayerSetting.isExo() ? "EXO" : "IJK";
            if (info.length() > 0) info.append(" · ");
            info.append(getString(R.string.osd_diagnostics, engine + " · " + PlayerSetting.getBuffer() + " ms"));
        }
        boolean showTitle = !TextUtils.isEmpty(osdTitle) && (controlUp || (masterOn && PlayerSetting.isOsdTitle()));
        mBinding.osdTitle.setText(osdTitle);
        mBinding.osdTitle.setVisibility(showTitle ? View.VISIBLE : View.GONE);
        mBinding.osdInfo.setText(info);
        mBinding.osdInfo.setVisibility(info.length() == 0 ? View.GONE : View.VISIBLE);
        mBinding.osdTime.setText(mOsdClock.format(new Date()));
        mBinding.osdTime.setVisibility(masterOn && PlayerSetting.isOsdTime() ? View.VISIBLE : View.GONE);
        mBinding.osdSpeed.setText(mOsdSpeed);
        mBinding.osdSpeed.setVisibility(masterOn && PlayerSetting.isOsdTraffic() ? View.VISIBLE : View.GONE);
        // 控制层弹起时屏显细进度行让位给可拖动的控制条
        boolean showProgress = masterOn && !controlUp && PlayerSetting.isOsdProgress() && duration > 0 && mBinding.osdProgressRow != null;
        mBinding.osdProgressRow.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        if (showProgress) {
            mBinding.osdPosition.setText(Util.timeMs(position));
            mBinding.osdDuration.setText(Util.timeMs(duration));
            mBinding.osdMini.setProgress(percent);
        }
        boolean anyVisible = showTitle || info.length() > 0 || (masterOn && (PlayerSetting.isOsdTime() || PlayerSetting.isOsdTraffic())) || showProgress;
        mBinding.osd.setVisibility(anyVisible ? View.VISIBLE : View.GONE);
    }
    private void updateTraffic() {
        long now = SystemClock.elapsedRealtime();
        long rx = TrafficStats.getTotalRxBytes();
        if (mOsdRxTime > 0 && now > mOsdRxTime && rx >= mOsdRxBytes) {
            long speed = (rx - mOsdRxBytes) * 1000 / (now - mOsdRxTime);
            mOsdSpeed = Formatter.formatShortFileSize(this, speed) + "/s";
        }
        mOsdRxBytes = rx;
        mOsdRxTime = now;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCastEvent(CastEvent event) {
        if (isRedirect()) return;
        ReceiveDialog.create().event(event).show(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (isRedirect()) return;
        if (event.getType() == RefreshEvent.Type.DETAIL) mVod.requestDetail();
        else if (event.getType() == RefreshEvent.Type.PLAYER) mVod.refresh();
        else if (event.getType() == RefreshEvent.Type.VOD) mVod.updateVod(event.getVod());
        else if (event.getType() == RefreshEvent.Type.SUBTITLE) player().setSub(Sub.from(event.getPath()));
        else if (event.getType() == RefreshEvent.Type.DANMAKU) player().setDanmaku(Danmaku.from(event.getPath()));
    }

    @Override
    protected long startPositionMs() {
        return mVod == null ? C.TIME_UNSET : mVod.startPositionMs();
    }

    private void checkOrientation() {
        // 短剧强制竖屏：抖音式竖屏播放器，不随视频宽高翻转成横屏
        if (mShortDramaMode) {
            if (isFullscreen() && !isRotate()) {
                setRequestedOrientation(PlaybackOrientation.getPortraitVideoSizeOrientation());
                setRotate(true);
            }
            return;
        }
        if (isFullscreen() && !isRotate() && player().isPortrait()) {
            setRequestedOrientation(PlaybackOrientation.getPortraitVideoSizeOrientation());
            setRotate(true);
        } else if (isFullscreen() && isRotate() && player().isLandscape()) {
            setRequestedOrientation(PlaybackOrientation.getLandscapeVideoSizeOrientation());
            setRotate(false);
        }
    }

    private void changeHeight() {
        if (isLand() || isFullscreen() || isInPictureInPictureMode()) return;
        int videoWidth = player().getVideoWidth();
        int videoHeight = player().getVideoHeight();
        if (videoWidth == 0 || videoHeight == 0) return;
        int viewWidth = ResUtil.getScreenWidth();
        int minHeight = ResUtil.dp2px(150);
        int maxHeight = ResUtil.getScreenHeight() / 2;
        int calculated = (int) (viewWidth * ((float) videoHeight / videoWidth));
        int finalHeight = Math.clamp(calculated, minHeight, Math.max(minHeight, maxHeight));
        if (finalHeight == mBinding.video.getHeight()) return;
        if (mAnimator.isRunning()) mAnimator.cancel();
        mAnimator.setIntValues(mBinding.video.getHeight(), finalHeight);
        mAnimator.setDuration(300);
        mAnimator.start();
    }

    private void setTrackVisible() {
        PlaybackAction.setTracks(player(), mBinding.control.action.text, mBinding.control.action.audio, mBinding.control.action.video);
    }

    private void setMediaOptionVisible() {
        PlaybackAction.setMediaOptions(player(), mBinding.control.action.edition, mBinding.control.action.chapter);
    }

    private void onPaused() {
        controller().pause();
    }

    private void onPlay() {
        if (mHistory != null && isEnded()) controller().seekTo(mHistory.getOpening());
        if (!player().isEmpty() && isIdle()) controller().prepare();
        controller().play();
    }

    private boolean isFullscreen() {
        return fullscreen;
    }

    private void setFullscreen(boolean fullscreen) {
        Util.toggleFullscreen(this, this.fullscreen = fullscreen);
    }

    public boolean isUseParse() {
        return useParse;
    }

    public void setUseParse(boolean useParse) {
        this.useParse = useParse;
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
        if (fullscreen && !rotate) setPadding(mBinding.control.getRoot());
        else noPadding(mBinding.control.getRoot());
    }

    private void notifyItemChanged(RecyclerView view, RecyclerView.Adapter<?> adapter) {
        view.post(() -> adapter.notifyItemRangeChanged(0, adapter.getItemCount()));
    }

    private void scrollToPosition(RecyclerView view, int position) {
        view.post(() -> view.scrollToPosition(position));
    }

    @Override
    public void onCasted() {
        player().stop();
    }

    @Override
    public void onScale(int tag) {
        mKeyDown.resetScale();
        setScale(tag);
    }

    @Override
    public void onParse(Parse item) {
        mVod.selectParse(item);
    }

    @Override
    public void onSpeedUp() {
        if (!player().isPlaying()) return;
        PlaybackAction.startSpeedPress(player(), mBinding.widget.message);
    }

    @Override
    public void onSpeedEnd() {
        PlaybackAction.hideSpeedHint(mBinding.widget.message);
        player().setSpeed(SpeedSetting.getPlayback());
    }

    @Override
    public void onBright(int progress) {
        mBinding.widget.bright.setVisibility(View.VISIBLE);
        mBinding.widget.brightProgress.setProgress(progress);
        if (progress < 35) mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_low);
        else if (progress < 70) mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_medium);
        else mBinding.widget.brightIcon.setImageResource(R.drawable.ic_widget_bright_high);
    }

    @Override
    public void onVolume(int progress) {
        mBinding.widget.volume.setVisibility(View.VISIBLE);
        mBinding.widget.volumeProgress.setProgress(progress);
        if (progress < 35) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_low);
        else if (progress < 70) mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_medium);
        else mBinding.widget.volumeIcon.setImageResource(R.drawable.ic_widget_volume_high);
    }

    @Override
    public void onFlingUp() {
        if (mEpisodeAdapter.getItemCount() == 1) onRefresh();
        else mVod.nextEpisode(true);
    }

    @Override
    public void onFlingDown() {
        if (mEpisodeAdapter.getItemCount() == 1) onRefresh();
        else mVod.prevEpisode(true);
    }

    @Override
    public void onSeeking(long time) {
        mBinding.widget.action.setImageResource(time > 0 ? R.drawable.ic_widget_forward : R.drawable.ic_widget_rewind);
        mBinding.widget.time.setText(player().getPositionTime(time));
        mBinding.widget.seek.setVisibility(View.VISIBLE);
        hideProgress();
    }

    @Override
    public void onSeekEnd(long time) {
        seekTo(time);
    }

    @Override
    public void onSingleTap() {
        if (mShortUi != null) {
            // 锁定态点画面不切播放/暂停，只能点解锁按钮
            if (isLock()) return;
            toggleShortPlayback();
            return;
        }
        if (isVisible(mBinding.control.getRoot())) hideControl();
        else showControl();
    }

    @Override
    public void onDoubleTap() {
        if (mShortUi != null) {
            onSingleTap();
            return;
        }
        if (isLock()) return;
        if (!isFullscreen()) {
            enterFullscreen();
        } else if (player().isPlaying()) {
            showControl();
            onPaused();
        } else {
            hideControl();
            onPlay();
        }
    }

    @Override
    public void onTouchEnd() {
        mBinding.widget.seek.setVisibility(View.GONE);
        mBinding.widget.bright.setVisibility(View.GONE);
        mBinding.widget.volume.setVisibility(View.GONE);
        PlaybackAction.hideSpeedHint(mBinding.widget.message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1001) PlaybackIntent.onExternalResult(data, service()::dispatchNext, controller()::seekTo);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (isRedirect()) return;
        if (isLock()) App.post(this::onLock, 500);
        if (service() != null && player().haveTrack(C.TRACK_TYPE_VIDEO)) mPiP.enter(this, player().getVideoWidth(), player().getVideoHeight(), getScale());
    }

    @Override
    public void onPictureInPictureModeChanged(boolean inPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(inPictureInPictureMode, newConfig);
        if (!isBindingOwner()) return;
        if (inPictureInPictureMode) onEnterPictureInPicture();
        else onExitPictureInPicture();
    }

    private void onEnterPictureInPicture() {
        if (!isFullscreen()) setVideoView(true);
        dismissDialogs();
        hideControl();
        hideDanmaku();
        // 短剧 UI 在画中画里必须整层隐藏，否则按钮/文字会随窗口放大
        if (mShortUi != null) mShortUi.setVisibility(View.GONE);
    }

    private void onExitPictureInPicture() {
        if (!isFullscreen()) setVideoView(false);
        syncDanmakuEnabled();
        if (mShortUi != null) {
            mShortUi.setVisibility(View.VISIBLE);
            updateShortDramaLockUi();
            refreshShortDramaInfo();
        }
        if (isStop()) finish();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 短剧固定竖屏：系统旋转/自动旋转都不改变页面方向
        if (mShortDramaMode) {
            if (isFullscreen()) Util.hideSystemUI(this);
            return;
        }
        if (isAutoRotate() && isPort() && newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && !isRotate() && !isLock()) exitFullscreen();
        if (isAutoRotate() && isPort() && newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) enterFullscreen();
        if (isFullscreen()) Util.hideSystemUI(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFullscreen() && hasFocus && !Util.isFullscreen(this)) Util.hideSystemUI(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sPlayerOwner = this;
        hasPlayer = true;
        mClock.stop().start();
        setAudioOnly(false);
        setStop(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 只有真正离场（不再前台）才放掉存活标志；本实例仍是属主时才许清
        if (sPlayerOwner == this) {
            hasPlayer = false;
            sPlayerOwner = null;
        }
        saveHistory(false);
        if (PlayerSetting.isBackgroundOff()) mClock.stop();
        if (!isAudioOnly()) setStop(true);
    }

    @Override
    protected void onBackInvoked() {
        // 短剧专用播放器：返回直接退出（竖屏全屏没有"退出全屏"这一层）
        if (mShortUi != null) {
            mViewModel.stopSearch();
            super.onBackInvoked();
            return;
        }
        if (isVisible(mBinding.control.getRoot())) {
            hideControl();
        } else if (isFullscreen() && !isLock()) {
            exitFullscreen();
        } else if (!isLock()) {
            mViewModel.stopSearch();
            if (isTaskRoot()) startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            super.onBackInvoked();
        }
    }

    @Override
    protected void onDestroy() {
        if (mShortDramaMode && player() != null) player().setNextEpisodePreloadDuration(10_000L);
        mClock.release();
        saveHistory(true);
        Timer.get().reset();
        DanmakuApi.cancel();
        RefreshEvent.keep();
        App.removeCallbacks(mR1, mR2, mR3, mR4);
        super.onDestroy();
    }
}
