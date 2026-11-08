package com.fongmi.android.tv.ui.dialog;

import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.ui.adapter.ScriptAdapter;
import com.github.catvod.utils.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 源脚本选择器（移植自 xingchen 分支）：搜索框 + PY/JS 双列。
 *
 * 尺寸规则与「站点」弹窗（SiteDialog）完全一致：
 * 宽 = min(屏宽 * 0.92, 620dp)，高 = wrap_content（列表在布局里用 app:maxHeight 限高）。
 * 因此本弹窗与站点弹窗视觉上同宽同高，不再单独定一套尺寸。
 */
public class SourceScriptsDialog extends Dialog {

    private final OnScriptSelectListener listener;
    private File currentFile;
    private final List<File> allPyFiles = new ArrayList<>();
    private final List<File> allJsFiles = new ArrayList<>();
    private ScriptAdapter pyAdapter;
    private ScriptAdapter jsAdapter;

    /** 脚本被点击（用于填入路径） */
    public interface OnScriptSelectListener {
        void onScriptSelect(File file, boolean isPy);
    }

    public SourceScriptsDialog(@NonNull Context context, OnScriptSelectListener listener) {
        super(context, DialogThemeHelper.getTheme());
        this.listener = listener;
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    public void setCurrentFile(File file) {
        this.currentFile = file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(android.view.LayoutInflater
                .from(com.fongmi.android.tv.ui.dialog.DialogThemeHelper.wrap(getContext()))
                .inflate(R.layout.dialog_source_scripts, null));

        if (getWindow() != null) {
            com.fongmi.android.tv.ui.dialog.DialogThemeHelper.applyStandardWindow(getWindow());
            getWindow().setGravity(android.view.Gravity.CENTER);
            applySameSizeAsSiteDialog();
        }

        RecyclerView rvPy = findViewById(R.id.rv_py);
        RecyclerView rvJs = findViewById(R.id.rv_js);
        LinearLayout colPy = findViewById(R.id.column_py);
        LinearLayout colJs = findViewById(R.id.column_js);
        LinearLayout layoutColumns = findViewById(R.id.layout_columns);
        TextView tvEmpty = findViewById(R.id.tv_empty);

        allPyFiles.addAll(loadScripts(true));
        allJsFiles.addAll(loadScripts(false));
        boolean hasPy = !allPyFiles.isEmpty();
        boolean hasJs = !allJsFiles.isEmpty();

        if (!hasPy && !hasJs) {
            layoutColumns.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutColumns.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            if (hasPy && hasJs) {
                colPy.setVisibility(View.VISIBLE);
                colJs.setVisibility(View.VISIBLE);
            } else if (hasPy) {
                colPy.setVisibility(View.VISIBLE);
                colJs.setVisibility(View.GONE);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) colPy.getLayoutParams();
                lp.weight = 1;
                lp.setMarginEnd(0);
                colPy.setLayoutParams(lp);
            } else {
                colPy.setVisibility(View.GONE);
                colJs.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) colJs.getLayoutParams();
                lp.weight = 1;
                lp.setMarginStart(0);
                colJs.setLayoutParams(lp);
            }

            rvPy.setLayoutManager(new LinearLayoutManager(getContext()));
            pyAdapter = new ScriptAdapter(allPyFiles, f -> {
                if (listener != null) listener.onScriptSelect(f, true);
                dismiss();
            });
            // 长按 → 预览弹窗（能看源码 + 复制）
            pyAdapter.setOnItemLongClickListener(this::showPreview);
            rvPy.setAdapter(pyAdapter);

            rvJs.setLayoutManager(new LinearLayoutManager(getContext()));
            jsAdapter = new ScriptAdapter(allJsFiles, f -> {
                if (listener != null) listener.onScriptSelect(f, false);
                dismiss();
            });
            jsAdapter.setOnItemLongClickListener(this::showPreview);
            rvJs.setAdapter(jsAdapter);

            scrollToCurrent(rvPy, allPyFiles);
            scrollToCurrent(rvJs, allJsFiles);
        }

        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilter(s.toString());
            }
        });
    }

    /** 长按条目：弹出源码预览 */
    private void showPreview(File file) {
        try {
            new ScriptPreviewDialog(getContext(), file).show();
        } catch (Exception ignored) {
        }
    }

    /**
     * 与 SiteDialog 共用同一套尺寸规则（用户要求：源脚本弹窗按站点弹窗来）。
     * 规则集中在 DialogThemeHelper.applyListDialogSize()，此处只做委托，
     * 以后改尺寸只改一个地方，两个弹窗不会跑偏。
     */
    private void applySameSizeAsSiteDialog() {
        DialogThemeHelper.applyListDialogSize(getWindow());
    }

    private void applyFilter(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        if (pyAdapter != null) pyAdapter.updateData(filterFiles(allPyFiles, kw));
        if (jsAdapter != null) jsAdapter.updateData(filterFiles(allJsFiles, kw));
    }

    private List<File> filterFiles(List<File> files, String kw) {
        if (kw.isEmpty()) return new ArrayList<>(files);
        List<File> result = new ArrayList<>();
        for (File f : files) if (f.getName().toLowerCase().contains(kw)) result.add(f);
        return result;
    }

    private List<File> loadScripts(boolean py) {
        Map<String, File> map = new LinkedHashMap<>();
        addDir(map, new File(getContext().getFilesDir(), py ? "plugins/py" : "plugins/js"), py);
        addDir(map, py ? Path.py() : Path.js(), py);
        List<File> list = new ArrayList<>(map.values());
        list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return list;
    }

    private void addDir(Map<String, File> map, File dir, boolean py) {
        if (dir == null || !dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return py ? lower.endsWith(".py") : lower.endsWith(".js") || lower.endsWith(".wv");
        });
        if (files == null) return;
        for (File file : files) map.put(file.getAbsolutePath(), file);
    }

    private void scrollToCurrent(RecyclerView rv, List<File> files) {
        if (currentFile == null || files.isEmpty()) return;
        String cur = currentFile.getAbsolutePath();
        for (int i = 0; i < files.size(); i++) {
            if (cur.equals(files.get(i).getAbsolutePath())) {
                rv.scrollToPosition(i);
                break;
            }
        }
    }
}
