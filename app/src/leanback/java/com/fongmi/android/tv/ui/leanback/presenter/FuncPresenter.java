package com.fongmi.android.tv.ui.leanback.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.bean.Func;
import com.fongmi.android.tv.databinding.AdapterFuncBinding;
import com.fongmi.android.tv.utils.ResUtil;

public class FuncPresenter extends Presenter {

    private final OnClickListener listener;
    private Context context;

    public FuncPresenter(OnClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickListener {
        void onItemClick(Func item);
    }

    /**
     * 首页功能块随屏幕自适应（同一规则，手机不过大、电视饱满）：
     * 卡片同时受「屏宽 1/6」与「屏高 1/4」约束，取较小者 ——
     *   手机横持（约 2772×1240，density≈2.75）：屏高约束生效 → 约 310px≈110dp，图标 40dp，不再过大；
     *   1080p 电视（density≈1）：约 270px；4K 电视约 540px，十英尺观看依然饱满。
     * 字号按真实 dp 屏宽（系统原始密度换算，不受 AutoSize 改写）：手机约 16sp，电视约 20sp。
     */
    private void applySize(ViewHolder holder) {
        int screenW = ResUtil.getScreenWidth(context);
        int screenH = ResUtil.getScreenHeight(context);
        int card = Math.max(dp2px(96), Math.min(screenW / 6, screenH / 4));
        int icon = Math.round(card * 0.36f);
        float text = Math.min(20f, Math.max(14f, screenW / rawDensity() / 62f));
        // 卡片高度 = 图标区 + 文字区，避免正方形把文字裁掉
        int cardH = Math.round(card * 1.25f);
        ViewGroup.LayoutParams cardParams = holder.binding.getRoot().getLayoutParams();
        if (cardParams != null) {
            cardParams.width = card;
            cardParams.height = cardH;
            holder.binding.getRoot().setLayoutParams(cardParams);
        }
        ViewGroup.LayoutParams iconParams = holder.binding.icon.getLayoutParams();
        if (iconParams != null) {
            iconParams.width = icon;
            iconParams.height = icon;
            holder.binding.icon.setLayoutParams(iconParams);
        }
        holder.binding.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, text);
    }

    /** 系统原始密度：Resources.getSystem() 不经过 AutoSize 改写，是最真实的 density */
    private static float rawDensity() {
        return Resources.getSystem().getDisplayMetrics().density;
    }

    /** 按系统原始密度把 dp 转 px，绕开 AutoSize 改写过的 Resources */
    private static int dp2px(int dp) {
        return Math.round(dp * rawDensity());
    }

    @NonNull
    @Override
    public Presenter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        context = parent.getContext();
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
