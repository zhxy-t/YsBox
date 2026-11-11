package com.fongmi.android.tv;

import android.content.Context;
import android.os.Build;
import android.view.DisplayCutout;

import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.setting.PlayerSetting;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.AbsoluteCornerSize;
import com.google.android.material.shape.RelativeCornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;

public class Product {

    public static int getDeviceType() {
        return com.fongmi.android.tv.ui.UiMode.isLeanback() ? 0 : 1;
    }

    public static int getColumn(Context context) {
        int count = ResUtil.isLand(context) ? 7 : 5;
        count = count + (ResUtil.isPad() ? 1 : 0);
        return Math.abs(PlayerSetting.getSize() - count);
    }

    public static int getColumn(Context context, Style style) {
        style = getCoverStyle(style);
        return style.isLand() ? getColumn(context) - 1 : getColumn(context);
    }

    public static Style getCoverStyle(Style style) {
        if (style == null || style.isList()) return style;
        if (PlayerSetting.getCoverRadius() == PlayerSetting.RADIUS_CIRCLE) return new Style("oval", 1f);
        float ratio = PlayerSetting.getCoverRatioValue();
        return ratio <= 0 ? style : new Style("rect", ratio);
    }

    public static void applyCoverShape(ShapeableImageView image) {
        int radius = PlayerSetting.getCoverRadius();
        if (radius == PlayerSetting.RADIUS_FOLLOW) return;
        ShapeAppearanceModel.Builder builder = new ShapeAppearanceModel.Builder();
        if (radius == PlayerSetting.RADIUS_CIRCLE) builder.setAllCornerSizes(new RelativeCornerSize(0.5f));
        else builder.setAllCornerSizes(new AbsoluteCornerSize(ResUtil.dp2px(PlayerSetting.getCoverRadiusDp())));
        image.setShapeAppearanceModel(builder.build());
    }

    public static int[] getSpec(Context context) {
        return getSpec(context, Style.rect());
    }

    public static int[] getSpec(Context context, int column) {
        Style style = getCoverStyle(Style.rect());
        int space = ResUtil.dp2px(32) + ResUtil.dp2px(16 * (column - 1)) + getCutout(context);
        return getSpec(context, space, column, style);
    }

    public static int[] getSpec(Context context, Style style) {
        style = getCoverStyle(style);
        int column = getColumn(context, style);
        int space = ResUtil.dp2px(32) + ResUtil.dp2px(16 * (column - 1)) + getCutout(context);
        if (style.isOval()) space += ResUtil.dp2px(column * 16);
        return getSpec(context, space, column, style);
    }

    private static int[] getSpec(Context context, int space, int column, Style style) {
        int base = ResUtil.getScreenWidth(context) - space;
        int width = base / column;
        int height = (int) (width / style.getRatio());
        return new int[]{width, height};
    }

    private static int getCutout(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return 0;
        DisplayCutout cutout = ResUtil.getDisplay(context).getCutout();
        if (cutout == null) return 0;
        int left = cutout.getSafeInsetLeft();
        int right = cutout.getSafeInsetRight();
        return left | right;
    }
}
