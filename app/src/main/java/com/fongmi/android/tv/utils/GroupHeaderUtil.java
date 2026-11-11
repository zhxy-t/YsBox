package com.fongmi.android.tv.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fongmi.android.tv.setting.Setting;

/**
 * 统一设置分组标题颜色：默认白色，设置主题色后跟随主题色；
 * 直接删除每个页面最顶部的分组标题（不是隐藏，不留占位与额外留白）；
 * 同时收紧分组标题与紧随其后功能项之间的间距。
 */
public final class GroupHeaderUtil {

    public static final String TAG = "group_header";
    private static final int ITEM_GAP_DP = 8;
    private static final int DEFAULT_COLOR = 0xFFFFFFFF;

    private GroupHeaderUtil() {
    }

    public static void apply(View root) {
        View first = first(root);
        if (first != null) remove(first);
        int theme = Setting.getDynamicColor();
        applyColor(root, theme != 0 ? theme : DEFAULT_COLOR);
    }

    private static View first(View view) {
        if (view instanceof TextView text && TAG.equals(text.getTag())) return text;
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = first(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void remove(View header) {
        if (header.getParent() instanceof ViewGroup group) group.removeView(header);
    }

    private static void applyColor(View view, int color) {
        if (view instanceof TextView text && TAG.equals(text.getTag())) {
            text.setTextColor(color);
            tighten(text);
        }
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) applyColor(group.getChildAt(i), color);
        }
    }

    private static void tighten(View header) {
        if (!(header.getParent() instanceof ViewGroup group)) return;
        int index = group.indexOfChild(header);
        if (index < 0 || index + 1 >= group.getChildCount()) return;
        View next = group.getChildAt(index + 1);
        if (!(next.getLayoutParams() instanceof LinearLayout.LayoutParams params)) return;
        int gap = (int) (ITEM_GAP_DP * next.getResources().getDisplayMetrics().density);
        if (params.topMargin > gap) {
            params.topMargin = gap;
            next.setLayoutParams(params);
        }
    }
}
