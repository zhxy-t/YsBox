package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.bean.Plugin;
import com.fongmi.android.tv.bean.PluginHealth;
import com.fongmi.android.tv.databinding.ItemPluginBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PluginAdapter extends RecyclerView.Adapter<PluginAdapter.ViewHolder> {

    public interface Listener {

        void onDelete(Plugin plugin);
    }

    private final List<Plugin> items = new ArrayList<>();
    private final Set<String> testing = new HashSet<>();
    private final Listener listener;
    private final Runnable onChanged;

    public PluginAdapter(Listener listener, Runnable onChanged) {
        this.listener = listener;
        this.onChanged = onChanged;
    }

    public void setItems(List<Plugin> values) {
        items.clear();
        items.addAll(values);
        notifyDataSetChanged();
    }

    public void setTesting(Set<String> paths) {
        testing.clear();
        if (paths != null) testing.addAll(paths);
    }

    public void selectAll(boolean checked) {
        for (Plugin item : items) item.setSelected(checked);
        notifyDataSetChanged();
        if (onChanged != null) onChanged.run();
    }

    public List<Plugin> getSelected() {
        List<Plugin> result = new ArrayList<>();
        for (Plugin item : items) if (item.isSelected()) result.add(item);
        return result;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPluginBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plugin item = items.get(position);
        holder.binding.name.setText(item.getName());
        bindHealth(holder, item);
        holder.binding.checkbox.setOnCheckedChangeListener(null);
        holder.binding.checkbox.setChecked(item.isSelected());
        holder.binding.checkbox.setOnCheckedChangeListener((view, checked) -> item.setSelected(checked));
        holder.binding.delete.setOnClickListener(view -> listener.onDelete(item));
    }

    private void bindHealth(ViewHolder holder, Plugin item) {
        int grade;
        long ms = 0;
        if (testing.contains(item.getPath())) {
            grade = PluginHealth.TESTING;
        } else {
            PluginHealth.Result result = PluginHealth.get(item.getPath());
            grade = result.grade;
            ms = result.ms;
        }
        if (grade == PluginHealth.UNKNOWN) {
            holder.binding.health.setVisibility(android.view.View.GONE);
            return;
        }
        holder.binding.health.setVisibility(android.view.View.VISIBLE);
        holder.binding.health.setText(PluginHealth.label(grade, ms));
        holder.binding.health.setTextColor(PluginHealth.color(grade));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemPluginBinding binding;

        ViewHolder(ItemPluginBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
