package com.fongmi.android.tv.ai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.fongmi.android.tv.event.RefreshEvent;

/**
 * AI 动作执行器。
 * 根据当前页面执行语音/文字指令：播放页走 VideoActivity，导航/开关类全局可用。
 */
public class AiActionExecutor {

    public interface Callback {
        void onFeedback(String text);
    }

    /** 本地指令直接执行 */
    public static void executeLocal(Context context, AiVoiceCommand.Command cmd, Callback cb) {
        if (cmd == null) {
            if (cb != null) cb.onFeedback("没听懂，换个说法试试");
            return;
        }
        if (isNavCommand(cmd)) {
            boolean done = navigate(context, cmd);
            if (cb != null) cb.onFeedback(done ? cmd.feedback : "打开失败，请重试");
            return;
        }
        if (isSwitchCommand(cmd)) {
            String feedback = executeSwitch(context, cmd);
            if (cb != null) cb.onFeedback(feedback);
            return;
        }
        Activity activity = findTopActivity(context);
        boolean done = activity != null && dispatchToActivity(activity, cmd);
        if (cb != null) cb.onFeedback(done ? cmd.feedback : "请在播放页面使用该指令");
    }

    private static boolean isNavCommand(AiVoiceCommand.Command cmd) {
        return cmd != null && cmd.action != null && cmd.action.startsWith("nav_");
    }

    private static boolean isSwitchCommand(AiVoiceCommand.Command cmd) {
        if (cmd == null || cmd.action == null) return false;
        String a = cmd.action;
        return a.equals(AiVoiceCommand.ACTION_FLOAT_ON)
                || a.equals(AiVoiceCommand.ACTION_FLOAT_OFF)
                || a.equals(AiVoiceCommand.ACTION_PY_REFRESH);
    }

