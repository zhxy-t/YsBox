package com.fongmi.android.tv.ui.leanback.presenter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.App;
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
     * 首页功能块随屏幕自适应：
     * 以 960dp（≈1920 宽 TV）为基准按屏宽 dp 等比缩放，
     * 避免 1280 屏上图标挤成一团、4K 屏上又显得过小。
     */
    private static float scaleFactor() {
        float widthDp = ResUtil.getScreenWidth() / App.get().getResources().getDisplayMetrics().density;
        if (widthDp <= 0) return 1f;
        float factor = widthDp / 960f;
        // 限制在 0.75x ~ 1.5x，避免极端分辨率下尺寸失真
        return Math.max(0.75f, Math.min(factor, 1.5f));
    }

    private static int cardSize() {
        return Math.round(ResUtil.dp2px(110) * scaleFactor());
    }

    private static int iconSize() {
        return Math.round(ResUtil.dp2px(40) * scaleFactor());
    }

    private static float textSize() {
        return 18f * scaleFactor();
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

    /** 按当前屏幕把卡片、图标、字号统一缩放 */
    private void applySize(ViewHolder holder) {
        int card = cardSize();
        int icon = iconSize();
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
        holder.binding.text.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, textSize());
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
