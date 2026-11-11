package com.fongmi.android.tv.event;

import android.app.Activity;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

/**
 * 播放器已存活时原地换源：小程序点播放不再新开 Activity，
 * 由当前播放器实例接单（对齐海阔 8.83 PlayerChooser.hasPlayer 口径）。
 */
public record PlaySourceUpdateEvent(Activity activity, Intent intent) {

    public static void post(Activity activity, Intent intent) {
        EventBus.getDefault().post(new PlaySourceUpdateEvent(activity, intent));
    }
}