    /**
     * 全局导航：用 Intent 跳转对应 Activity（Class.forName，因目标在 mobile 源集）。
     */
    private static boolean navigate(Context context, AiVoiceCommand.Command cmd) {
        try {
            String pkg = "com.fongmi.android.tv.ui.activity.";
            String cls;
            Intent intent;
            switch (cmd.action) {
                case AiVoiceCommand.ACTION_NAV_HOME:
                    cls = pkg + "HomeActivity";
                    intent = new Intent(context, Class.forName(cls))
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    break;
                case AiVoiceCommand.ACTION_NAV_LIVE:
                    cls = pkg + "LiveActivity";
                    intent = new Intent(context, Class.forName(cls));
                    break;
                case AiVoiceCommand.ACTION_NAV_MINIAPP:
                    cls = pkg + "HkPageActivity";
                    intent = new Intent(context, Class.forName(cls));
                    break;
                case AiVoiceCommand.ACTION_NAV_SEARCH:
                    cls = pkg + "SearchActivity";
                    intent = new Intent(context, Class.forName(cls));
                    if (!TextUtils.isEmpty(cmd.textParam)) intent.putExtra("keyword", cmd.textParam);
                    break;
                case AiVoiceCommand.ACTION_NAV_HISTORY:
                    cls = pkg + "HistoryActivity";
                    intent = new Intent(context, Class.forName(cls));
                    break;
                case AiVoiceCommand.ACTION_NAV_KEEP:
                    cls = pkg + "KeepActivity";
                    intent = new Intent(context, Class.forName(cls));
                    break;
                case AiVoiceCommand.ACTION_NAV_DOWNLOAD:
                    cls = pkg + "DownloadActivity";
                    intent = new Intent(context, Class.forName(cls));
                    break;
                case AiVoiceCommand.ACTION_NAV_PLUGIN:
                    cls = pkg + "PluginActivity";
                    intent = new Intent(context, Class.forName(cls));
                    break;
                case AiVoiceCommand.ACTION_NAV_LAB:
                    cls = pkg + "LabActivity";
                    intent = new Intent(context, Class.forName(cls));
                    break;
                case AiVoiceCommand.ACTION_NAV_CLOUD:
                    cls = pkg + "PanLoginActivity";
                    intent = new Intent(context, Class.forName(cls));
                    break;
                case AiVoiceCommand.ACTION_NAV_FEATURES:
                    cls = pkg + "EnhancedActivity";
                    intent = new Intent(context, Class.forName(cls));
                    break;
                case AiVoiceCommand.ACTION_NAV_AI_SETTINGS:
                    cls = pkg + "AiSettingsActivity";
                    intent = new Intent(context, Class.forName(cls));
                    break;
                case AiVoiceCommand.ACTION_NAV_SETTINGS:
                case AiVoiceCommand.ACTION_NAV_PLAYER_SET:
                case AiVoiceCommand.ACTION_NAV_UI_SET:
                case AiVoiceCommand.ACTION_NAV_NODE:
                case AiVoiceCommand.ACTION_NAV_NODE_TEST:
                case AiVoiceCommand.ACTION_NAV_BOOKSHELF:
                case AiVoiceCommand.ACTION_NAV_CONTINUE_READING:
                case AiVoiceCommand.ACTION_NAV_BOOKSOURCE:
                    cls = pkg + "HomeActivity";
                    intent = new Intent(context, Class.forName(cls))
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    break;
                default:
                    return false;
            }
            if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 全局开关指令执行。返回反馈文案。
     */
    private static String executeSwitch(Context context, AiVoiceCommand.Command cmd) {
        try {
            switch (cmd.action) {
                case AiVoiceCommand.ACTION_FLOAT_ON: {
                    AiStore.setFloatEnabled(context, true);
                    Activity top = findTopActivity(context);
                    if (top != null) AiFloatManager.get().refresh(top);
                    return "悬浮窗已打开";
                }
                case AiVoiceCommand.ACTION_FLOAT_OFF: {
                    AiStore.setFloatEnabled(context, false);
                    Activity top = findTopActivity(context);
                    if (top != null) AiFloatManager.get().refresh(top);
                    return "悬浮窗已关闭";
                }
                case AiVoiceCommand.ACTION_PY_REFRESH: {
                    RefreshEvent.home();
                    return "正在刷新首页";
                }
                default:
                    return "不支持的操作";
            }
        } catch (Exception e) {
            return "执行失败，请重试";
        }
    }

    private static Activity findTopActivity(Context context) {
        if (context instanceof Activity) return (Activity) context;
        try {
            return AiActivityTracker.top();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean dispatchToActivity(Activity activity, AiVoiceCommand.Command cmd) {
        try {
            Class<?> videoCls = Class.forName("com.fongmi.android.tv.ui.activity.VideoActivity");
            if (videoCls.isInstance(activity)) return dispatchToVideo(activity, cmd);
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean dispatchToVideo(Activity activity, AiVoiceCommand.Command cmd) {
        try {
            switch (cmd.action) {
                case AiVoiceCommand.ACTION_NEXT:
                    invoke(activity, "aiNext");
                    return true;
                case AiVoiceCommand.ACTION_PREV:
                    invoke(activity, "aiPrev");
                    return true;
                case AiVoiceCommand.ACTION_TOGGLE_PLAY:
                    invoke(activity, "aiTogglePlay");
                    return true;
                case AiVoiceCommand.ACTION_SEEK_BY:
                    invoke(activity, "aiSeekBy", new Class[]{long.class}, cmd.param);
                    return true;
                case AiVoiceCommand.ACTION_VOLUME_UP:
                    invoke(activity, "aiVolumeUp");
                    return true;
                case AiVoiceCommand.ACTION_VOLUME_DOWN:
                    invoke(activity, "aiVolumeDown");
                    return true;
                case AiVoiceCommand.ACTION_SPEED_UP:
                    invoke(activity, "aiSpeedUp");
                    return true;
                case AiVoiceCommand.ACTION_SPEED_DOWN:
                    invoke(activity, "aiSpeedDown");
                    return true;
                case AiVoiceCommand.ACTION_SPEED_SET:
                    invoke(activity, "aiSetSpeed", new Class[]{float.class}, cmd.param / 100f);
                    return true;
                case AiVoiceCommand.ACTION_BRIGHT_UP:
                    invoke(activity, "aiBrightUp");
                    return true;
                case AiVoiceCommand.ACTION_BRIGHT_DOWN:
                    invoke(activity, "aiBrightDown");
                    return true;
                case AiVoiceCommand.ACTION_DANMAKU:
                    invoke(activity, "aiDanmakuSet", new Class[]{int.class}, (int) cmd.param);
                    return true;
                case AiVoiceCommand.ACTION_SUBTITLE:
                    invoke(activity, "aiSubtitleSet", new Class[]{int.class}, (int) cmd.param);
                    return true;
                case AiVoiceCommand.ACTION_AI_SUBTITLE:
                    invoke(activity, "aiAiSubtitleSet", new Class[]{int.class}, (int) cmd.param);
                    return true;
                case AiVoiceCommand.ACTION_EPISODE:
                    invoke(activity, "aiPlayEpisode", new Class[]{int.class}, (int) cmd.param);
                    return true;
                case AiVoiceCommand.ACTION_CAST:
                    invoke(activity, "aiCast");
                    return true;
                case AiVoiceCommand.ACTION_DL_EPISODE:
                    invoke(activity, "aiDownloadEpisode");
                    return true;
                case AiVoiceCommand.ACTION_FULLSCREEN:
                    invoke(activity, "aiEnterFullscreen");
                    return true;
                case AiVoiceCommand.ACTION_EXIT_FULLSCREEN:
                    invoke(activity, "aiExitFullscreen");
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static void invoke(Activity activity, String method) throws Exception {
        activity.getClass().getMethod(method).invoke(activity);
    }

    private static void invoke(Activity activity, String method, Class<?>[] types, Object... args) throws Exception {
        activity.getClass().getMethod(method, types).invoke(activity, args);
    }
}
