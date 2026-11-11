package com.fongmi.android.tv.read;

import android.content.Context;

import com.fongmi.android.tv.utils.Notify;

/**
 * 阅读器路由适配层。
 * <p>
 * trae 分支未移植独立阅读器（小说/漫画/听书/TTS）组件，这里保留
 * {@code ReadingRouter} 的最小接口，海阔小程序仍可正常完成详情解析与会话准备，
 * 仅在真正进入阅读器时提示该能力未集成，不影响小程序主体（规则/WebView/搜索/收藏/历史/播放）。
 */
public class ReadingRouter {

    public static void openReading(Context context, ReadingSession session) {
        Notify.show("当前版本未集成阅读器");
    }

    public static void openReadingWithStartPage(Context context, ReadingSession session, int startPage) {
        Notify.show("当前版本未集成阅读器");
    }
}
