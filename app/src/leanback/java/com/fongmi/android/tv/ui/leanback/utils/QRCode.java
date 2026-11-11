package com.fongmi.android.tv.ui.leanback.utils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import java.util.EnumMap;
import java.util.Map;
/** 标准黑白二维码：白底 + 黑色方块模块，保证任意扫码器可识别。 */
public class QRCode {
    public static Bitmap getBitmap(String content, int size, int margin) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, Math.max(margin, 2));
            int px = Math.max(240, ResUtil.dp2px(size));
            BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, px, px, hints);
            int w = matrix.getWidth(), h = matrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) if (matrix.get(x, y)) canvas.drawRect(x, y, x + 1, y + 1, paint);
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }
}
