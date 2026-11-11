package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.fongmi.android.tv.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.databinding.AdapterQualityBinding;

public class QualityAdapter extends RecyclerView.Adapter<QualityAdapter.ViewHolder> {

    private final OnClickListener listener;
    private Result result;
    private int position;

    public QualityAdapter(OnClickListener listener) {
        this.listener = listener;
        this.result = Result.empty();
    }
    private boolean detailMode;
    /** 详情页宿主打开：chip 用深色 scrim 底，保证浅色壁纸下白字可读 */
    public QualityAdapter detailMode() {
        this.detailMode = true;
        return this;
    }

    public interface OnClickListener {

        void onItemClick(Result result);
    }

    public void addAll(Result result) {
        this.result = result;
        notifyDataSetChanged();
    }

    public int getPosition() {
        return position;
    }

    @Override
    public int getItemCount() {
        return result.getUrl().getValues().size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(AdapterQualityBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        if (detailMode) {
            holder.binding.text.setBackgroundResource(R.drawable.selector_item_dark);
            holder.binding.text.setTextColor(androidx.appcompat.content.res.AppCompatResources.getColorStateList(parent.getContext(), R.color.selector_detail_text_dark));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.text.setText(result.getUrl().n(position));
        holder.binding.text.setOnClickListener(v -> onItemClick(position));
        holder.binding.text.setSelected(result.getUrl().getPosition() == position);
    }

    private void onItemClick(int position) {
        this.position = position;
        result.getUrl().set(position);
        listener.onItemClick(result);
        notifyItemRangeChanged(0, getItemCount());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterQualityBinding binding;

        ViewHolder(@NonNull AdapterQualityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}