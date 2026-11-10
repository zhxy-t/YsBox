package com.fongmi.android.tv.ui.leanback.presenter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.bean.Func;
import com.fongmi.android.tv.databinding.AdapterFuncBinding;
import com.fongmi.android.tv.utils.ResUtil;

public class FuncPresenter extends Presenter {

    private final OnClickListener listener;

    public FuncPresenter(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener {
        void onItemClick(Func item);
    }

    /**
     * 首页功能块随屏幕自适应（按真实像素，不受 AutoSize 密度改写影响）：
     * 卡片 = 屏宽的 1/6，图标 = 卡片的 36%，字号随屏宽。
     * 1280 小屏约 213px 卡片、1920 电视约 320px、4K 约 640px，观感一致。
     */
    private void applySize(ViewHolder holder) {
        int screenW = ResUtil.getScreenWidth();
        int card = Math.max(ResUtil.dp2px(80), screenW / 6);
        int icon = Math.round(card * 0.36f);
        float text = Math.max(14f, screenW / 100f);

        ViewGroup.LayoutParams cardParams = holder.binding.getRoot().getLayoutParams();
        if (cardParams != null) {
            cardParams.width = card;
            cardParams.height = card;
            holder.binding.getRoot().setLayoutParams(cardParams);
        }
        ViewGroup.LayoutParams iconParams = holder.binding.icon.getLayoutParams();
        if (iconParams != null) {
            iconParams.width = icon;
            iconParams.height = icon;
            holder.binding.icon.setLayoutParams(iconParams);
        }
        holder.binding.text.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, text);
    }

    @NonNull
    @Override
    public Presenter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new ViewHolder(AdapterFuncBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Presenter.ViewHolder viewHolder, Object object) {
        Func item = (Func) object;
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.binding.text.setText(item.getText());
        holder.binding.icon.setImageResource(item.getDrawable());
        applySize(holder);
        setOnClickListener(holder, view -> listener.onItemClick(item));
    }

    @Override
    public void onUnbindViewHolder(@NonNull Presenter.ViewHolder viewHolder) {
    }

    public static class ViewHolder extends Presenter.ViewHolder {

        private final AdapterFuncBinding binding;

        public ViewHolder(@NonNull AdapterFuncBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
