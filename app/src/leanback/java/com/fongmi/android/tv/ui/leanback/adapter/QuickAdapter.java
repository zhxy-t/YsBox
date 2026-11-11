package com.fongmi.android.tv.ui.leanback.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.TvAdapterQuickBinding;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.ArrayList;
import java.util.List;

public class QuickAdapter extends RecyclerView.Adapter<QuickAdapter.ViewHolder> {

    private final OnClickListener mListener;
    private final List<Vod> mItems;
    private int width;
    private int nextFocusUp = -1;
    private int nextFocusDown = -1;

    public QuickAdapter(OnClickListener listener) {
        mListener = listener;
        mItems = new ArrayList<>();
        int space = ResUtil.dp2px(24) + ResUtil.dp2px(32);
        width = (ResUtil.getScreenWidth() - space) / 4;
    }

    public void addAll(List<Vod> items) {
        int start = mItems.size();
        mItems.addAll(items);
        notifyItemRangeInserted(start, items.size());
    }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public Vod get(int position) {
        return mItems.get(position);
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    /** 允许调用方按弹窗宽度覆盖默认列宽。 */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * 兼容调用方对焦点导航的设定。leanback 网格由 VerticalGridView 统一管理焦点，
     * 这里仅记录值，实际导航交给父容器。
     */
    public void setNextFocus(int nextFocusUp, int nextFocusDown) {
        this.nextFocusUp = nextFocusUp;
        this.nextFocusDown = nextFocusDown;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(TvAdapterQuickBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        holder.binding.getRoot().getLayoutParams().width = width;
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vod item = mItems.get(position);
        holder.binding.name.setText(item.getName());
        holder.binding.site.setText(item.getSiteName());
        holder.binding.remark.setText(item.getRemarks());
        holder.binding.getRoot().setOnClickListener(v -> mListener.onItemClick(item));
    }

    public interface OnClickListener {

        void onItemClick(Vod item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TvAdapterQuickBinding binding;

        ViewHolder(@NonNull TvAdapterQuickBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
