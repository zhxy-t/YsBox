package com.fongmi.android.tv.ui.activity;

import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Updater;
import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.api.config.WallConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.databinding.ActivityHomeBinding;
import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.event.ConfigEvent;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.event.ServerEvent;
import com.fongmi.android.tv.event.StateEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.player.extractor.Source;
import com.fongmi.android.tv.receiver.ShortcutReceiver;
import com.fongmi.android.tv.server.Server;
import com.fongmi.android.tv.service.PlaybackService;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.custom.FragmentStateManager;
import com.fongmi.android.tv.ui.fragment.SettingAppearanceFragment;
import com.fongmi.android.tv.ui.fragment.SettingDanmakuFragment;
import com.fongmi.android.tv.ui.fragment.SettingDecodeFragment;
import com.fongmi.android.tv.ui.fragment.SettingFragment;
import com.fongmi.android.tv.ui.fragment.SettingPersonalFragment;
import com.fongmi.android.tv.ui.fragment.SettingPlayerFragment;
import com.fongmi.android.tv.ui.fragment.SettingPreloadFragment;
import com.fongmi.android.tv.ui.fragment.SettingSubtitleFragment;
import com.fongmi.android.tv.ui.fragment.VodFragment;
import com.fongmi.android.tv.utils.FileChooser;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.PermissionUtil;
import com.fongmi.android.tv.utils.UrlUtil;
import com.fongmi.android.tv.utils.Util;
import com.github.catvod.net.OkHttp;
import com.google.android.material.navigation.NavigationBarView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HomeActivity extends BaseActivity implements NavigationBarView.OnItemSelectedListener {

    private boolean navHidden;
    private float navTouchY;


    private FragmentStateManager mManager;
    private ActivityHomeBinding mBinding;
    private int orientation;
    /** 已处理过的剪贴板文本（落盘），跨 Activity 重建/进程重启仍生效。 */
    private static final String PREF_CLIP_HANDLED = "hk_clip_handled";
    private String lastClipboardRule;

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityHomeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从播放器/全屏页返回时方向可能被设为 USER_LANDSCAPE/FULL_USER；
        // 手机界面首页恢复竖屏（TV 模式由 manifest 锁横屏，不处理）。
        if (!com.fongmi.android.tv.ui.UiMode.isLeanback())
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        checkClipboardRule();
    }

    /** 回到首页时识别剪贴板里的小程序口令/规则，直接询问导入，不用先进小程序。 */
    private void checkClipboardRule() {
        try {
            if (!MiniAppActivity.isEnabled(this)) return;
            android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm == null || !cm.hasPrimaryClip()) return;
            android.content.ClipData clip = cm.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) return;
            CharSequence raw = clip.getItemAt(0).coerceToText(this);
            String text = raw == null ? "" : raw.toString().trim();
            if (text.isEmpty() || text.length() > 600000 || text.equals(lastClipboardRule)) return;
            // 落盘去重：导入过（或已取消过）的同一段文本，回到首页/进出小程序不再反复弹
            String handled = com.github.catvod.utils.Prefers.getString(PREF_CLIP_HANDLED, "");
            if (text.equals(handled)) {
                lastClipboardRule = text;
                return;
            }
            lastClipboardRule = text;
            boolean ruleLike = com.fongmi.android.tv.api.hk.HkRuleManager.isShareCommandText(text)
                    || com.fongmi.android.tv.api.hk.HkRuleManager.isCloudCode(text)
                    || com.fongmi.android.tv.api.hk.HkRuleManager.isJsRuleText(text)
                    || text.contains("\"find_rule\"")
                    || text.contains("海阔视界")
                    || text.contains("小程序￥")
                    || (text.contains("home_rule") && text.contains("{"))
                    || text.matches(".*云[1-9]oooole/\\S+.*");
            if (!ruleLike) return;
            new com.fongmi.android.tv.ui.hk.HkDialogs(this)
                    .setTitle("检测到小程序")
                    .setMessage("剪贴板里有小程序规则，要现在导入吗？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("导入", (d, w) -> {
                        com.github.catvod.utils.Prefers.put(PREF_CLIP_HANDLED, text);
                        // 交小程序页走完整导入链（解析 → 选分组 → 落盘）
                        com.fongmi.android.tv.ui.activity.HkPageActivity.startImport(this, text);
                    })
                    .show();
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkAction(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        orientation = getResources().getConfiguration().orientation;
        mBinding.navigation.setOnItemSelectedListener(this);
        applyCapsuleTheme();
        PermissionUtil.requestNotify(this);
        initFragment(savedInstanceState);
        Updater.create().start(this);
        initConfig();
    }

    @Override
    protected void initEvent() {
        mBinding.navigation.findViewById(R.id.live).setOnLongClickListener(this::addShortcut);
    }

    /** 底栏胶囊受「胶囊背景」设置控制：背景/图标色/选中指示器一并刷新 */
    private void applyCapsuleTheme() {
        com.google.android.material.navigation.NavigationBarView nav = mBinding.navigation;
        nav.setBackground(com.fongmi.android.tv.ui.dialog.ChipThemeHelper.background(this, 28f));
        nav.setItemIconTintList(com.fongmi.android.tv.ui.dialog.ChipThemeHelper.navIconTint(this));
        nav.setItemActiveIndicatorEnabled(false);
        // 兜底：Material 在选中刷新时会重设 item 内容容器 gravity，强制垂直居中
        forceNavIconsCenter();
    }
    private void forceNavIconsCenter() {
        // M3 在 unlabeled 模式下标签组仍以 INVISIBLE 参与测量，内容容器比图标高，
        // 因此不能只居中内容容器——要按「图标自身相对 item 的中心」校正。
        // 每帧先归零再计算，避免位移累积。
        final ViewGroup nav = mBinding.navigation;
        nav.getViewTreeObserver().addOnPreDrawListener(new android.view.ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                try {
                    View first = nav.getChildCount() > 0 ? nav.getChildAt(0) : null;
                    if (!(first instanceof ViewGroup)) return true;
                    ViewGroup menu = (ViewGroup) first;
                    for (int i = 0; i < menu.getChildCount(); i++) {
                        View item = menu.getChildAt(i);
                        if (item instanceof com.google.android.material.navigation.NavigationBarItemView) {
                            com.google.android.material.navigation.NavigationBarItemView nb =
                                    (com.google.android.material.navigation.NavigationBarItemView) item;
                            nb.setItemPaddingTop(0);
                            nb.setItemPaddingBottom(0);
                        }
                        View icon = item.findViewById(com.google.android.material.R.id.navigation_bar_item_icon_view);
                        if (icon == null) continue;
                        // 找出要平移的容器：item 下、图标之上的最外层祖先
                        View target = icon;
                        while (target.getParent() != null && target.getParent() != item)
                            target = (View) target.getParent();
                        if (item.getHeight() <= 0) continue;
                        target.setTranslationY(0f);
                        int top = 0;
                        View v = icon;
                        while (v != item) {
                            top += v.getTop();
                            v = (View) v.getParent();
                        }
                        float iconCenter = top + icon.getHeight() / 2f;
                        float want = item.getHeight() / 2f - iconCenter; want += dp(5);
                        if (Math.abs(want) > 0.5f) target.setTranslationY(want);
                    }
                } catch (Exception ignored) {
                }
                return true;
            }
        });
    }

    private float dp(int v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private void checkAction(Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            VideoActivity.push(this, intent.getStringExtra(Intent.EXTRA_TEXT));
        } else if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            PermissionUtil.requestFile(this, allGranted -> checkType(intent));
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String keyword = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(keyword)) SearchActivity.start(this, keyword);
        }
    }

    private void checkType(Intent intent) {
        if ("text/plain".equals(intent.getType()) || UrlUtil.path(intent.getData()).endsWith(".m3u")) {
            loadLive("file:/" + FileChooser.getPathFromUri(intent.getData()));
        } else {
            VideoActivity.push(this, intent.getData().toString());
        }
    }

    private void initFragment(Bundle savedInstanceState) {
        mManager = new FragmentStateManager(mBinding.container, getSupportFragmentManager(), position -> switch (position) {
            case 0 -> VodFragment.newInstance();
            case 1 -> SettingFragment.newInstance();
            case 2 -> SettingPlayerFragment.newInstance();
            case 3 -> SettingDanmakuFragment.newInstance();
            case 4 -> SettingPreloadFragment.newInstance();
            case 5 -> SettingDecodeFragment.newInstance();
            case 6 -> SettingSubtitleFragment.newInstance();
            case 7 -> SettingAppearanceFragment.newInstance();
            case 8 -> SettingPersonalFragment.newInstance();
            default -> null;
        });
        if (savedInstanceState == null) change(0);
    }

    private void initConfig() {
        VodConfig.get().init().load(getCallback());
        LiveConfig.get().init().load();
        WallConfig.get().init();
    }

    private Callback getCallback() {
        return new Callback() {
            @Override
            public void success() {
                checkAction(getIntent());
            }

            @Override
            public void error(String msg) {
                checkAction(getIntent());
                StateEvent.empty();
                Notify.show(msg);
            }
        };
    }

    private void loadLive(String url) {
        LiveConfig.load(Config.find(url, 1), new Callback() {
            @Override
            public void success() {
                openLive();
            }
        });
    }

    private void setNavigation() {
        mBinding.navigation.getMenu().findItem(R.id.vod).setVisible(true);
        mBinding.navigation.getMenu().findItem(R.id.setting).setVisible(true);
        mBinding.navigation.getMenu().findItem(R.id.live).setVisible(LiveConfig.hasUrl());
    }

    private boolean openLive() {
        LiveActivity.start(this);
        return false;
    }

    private boolean addShortcut(View view) {
        ShortcutInfoCompat info = new ShortcutInfoCompat.Builder(this, getString(R.string.nav_live)).setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher)).setIntent(new Intent(Intent.ACTION_VIEW, null, this, LiveActivity.class)).setShortLabel(getString(R.string.nav_live)).build();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, ShortcutReceiver.class).setAction(ShortcutReceiver.ACTION), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        ShortcutManagerCompat.requestPinShortcut(this, info, pendingIntent.getIntentSender());
        return true;
    }

    /** 手势控制胶囊底栏：上滑隐藏、下滑显示（不依赖内部列表的嵌套滚动） */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    navTouchY = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dy = ev.getY() - navTouchY;
                    float limit = dp(24);
                    if (dy > limit) {
                        showNav();
                        navTouchY = ev.getY();
                    } else if (dy < -limit) {
                        hideNav();
                        navTouchY = ev.getY();
                    }
                    break;
            }
        } catch (Throwable ignored) {
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideNav() {
        if (navHidden) return;
        navHidden = true;
        View nav = mBinding.navWrap;
        nav.animate().cancel();
        nav.animate().translationY(nav.getHeight() > 0 ? nav.getHeight() : dp(80)).alpha(0f).setDuration(200).start();
    }

    private void showNav() {
        if (!navHidden) return;
        navHidden = false;
        View nav = mBinding.navWrap;
        nav.animate().cancel();
        nav.animate().translationY(0f).alpha(1f).setDuration(200).start();
    }

    public void change(int position) {
        showNav();
        if (position < 2) mBinding.navigation.setSelectedItemId(position == 0 ? R.id.vod : R.id.setting);
        else mManager.change(position);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigEvent(ConfigEvent event) {
        switch (event.type()) {
            case VOD:
                RefreshEvent.home();
                break;
            case COMMON:
                setNavigation();
                break;
            case BOOT:
                LiveActivity.start(this);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (event.getType() == RefreshEvent.Type.THEME) recreate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerEvent(ServerEvent event) {
        if (event.type() == ServerEvent.Type.PUSH) VideoActivity.push(this, event.text());
        if (event.type() == ServerEvent.Type.SEARCH) SearchActivity.start(this, event.text());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.setting) return mManager.change(1);
        if (item.getItemId() == R.id.vod) return mManager.change(0);
        if (item.getItemId() == R.id.live) return openLive();
        return false;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        App.post(() -> checkOrientation(newConfig), 100);
    }

    private void checkOrientation(Configuration newConfig) {
        if (orientation != newConfig.orientation) {
            orientation = newConfig.orientation;
            RefreshEvent.home();
        }
    }

    @Override
    protected void onBackInvoked() {
        if (!mBinding.navigation.getMenu().findItem(R.id.vod).isVisible()) {
            setNavigation();
        } else if (mManager.isVisible(4) || mManager.isVisible(5)) {
            change(2);
        } else if (mManager.isVisible(7) || mManager.isVisible(6) || mManager.isVisible(3) || mManager.isVisible(2) || mManager.isVisible(8)) {
            change(1);
        } else if (mManager.isVisible(1)) {
            change(0);
        } else if (mManager.canBack(0)) {
            if (PlaybackService.isRunning()) Util.moveToBackground(this);
            else super.onBackInvoked();
        }
    }

    @Override
    protected void onDestroy() {
        LiveConfig.get().clear();
        VodConfig.get().clear();
        AppDatabase.backup();
        OkHttp.get().clear();
        Source.get().exit();
        Server.get().stop();
        super.onDestroy();
    }
}
