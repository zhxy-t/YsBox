package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fongmi.android.tv.R;
import java.io.File;
import java.util.List;

public class ScriptAdapter extends RecyclerView.Adapter<ScriptAdapter.ViewHolder> {
    private List<File> files;
    private final OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(File file);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(File file);
    }

    public ScriptAdapter(List<File> files, OnItemClickListener listener) {
        this.files = files;
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        this.longClickListener = l;
    }

    public void updateData(List<File> newFiles) {
        this.files = newFiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 布局里不再用 ?attr，直接 inflate 即可，颜色在 onBindViewHolder 里按主题设置
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_script, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File f = files.get(position);
        holder.name.setText(f.getName());
        // 直接按当前弹窗风格着色，不依赖 XML 里的 ?attr（避免 context 无主题时崩溃）
        android.content.Context ctx = holder.itemView.getContext();
        int textColor = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.primaryText(ctx);
        int bgRes = com.fongmi.android.tv.ui.dialog.DialogThemeHelper.resolveResId(ctx, com.fongmi.android.tv.R.attr.dialogCardSurface);
        holder.name.setTextColor(textColor);
        if (bgRes != 0) holder.name.setBackgroundResource(bgRes);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(f);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(f);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_script_name);
        }
    }
}
