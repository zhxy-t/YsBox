package com.fongmi.android.tv.ui.fragment;


import com.fongmi.android.tv.ui.dialog.DialogThemeHelper;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;
import androidx.viewpager.widget.ViewPager;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Class;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Value;
import com.fongmi.android.tv.databinding.FragmentVodBinding;
import com.fongmi.android.tv.event.CastEvent;
import com.fongmi.android.tv.event.ConfigEvent;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.event.StateEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.impl.ConfigListener;
import com.fongmi.android.tv.impl.FilterListener;
import com.fongmi.android.tv.impl.SiteListener;
import com.fongmi.android.tv.model.SiteViewModel;
import com.fongmi.android.tv.ui.activity.HistoryActivity;
import com.fongmi.android.tv.ui.activity.KeepActivity;
import com.fongmi.android.tv.ui.activity.SearchActivity;
import com.fongmi.android.tv.ui.adapter.TypeAdapter;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.dialog.FilterDialog;
import com.fongmi.android.tv.ui.dialog.LinkDialog;
import com.fongmi.android.tv.ui.dialog.ReceiveDialog;
import com.fongmi.android.tv.ui.dialog.SiteDialog;
import com.fongmi.android.tv.ui.dialog.SourceScriptsDialog;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class VodFragment extends BaseFragment implements ConfigListener, SiteListener, FilterListener, TypeAdapter.OnClickListener {

    private FragmentVodBinding mBinding;
    private SiteViewModel mViewModel;
    private TypeAdapter mAdapter;
    private Result mResult;

    public static VodFragment newInstance() {
        return new VodFragment();
    }

    private FolderFragment getFragment() {
        return (FolderFragment) mBinding.pager.getAdapter().instantiateItem(mBinding.pager, mBinding.pager.getCurrentItem());
    }

    private Site getHome() {
        return VodConfig.get().getHome();
    }

    private Config getConfig() {
        return VodConfig.get().getConfig();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentVodBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        mBinding.title.setSelected(true);
        setRecyclerView();
        setViewModel();
        showProgress();
        setTitle();
        setLogo();
        tintOverflow();
    }

    @Override
    protected void initEvent() {
        mBinding.top.setOnClickListener(this::onTop);
        mBinding.logo.setOnClickListener(this::onLogo);
        mBinding.logo.setOnLongClickListener(this::onLogoRefresh);
        mBinding.link.setOnClickListener(this::onLink);
        mBinding.title.setOnClickListener(this::onSite);
        mBinding.typeMore.setOnClickListener(this::onTypeAll);
        mBinding.filter.setOnClickListener(this::onFilter);
        mBinding.filter.setOnLongClickListener(this::onLink);
        mBinding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
        mBinding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            float factor = Math.abs(verticalOffset * 1f / appBarLayout.getTotalScrollRange());
            int padding = (int) (ResUtil.dp2px(12) * factor);
            if (mBinding.type.getPaddingTop() == padding) return;
            mBinding.type.setPadding(mBinding.type.getPaddingStart(), padding, mBinding.type.getPaddingEnd(), mBinding.type.getPaddingBottom());
        });
        mBinding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mBinding.type.smoothScrollToPosition(position);
                mAdapter.setSelected(position);
                setFabVisible(position);
            }
        });
    }

    private void setRecyclerView() {
        mBinding.type.setHasFixedSize(true);
        mBinding.type.setItemAnimator(null);
        mBinding.type.setAdapter(mAdapter = new TypeAdapter(this));
        mBinding.type.post(this::updateTypeMoreVisible);
        mBinding.pager.setAdapter(new PageAdapter(getChildFragmentManager()));
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(SiteViewModel.class);
        mViewModel.getResult().observe(getViewLifecycleOwner(), this::setAdapter);
    }

    private void setAdapter(Result result) {
        mAdapter.addAll(mResult = result);
        mBinding.pager.getAdapter().notifyDataSetChanged();
        updateTypeMoreVisible();
        setFabVisible(0);
        hideProgress();
        showContent();
    }

    private void setFabVisible(int position) {
        if (mAdapter.getItemCount() == 0) {
            mBinding.top.setVisibility(View.INVISIBLE);
            mBinding.link.setVisibility(View.VISIBLE);
            mBinding.filter.setVisibility(View.GONE);
        } else if (!mAdapter.get(position).getFilters().isEmpty()) {
            mBinding.top.setVisibility(View.INVISIBLE);
            mBinding.link.setVisibility(View.GONE);
            mBinding.filter.show();
        } else if (position == 0 || mAdapter.get(position).getFilters().isEmpty()) {
            mBinding.top.setVisibility(View.INVISIBLE);
            mBinding.filter.setVisibility(View.GONE);
            mBinding.link.show();
        }
    }

    private void setTitle() {
        List<String> items = Arrays.asList(getHome().getName(), getConfig().getName(), getString(R.string.app_name));
        Optional<String> optional = items.stream().filter(s -> !TextUtils.isEmpty(s)).findFirst();
        optional.ifPresent(s -> mBinding.title.setText(s));
    }

    private void onTop(View view) {
        getFragment().scrollToTop();
        mBinding.top.setVisibility(View.INVISIBLE);
        if (mBinding.filter.getVisibility() == View.INVISIBLE) mBinding.filter.show();
        else if (mBinding.link.getVisibility() == View.INVISIBLE) mBinding.link.show();
    }

    private boolean onLink(View view) {
        LinkDialog.show(this);
        return true;
    }

    private void onLogo(View view) {
        // 短按：切换线路（首页配置线路列表）
        List<Config> configs = Config.getAll(0);
        if (configs.isEmpty()) return;
        String[] names = new String[configs.size()];
        final int current = indexOfCurrent(configs);
        for (int i = 0; i < configs.size(); i++) {
            Config config = configs.get(i);
            names[i] = TextUtils.isEmpty(config.getName()) ? config.getUrl() : config.getName();
        }
        DialogThemeHelper.builder(requireActivity()).setTitle(R.string.switch_line).setSingleChoiceItems(names, current, (dialog, which) -> {
            dialog.dismiss();
            if (which == current) return;
            setConfig(configs.get(which));
        }).show();
    }

    private int indexOfCurrent(List<Config> configs) {
        int cid = VodConfig.getCid();
        for (int i = 0; i < configs.size(); i++) if (configs.get(i).getId() == cid) return i;
        return 0;
    }

    private boolean onLogoRefresh(View view) {
        // 长按：刷新首页内容
        RefreshEvent.home();
        return true;
    }

    private void onSite(View view) {
        // 标题文字：当前 Config 里面的真实 Site / 直线路列表，可直接选择。
        SiteDialog.create().change().show(this);
    }

    private void onTypeAll(View view) {
        int count = mAdapter.getItemCount();
        if (count == 0) return;
        String[] names = new String[count];
        for (int i = 0; i < count; i++) names[i] = mAdapter.get(i).getTypeName();
        final int[] selected = {mBinding.pager.getCurrentItem()};
        android.widget.GridView grid = new android.widget.GridView(DialogThemeHelper.wrap(requireActivity()));
        grid.setNumColumns(2);
        grid.setVerticalSpacing(0);
        grid.setPadding(0, ResUtil.dp2px(8), 0, 0);
        grid.setClipToPadding(false);
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(grid.getContext(), R.layout.item_type_all, R.id.text, names) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View item = super.getView(position, convertView, parent);
                ((android.widget.RadioButton) item.findViewById(R.id.radio)).setChecked(position == selected[0]);
                return item;
            }
        };
        grid.setAdapter(adapter);
        androidx.appcompat.app.AlertDialog dialog = DialogThemeHelper.builder(requireActivity()).setTitle(R.string.type_all).setView(grid).create();
        grid.setOnItemClickListener((parent, item, position, id) -> {
            selected[0] = position;
            adapter.notifyDataSetChanged();
            dialog.dismiss();
            onItemClick(position, mAdapter.get(position));
        });
        dialog.show();
    }

    private void updateTypeMoreVisible() {
        mBinding.type.post(() -> {
            int width = mBinding.type.getWidth();
            if (width == 0) return;
            boolean visible = mAdapter.getItemCount() > 0 && mBinding.type.computeHorizontalScrollRange() > width;
            mBinding.typeMore.setVisibility(visible ? View.VISIBLE : View.GONE);
        });
    }

    private void onFilter(View view) {
        if (mAdapter.getItemCount() > 0) FilterDialog.create().filter(mAdapter.get(mBinding.pager.getCurrentItem()).getFilters()).show(this);
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.plugin) onSourceScripts();
        else if (item.getItemId() == R.id.keep) KeepActivity.start(requireActivity());
        else if (item.getItemId() == R.id.search) SearchActivity.start(requireActivity());
        else if (item.getItemId() == R.id.history) HistoryActivity.start(requireActivity());
        return true;
    }

    private void onSourceScripts() {
        try {
            SourceScriptsDialog dialog = new SourceScriptsDialog(requireActivity(), (file, isPy) -> onScriptSelected(file, isPy));
            dialog.setCurrentFile(getCurrentScriptFile());
            dialog.show();
        } catch (Exception e) {
            Notify.show(e.getMessage());
        }
    }

    private java.io.File getCurrentScriptFile() {
        try {
            Site home = VodConfig.get().getHome();
            if (home != null && home.getApi() != null && !home.getApi().startsWith("http")) {
                java.io.File file = new java.io.File(home.getApi());
                if (file.exists()) return file;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void onScriptSelected(java.io.File file, boolean isPy) {
        try {
            String name = file.getName();
            String base = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
            Site site = new Site();
            site.setKey((isPy ? "py_" : "js_") + base);
            site.setName(base);
            site.setApi(file.getAbsolutePath());
            site.setType(3);
            String extKey = (isPy ? "py_" : "js_") + base;
            String savedExt = com.fongmi.android.tv.utils.PyExtConfig.load(requireActivity(), extKey);
            site.setExt(savedExt == null || savedExt.isEmpty() ? "{}" : savedExt);
            site.setJar("");
            // 只清这个脚本对应的旧蜘蛛（文件被替换后要重新加载），不能异步全清：
            // 全清会和下面这次首页加载并发，把刚建好的蜘蛛销毁掉。
            com.fongmi.android.tv.api.loader.BaseLoader.get().remove(site.getKey());
            // setHome 内部会发 RefreshEvent.home() 触发一次首页加载；
            // 这里再手动调一次会让 ViewModelTaskRunner 取消掉前一次，
            // 被中断的那次会把 SpiderNull 缓存住，导致首页永远空白。
            VodConfig.get().setHome(site);
            Notify.show(getString(R.string.script_picker_selected, name));
        } catch (Exception e) {
            Notify.show(e.getMessage());
        }
    }

    private void showProgress() {
        mBinding.progress.getRoot().setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mBinding.progress.getRoot().setVisibility(View.GONE);
    }

    private void hideContent() {
        mBinding.type.setVisibility(View.INVISIBLE);
        mBinding.pager.setVisibility(View.INVISIBLE);
    }

    private void showContent() {
        mBinding.type.setVisibility(View.VISIBLE);
        mBinding.pager.setVisibility(View.VISIBLE);
    }

    private void homeContent() {
        showProgress();
        setFabVisible(0);
        mAdapter.clear();
        mViewModel.homeContent();
        mBinding.pager.setAdapter(new PageAdapter(getChildFragmentManager()));
    }

    public Result getResult() {
        return mResult == null ? new Result() : mResult;
    }

    private void setLogo() {
        ImgUtil.logo(mBinding.logo);
    }

    private void tintOverflow() {
        Drawable icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_action_overflow);
        if (icon == null) return;
        Drawable tinted = icon.mutate();
        tinted.setTint(Color.WHITE);
        mBinding.toolbar.setOverflowIcon(tinted);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigEvent(ConfigEvent event) {
        if (event.type() == ConfigEvent.Type.VOD) setLogo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        switch (event.getType()) {
            case HOME:
                setTitle();
            case SIZE:
                homeContent();
                break;
            case CATEGORY:
                getFragment().onRefresh();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStateEvent(StateEvent event) {
        switch (event.type()) {
            case EMPTY:
                hideProgress();
                break;
            case PROGRESS:
                showProgress();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCastEvent(CastEvent event) {
        ReceiveDialog.create().event(event).show(this);
    }

    @Override
    public void setConfig(Config config) {
        VodConfig.load(config, new Callback() {
            @Override
            public void start() {
                showProgress();
                hideContent();
                setTitle();
                setLogo();
            }

            @Override
            public void error(String msg) {
                Notify.dismiss();
                Notify.show(msg);
                showContent();
            }
        });
    }

    @Override
    public void setSite(Site item) {
        VodConfig.get().setHome(item);
    }

    @Override
    public void onItemClick(int position, Class item) {
        mBinding.pager.setCurrentItem(position);
        mAdapter.setSelected(position);
    }

    @Override
    public void setFilter(String key, Value value) {
        getFragment().setFilter(key, value);
    }

    @Override
    public boolean canBack() {
        if (mBinding.pager.getAdapter() == null || mBinding.pager.getAdapter().getCount() == 0) return true;
        if (!getFragment().canBack()) return true;
        getFragment().goBack();
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    class PageAdapter extends FragmentStatePagerAdapter {

        public PageAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Class type = mAdapter.get(position);
            return FolderFragment.newInstance(getHome().getKey(), type, 4);
        }

        @Override
        public int getCount() {
            return mAdapter.getItemCount();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        }
    }
}
