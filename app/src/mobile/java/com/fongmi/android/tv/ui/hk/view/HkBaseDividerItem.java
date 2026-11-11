package com.fongmi.android.tv.ui.hk.view;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 照抄 8.83 BaseDividerItem（ui/view）：GridLayoutManager 下按 viewType 的 leftRight 做等分间距。
 * noBorder 分支按 poly 位置分摊；竖向整行 span 时左右各给满 leftRight、否则按 spanIndex 比例分；
 * 横向时只有第一组给 left，right 恒给满。
 */
public abstract class HkBaseDividerItem extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (!(parent.getLayoutManager() instanceof GridLayoutManager)) return;
        if (!(view.getLayoutParams() instanceof GridLayoutManager.LayoutParams)) return;
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        int position = parent.getChildAdapterPosition(view);
        int spanCount = layoutManager.getSpanCount();
        int viewType = layoutManager.getItemViewType(view);
        boolean noBorder = noBorder(viewType);
        int leftRight = getLeftRight(viewType);
        if (noBorder) {
            // 跨度可能因 SpanSizeLookup 返回异常值而为 0，先兜住再取模，避免除零
            int spanSize = lp.getSpanSize();
            if (spanSize <= 0) spanSize = spanCount;
            int polyPos = getPolyPos(viewType, position) % spanSize;
            outRect.left = polyPos * leftRight / spanSize;
            outRect.right = leftRight - (polyPos + 1) * leftRight / spanSize;
            return;
        }
        if (layoutManager.getOrientation() == RecyclerView.VERTICAL) {
            if (lp.getSpanSize() == spanCount) {
                outRect.left = leftRight;
                outRect.right = leftRight;
            } else {
                outRect.left = (int) ((float) (spanCount - lp.getSpanIndex()) / (float) spanCount
                        * (float) leftRight);
                outRect.right = (int) ((float) (spanCount + 1) * (float) leftRight / (float) spanCount
                        - (float) outRect.left);
            }
        } else {
            // 横向取组序号依赖 SpanSizeLookup 内部跨度记账，异常时按 0 组兜底，不让装饰器打断布局
            int groupIndex = 0;
            try {
                groupIndex = layoutManager.getSpanSizeLookup().getSpanGroupIndex(position, spanCount);
            } catch (Throwable ignored) {
            }
            if (groupIndex == 0) outRect.left = leftRight;
            outRect.right = leftRight;
        }
    }

    public abstract int getLeftRight(int viewType);

    protected int getPolyPos(int viewType, int position) {
        return position;
    }

    protected boolean noBorder(int viewType) {
        return false;
    }
}
