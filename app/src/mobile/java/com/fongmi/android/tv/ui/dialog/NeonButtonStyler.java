package com.fongmi.android.tv.ui.dialog;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.widget.TextView;

import com.fongmi.android.tv.setting.Setting;

/**
 * 霓虹风格专用：给按钮（TextView / MaterialButton）做“按下时边框＋文字变渐变”的效果。
 *
 * 背景由 drawable/dialog_btn_style_neon.xml 负责（常态淡边框、按下渐变边框），
 * 文字渐变无法用 colorStateList 表达，必须用 LinearGradient 着色器，故放在这里。
 *
 * 只在 Setting.getDialogStyle()==4（霓虹）时生效，其它风格直接 return，不影响原观感。
 */
public final class NeonButtonStyler {

    /** 渐变三色，与 bg_dialog_neon.xml 完全一致 */
    private static final int[] COLORS = {0xFF00D4FF, 0xFF7B61FF, 0xFFFF006E};

    private NeonButtonStyler() {
    }

    public static boolean isNeon() {
        return Setting.getDialogStyle() == 4;
    }

    /**
     * 给按钮装上“按下变渐变”的触摸行为。
     * 注意：不拦截点击事件（返回 false），只负责在按下/抬起时切换着色器。
     */
    public static void apply(TextView button) {
        if (button == null || !isNeon()) return;
        button.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    applyGradient((TextView) v, true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    applyGradient((TextView) v, false);
                    break;
                default:
                    break;
            }
            return false;
        });
        // 键盘/遥控器选中时也变渐变
        button.setOnFocusChangeListener((v, hasFocus) -> applyGradient((TextView) v, hasFocus));
    }

    /**
     * @param on true=渐变字，false=恢复常态白字
     */
    public static void applyGradient(TextView tv, boolean on) {
        if (tv == null) return;
        if (!on) {
            tv.getPaint().setShader(null);
            tv.invalidate();
            return;
        }
        tv.post(() -> {
            int w = tv.getPaint() == null ? 0 : (int) Math.ceil(tv.getPaint().measureText(tv.getText() == null ? "" : tv.getText().toString()));
            if (w <= 0) w = tv.getWidth();
            if (w <= 0) w = 1;
            LinearGradient shader = new LinearGradient(0, 0, w, 0, COLORS, null, Shader.TileMode.CLAMP);
            tv.getPaint().setShader(shader);
            tv.invalidate();
        });
    }
}
