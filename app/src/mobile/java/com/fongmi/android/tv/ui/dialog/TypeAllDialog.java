package com.fongmi.android.tv.ui.dialog;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.fongmi.android.tv.utils.ResUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import com.google.android.material.textview.MaterialTextView;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogTypeAllBinding;
import java.util.List;

/**
 * 全部分类：与筛选面板完全同款的 BottomSheet 胶囊流。
 * 复用筛选 chip（adapter_value/shape_accent/14sp），选中态 chip 填充主题色。
 */
public class TypeAllDialog extends BaseBottomSheetDialog {
    private DialogTypeAllBinding binding;
    private List<TypeAllBean> items;
    private int selected = -1;
    private Listener listener;

    public interface Listener {
        void onTypeAllPick(int position);
    }

    public static class TypeAllBean {
        private final String id;
        private final String name;
        public TypeAllBean(String id, String name) {
            this.id = id;
            this.name = name;
        }
        public String getId() {
            return id;
        }
        public String getName() {
            return name;
        }
    }

    public static TypeAllDialog create() {
        return new TypeAllDialog();
    }

    public TypeAllDialog items(List<TypeAllBean> items) {
        this.items = items;
        return this;
    }

    public TypeAllDialog selected(int selected) {
        this.selected = selected;
        return this;
    }

    public TypeAllDialog listener(Listener listener) {
        this.listener = listener;
        return this;
    }

    /** 与 FilterDialog 一致的 Fragment 宿主入口：走 childFragmentManager，防重复弹窗。 */
    public void show(androidx.fragment.app.Fragment fragment) {
        for (androidx.fragment.app.Fragment f : fragment.getChildFragmentManager().getFragments()) if (f instanceof TypeAllDialog) return;
        show(fragment.getChildFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return binding = DialogTypeAllBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        // 与筛选面板同款胶囊流：紧凑排列、自动换行，杜绝纵向长列表
        FlexboxLayoutManager lm = new FlexboxLayoutManager(getContext());
        lm.setFlexWrap(FlexWrap.WRAP);
        binding.recycler.setLayoutManager(lm);
        binding.recycler.setAdapter(new ChipAdapter());
    }

    private class ChipAdapter extends RecyclerView.Adapter<VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MaterialTextView chip = (MaterialTextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_value, parent, false);
            ChipThemeHelper.applyDialog(chip, 8f);
            return new VH(chip);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) holder.chip.getLayoutParams();
            lp.topMargin = lp.bottomMargin = ResUtil.dp2px(4);
            holder.chip.setLayoutParams(lp);
            holder.chip.setText(items.get(position).getName());
            ChipThemeHelper.select(holder.chip, position == selected);
            holder.chip.setOnClickListener(v -> {
                selected = position;
                notifyDataSetChanged();
                if (listener != null) listener.onTypeAllPick(position);
            });
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }
    }

    private static class VH extends RecyclerView.ViewHolder {
        final MaterialTextView chip;
        VH(MaterialTextView chip) {
            super(chip);
            this.chip = chip;
        }
    }
}