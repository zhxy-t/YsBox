package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ItemDownloadTaskBinding;
import com.fongmi.android.tv.utils.download.DownloadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DownloadTaskAdapter extends RecyclerView.Adapter<DownloadTaskAdapter.ViewHolder> {

    public interface Listener {

        void onCancel(DownloadTask task);

        void onRetry(DownloadTask task);

        void onDelete(DownloadTask task);
    }

    private final List<DownloadTask> items = new ArrayList<>();
    private final Listener listener;

    public DownloadTaskAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<DownloadTask> values) {
        items.clear();
        items.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemDownloadTaskBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloadTask item = items.get(position);
        holder.binding.name.setText(item.getName());
        holder.binding.url.setText(item.getUrl());
        holder.binding.status.setText(statusText(holder, item));
        holder.binding.progress.setProgress(item.getPercent());
        holder.binding.size.setText(sizeText(item));
        holder.binding.retry.setVisibility(item.getStatus() == DownloadTask.ERROR || item.getStatus() == DownloadTask.CANCELED ? View.VISIBLE : View.GONE);
        holder.binding.cancel.setVisibility(item.isFinished() ? View.GONE : View.VISIBLE);
        holder.binding.cancel.setOnClickListener(view -> listener.onCancel(item));
        holder.binding.retry.setOnClickListener(view -> listener.onRetry(item));
        holder.binding.delete.setOnClickListener(view -> listener.onDelete(item));
    }

    private String statusText(ViewHolder holder, DownloadTask item) {
        int resId;
        switch (item.getStatus()) {
            case DownloadTask.RUNNING:
                return holder.binding.getRoot().getContext().getString(R.string.download_running) + " " + item.getPercent() + "%";
            case DownloadTask.DONE:
                resId = R.string.download_done;
                break;
            case DownloadTask.ERROR:
                resId = R.string.download_error;
                break;
            case DownloadTask.CANCELED:
                resId = R.string.download_canceled;
                break;
            default:
                resId = R.string.download_waiting;
                break;
        }
        return holder.binding.getRoot().getContext().getString(resId);
    }

    private String sizeText(DownloadTask item) {
        if (item.getTotal() > 0) return format(item.getDownloaded()) + " / " + format(item.getTotal());
        if (item.getDownloaded() > 0) return format(item.getDownloaded());
        return "";
    }

    private static String format(long size) {
        if (size <= 0) return "0 B";
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int group = (int) (Math.log10(size) / Math.log10(1024));
        return String.format(Locale.US, "%.1f %s", size / Math.pow(1024, group), units[group]);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemDownloadTaskBinding binding;

        ViewHolder(ItemDownloadTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
