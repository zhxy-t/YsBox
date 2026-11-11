package com.fongmi.android.tv.ui.dialog;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

/**
 * 弹窗按钮统一边框/点击态构建器（与 lab 模块共用同一套规则）。
 *
 * 背景用 StateListDrawable 做两态，比 colorSelector 更容易表现「边框→填充」的变化：
 *  - 常态：透明底 + 1.5dp 风格色边框
 *  - 按下/选中：风格色填充（霓虹为三色渐变）
 */
public final class DialogButtonFactory {

    private DialogButtonFactory() {
    }

    /**
     * @param accent   风格强调色
     * @param radiusDp 圆角 dp
     */
    public static StateListDrawable outline(int accent, int radiusDp, float density) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed}, filled(accent, radiusDp, density));
        s.addState(new int[]{android.R.attr.state_focused}, filled(accent, radiusDp, density));
        s.addState(new int[]{android.R.attr.state_selected}, filled(accent, radiusDp, density));
        s.addState(new int[]{}, stroke(accent, radiusDp, density));
        return s;
    }

    /** 常态：透明底 + 边框 */
    public static GradientDrawable stroke(int accent, int radiusDp, float density) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(0x00000000);
        d.setCornerRadius(radiusDp * density);
        d.setStroke(Math.max(1, Math.round(1.5f * density)), accent);
        return d;
    }

    /** 按下：实心填充 */
    public static GradientDrawable filled(int accent, int radiusDp, float density) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(accent);
        d.setCornerRadius(radiusDp * density);
        return d;
    }

    /** 按下：三色渐变填充（霓虹） */
    public static GradientDrawable gradient(int[] colors, int radiusDp, float density) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadius(radiusDp * density);
        return d;
    }
}
