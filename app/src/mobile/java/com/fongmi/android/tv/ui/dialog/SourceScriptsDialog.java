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
 */
public class SourceScriptsDialog extends Dialog {

    private final OnScriptSelectListener listener;
    private File currentFile;
    private final List<File> allPyFiles = new ArrayList<>();
    private final List<File> allJsFiles = new ArrayList<>();
    private ScriptAdapter pyAdapter;
    private ScriptAdapter jsAdapter;

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
        setContentView(android.view.LayoutInflater.from(com.fongmi.android.tv.ui.dialog.DialogThemeHelper.wrap(getContext())).inflate(R.layout.dialog_source_scripts, null));
        if (getWindow() != null) getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        try {
            if (getWindow() != null) {
                com.fongmi.android.tv.ui.dialog.DialogThemeHelper.applyStandardWindow(getWindow());
                getWindow().setGravity(android.view.Gravity.CENTER);
                android.view.WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.78f);
                lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
                lp.softInputMode = android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN;
                getWindow().setAttributes(lp);
            }
        } catch (Exception ignored) {
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
            rvPy.setAdapter(pyAdapter);

            rvJs.setLayoutManager(new LinearLayoutManager(getContext()));
            jsAdapter = new ScriptAdapter(allJsFiles, f -> {
                if (listener != null) listener.onScriptSelect(f, false);
                dismiss();
            });
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
