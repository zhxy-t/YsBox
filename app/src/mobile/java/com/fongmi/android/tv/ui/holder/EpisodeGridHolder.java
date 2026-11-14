package com.fongmi.android.tv.ui.holder;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.bean.Episode;
import com.fongmi.android.tv.databinding.AdapterEpisodeGridBinding;
import com.fongmi.android.tv.ui.adapter.EpisodeAdapter;
import com.fongmi.android.tv.ui.base.BaseEpisodeHolder;

public class EpisodeGridHolder extends BaseEpisodeHolder {

    private final EpisodeAdapter.OnClickListener listener;
    private final AdapterEpisodeGridBinding binding;
    /** 固定列网格：chip 等宽铺满格子，不做文字自适应宽度 */
    private final boolean fixedWidth;

    public EpisodeGridHolder(@NonNull AdapterEpisodeGridBinding binding, EpisodeAdapter.OnClickListener listener) {
        this(binding, listener, false);
    }

    public EpisodeGridHolder(@NonNull AdapterEpisodeGridBinding binding, EpisodeAdapter.OnClickListener listener, boolean fixedWidth) {
        super(binding.getRoot());
        this.binding = binding;
        this.listener = listener;
        this.fixedWidth = fixedWidth;
    }

    @Override
    public void initView(Episode item) {
        com.fongmi.android.tv.ui.dialog.ChipThemeHelper.select(binding.text, item.isSelected());
        binding.text.setText(item.getDesc().concat(item.getName()));
        // 选集胶囊：横向形状，保证「宽比高长」（最小宽 48dp > 统一高 40dp），长文字自然加宽
        binding.text.setMinimumHeight(com.fongmi.android.tv.utils.ResUtil.dp2px(40));
        if (fixedWidth) {
            // 固定 4 列网格：chip 横向铺满格子，每排右缘严格对齐（grid 已等分列宽）
            binding.text.setMinimumWidth(0);
            binding.text.setMaxWidth(Integer.MAX_VALUE);  // 彻底关掉流式模式留下的宽度上限
            binding.text.setEllipsize(android.text.TextUtils.TruncateAt.END);
            binding.text.setHorizontallyScrolling(false);
        } else {
            binding.text.setMinimumWidth(com.fongmi.android.tv.utils.ResUtil.dp2px(48));
            // Flexbox 布局：胶囊宽度随文字长度变化；设定上限（不超过列表可用宽度），超出跑马灯
            binding.text.post(() -> {
                // 胶囊宽度随文字长度变化；只按屏幕 1/3 绝对封顶，超出走跑马灯。
                // 不能用 itemView 宽度推导——流式布局下那正是文字本身宽度，会把文字挤没。
                int dp12 = com.fongmi.android.tv.utils.ResUtil.dp2px(12);
                int cap = com.fongmi.android.tv.utils.ResUtil.getScreenWidth() / 3 - dp12;
                if (cap < dp12) cap = dp12;
                binding.text.setMaxWidth(cap);
            });
        }
        binding.text.setOnClickListener(v -> listener.onItemClick(item));
    }
}
