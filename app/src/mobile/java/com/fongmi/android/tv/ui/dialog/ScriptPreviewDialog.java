package com.fongmi.android.tv.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * 源脚本预览弹窗：长按源脚本列表项后弹出，能看到脚本源码，并可一键复制。
 *
 * 尺寸与 SiteDialog / SourceScriptsDialog 保持同一套规则：
 * 宽 = min(屏宽 * 0.92, 620dp)，高 = 屏高 * 0.8（内容最多这个高度，内部滚动）。
 * 配色全部走 ?attr 语义色，因此跟随「弹窗风格」设置。
 */
public class ScriptPreviewDialog extends Dialog {

    /** 最多读这么多字节，避免超大脚本把弹窗卡死 */
    private static final int MAX_BYTES = 200 * 1024;

    private final File file;
    private String content = "";

    public ScriptPreviewDialog(@NonNull Context context, File file) {
        super(context, DialogThemeHelper.getTheme());
        this.file = file;
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(android.view.LayoutInflater
                .from(DialogThemeHelper.wrap(getContext()))
                .inflate(R.layout.dialog_script_preview, null));

        if (getWindow() != null) {
            DialogThemeHelper.applyStandardWindow(getWindow());
            getWindow().setGravity(Gravity.CENTER);
            DialogThemeHelper.applyListDialogSize(getWindow());
        }

        content = readFile(file);

        TextView tvPath = findViewById(R.id.tv_path);
        TextView tvContent = findViewById(R.id.tv_content);
        TextView btnCopy = findViewById(R.id.btn_copy);

        if (tvPath != null) tvPath.setText(file == null ? "" : file.getAbsolutePath());
        if (tvContent != null) tvContent.setText(content.isEmpty() ? "（空文件）" : content);

        if (btnCopy != null) {
            btnCopy.setClickable(true);
            btnCopy.setFocusable(true);
            btnCopy.setOnClickListener(v -> Util.copy(content));
            NeonButtonStyler.apply(btnCopy);
        }

        // 源码区限高：内容再长也只占屏幕一半，超出部分在内部滚动，
        // 不会把弹窗撑到顶状态栏（与源脚本弹窗的 376dp 列表上限同一思路）。
        View scroll = findViewById(R.id.scroll);
        if (scroll != null) {
            int maxH = Math.round(getContext().getResources().getDisplayMetrics().heightPixels * 0.5f);
            scroll.post(() -> {
                if (scroll.getHeight() > maxH) {
                    ViewGroup.LayoutParams lp = scroll.getLayoutParams();
                    lp.height = maxH;
                    scroll.setLayoutParams(lp);
                }
            });
        }
    }

    private String readFile(File f) {
        if (f == null || !f.isFile()) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            char[] buf = new char[4096];
            int len;
            while ((len = reader.read(buf)) > 0) {
                sb.append(buf, 0, len);
                if (sb.length() > MAX_BYTES) {
                    sb.append("\n\n…（内容过长，已截断）");
                    break;
                }
            }
        } catch (Exception e) {
            return "（读取失败：" + e.getMessage() + "）";
        }
        return sb.toString();
    }
}
