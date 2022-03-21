package me.kaneki.download.ui.base.recycler;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import com.bumptech.glide.Glide;
import me.kaneki.download.R;
import me.kaneki.download.ui.base.recycler.holder.RecyclerViewHolder;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_FLING;

public class XRecyclerView extends RecyclerView {
    public static final int STATE_REFRESH = 0;
    public static final int STATE_FAIL = 1;
    public static final int STATE_END = 2;

    private OnBottomRefreshListener mBottomRefreshListener;
    private RecyclerView.OnScrollListener mOnScrollListener;
    private boolean mBottomRefreshing;
    private boolean mBottomRefreshable;
    private boolean mShouldHideAfterScrollIdle;
    private View emptyView;

    private BottomRefreshViewHolder bottomRefreshViewHolder;

    public XRecyclerView(Context context) {
        super(context);
        init();
    }

    public XRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        emptyView = new View(getContext());
        mBottomRefreshing = false;
        mBottomRefreshable = false;
        mShouldHideAfterScrollIdle = false;
        mBottomRefreshListener = null;

        mOnScrollListener = new RecyclerView.OnScrollListener() {

            // 此变量为true表示的意思是此轮滑动过程已经执行过一次bottom refresh了
            // 设想在一次滑动过程中，如果已经执行过一次bottom refresh，这时手指不离开屏幕，
            // 接着收到bottom refresh结果，将bottom refresh状态置为false，这时如果仍然在滑动，即使bottom view又变得可见了，也不应当再次执行bottom refresh
            private boolean mAlreadyRefreshed = false;
            private int mBottomViewVisibleDy = 0;
            private int mBottomViewHeight = 0;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case SCROLL_STATE_IDLE:
                        //滑动停止
                        if (getContext() != null) {
                            Glide.with(getContext()).resumeRequests();
                        }

                        boolean shouldHide = false;             // 是否需要隐藏bottom view
                        // 如果需要隐藏bottom view，则将shouldHide置为true
                        if (mShouldHideAfterScrollIdle) {
                            shouldHide = true;
                            mShouldHideAfterScrollIdle = false;
                        }
                        // 如果之前还没有滑到指定的位置就停止了滑动，则同样将shouldHide置为true
                        if (mBottomViewVisibleDy != 0) {
                            if (mBottomViewVisibleDy > 0) {
                                shouldHide = true;
                            }
                            mBottomViewVisibleDy = 0;
                        }
                        // 隐藏bottom view
                        if (shouldHide) {
                            hideBottomView();
                        }
                        if (mAlreadyRefreshed) {
                            mAlreadyRefreshed = false;
                        }
                        break;
                    case SCROLL_STATE_FLING:
                        //正在滚动
                        if (getContext() != null) {
                            Glide.with(getContext()).pauseRequests();
                        }
                        break;

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                emptyView.scrollBy(0, dy);

                if (mBottomRefreshListener != null) {
                    mBottomRefreshListener.onScroll(recyclerView, emptyView.getScrollY());
                }

                // 如果当前不可刷新，或者正在刷新，则不执行bottom refresh操作
                if (!isBottomRefreshable() || isBottomRefreshing() || mAlreadyRefreshed || dy == 0) {
                    return;
                }
                if (isBottomViewVisible()) {
                    // dy是本次调用onScrolled和上次调用onScrolled在y轴方向的偏移量，这里将bottom view可见之后的偏移量累加起来
                    mBottomViewVisibleDy += dy;
                    if (mBottomViewHeight == 0) {
                        View itemView = getLastVisibleItem();
                        if (itemView != null) {
                            mBottomViewHeight = itemView.getHeight();
                        }
                    }
                    // 如果bottom view可见之后的y轴偏移量大于bottom view高度的一半，则执行bottom refresh
                    if (mBottomViewHeight != 0 && mBottomViewVisibleDy > mBottomViewHeight / 2) {
                        if (mBottomRefreshListener != null) {
                            mBottomRefreshListener.onBottomRefresh();
                            mBottomRefreshing = true;
                            mAlreadyRefreshed = true;
                        }
                        mBottomViewVisibleDy = 0;
                    }
                } else {
                    mBottomViewVisibleDy = 0;
                }
            }
        };
    }

    public View getFirstVisibleItem() {
        return getLayoutManager().getChildAt(0);
    }

    public View getSecondVisibleItem() {
        return getLayoutManager().getChildAt(1);
    }

    private View getLastVisibleItem() {
        int firstItemPosition = getFirstVisibleItemPosition();
        int lastItemPosition = getLastVisibleItemPosition();
        if (firstItemPosition != NO_POSITION && lastItemPosition != NO_POSITION) {
            return getLayoutManager().getChildAt(lastItemPosition - firstItemPosition);
        } else {
            return null;
        }
    }

    private int getFirstVisibleItemPosition() {
        RecyclerView.LayoutManager manager = getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findFirstVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            int positions[] = ((StaggeredGridLayoutManager) manager).findFirstVisibleItemPositions(null);
            int min = Integer.MAX_VALUE;
            for (int pos : positions) {
                if (pos < min) {
                    min = pos;
                }
            }
            return min;
        }
        return NO_POSITION;
    }

    private int getLastVisibleItemPosition() {
        RecyclerView.LayoutManager manager = getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) manager).findLastVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            int positions[] = ((StaggeredGridLayoutManager) manager).findLastVisibleItemPositions(null);
            int max = NO_POSITION;
            for (int pos : positions) {
                if (pos > max) {
                    max = pos;
                }
            }
            return max;
        }
        return NO_POSITION;
    }

    private boolean isBottomViewVisible() {
        int lastVisibleItem = getLastVisibleItemPosition();
        return lastVisibleItem != NO_POSITION && lastVisibleItem == getAdapter().getItemCount() - 1;
    }

    // 滑到顶部
    public void gotoTop() {
        smoothScrollToPosition(0);
    }

    // 设置为没有数据了
    public void setBottomRefreshable(boolean refreshable) {
        mBottomRefreshable = refreshable;
        getAdapter().notifyDataSetChanged();
    }

    public boolean isBottomRefreshable() {
        return mBottomRefreshable;
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter != null) {
            WrapperAdapter wrapperAdapter = new WrapperAdapter(adapter);
            super.setAdapter(wrapperAdapter);
        }
    }

    // 设置底部下拉刷新监听
    public void setOnBottomRefreshListener(OnBottomRefreshListener listener) {
        mBottomRefreshListener = listener;
        if (mBottomRefreshListener != null) {
            addOnScrollListener(mOnScrollListener);
            mBottomRefreshable = true;
        } else {
            removeOnScrollListener(mOnScrollListener);
            mBottomRefreshable = false;
        }
    }

    // 当前是否正在bottom refreshing
    public boolean isBottomRefreshing() {
        return mBottomRefreshing;
    }

    // 下拉刷新完成之后需要隐藏bottom view
    public void onBottomRefreshComplete() {
        mBottomRefreshing = false;
        bottomRefreshViewHolder.setBottomState(STATE_REFRESH);
        // 如果当前没有在滑动状态，则直接隐藏
        // 如果当前在滑动状态，则等待滑动停止后再隐藏
        if (getScrollState() == SCROLL_STATE_IDLE) {
            hideBottomView();
            mShouldHideAfterScrollIdle = false;
        } else {
            mShouldHideAfterScrollIdle = true;
        }
    }

    // 下拉刷新完成之后需要隐藏bottom view
    public void onBottomRefreshFail() {
        mBottomRefreshing = false;
        bottomRefreshViewHolder.setBottomState(STATE_FAIL);
    }

    // 下拉刷新完成之后需要隐藏bottom view
    public void onBottomRefreshEnd() {
        mBottomRefreshing = false;
        bottomRefreshViewHolder.setBottomState(STATE_END);
    }

    // 隐藏bottom view
    // 如果bottom view是可见的，则根据bottom view 当前的位置和RecyclerView当前位置来决定要向上滑动的距离
    private void hideBottomView() {
        if (isBottomViewVisible()) {
            View bottomView = getLastVisibleItem();
            if (bottomView != null) {
                int[] bottomViewLocation = new int[2];
                bottomView.getLocationInWindow(bottomViewLocation);
                int[] recyclerViewLocation = new int[2];
                getLocationInWindow(recyclerViewLocation);
                int recyclerViewHeight = getHeight();
                int offset = recyclerViewLocation[1] + recyclerViewHeight - bottomViewLocation[1];
                if (offset > 0) {
                    scrollBy(0, -offset);
                }
            }
        }
    }

    public interface OnBottomRefreshListener {
        void onBottomRefresh();

        void onScroll(RecyclerView recyclerView, int scrollY);
    }



    /**
     * 自定义包裹的Adapter,主要用来处理加载更多视图
     */
    private class WrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_BOTTOM_REFRESH_ITEM = Integer.MIN_VALUE + 1;

        /**
         * 被包裹的外部Adapter
         */
        private RecyclerView.Adapter mInnerAdapter;

        private RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                notifyItemRangeRemoved(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                notifyItemRangeChanged(fromPosition, toPosition + itemCount);
            }
        };

        private WrapperAdapter(@NonNull RecyclerView.Adapter adapter) {
            if (mInnerAdapter != null) {
                notifyItemRangeRemoved(0, mInnerAdapter.getItemCount());
                mInnerAdapter.unregisterAdapterDataObserver(dataObserver);
            }
            this.mInnerAdapter = adapter;
            mInnerAdapter.registerAdapterDataObserver(dataObserver);
            notifyItemRangeInserted(0, mInnerAdapter.getItemCount());
        }

        public boolean isLoadMoreView(int position) {
            return isBottomRefreshable() && position == getItemCount() - 1;
        }

        @Override
        public int getItemCount() {
            if (mInnerAdapter != null) {
                int itemCount = mInnerAdapter.getItemCount();
                if (isBottomRefreshable()) {
                    return itemCount + 1;
                } else {
                    return itemCount;
                }
            } else {
                return 0;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isBottomRefreshable()) {
                if (mInnerAdapter != null) {
                    int adapterCount = mInnerAdapter.getItemCount();
                    if (position < adapterCount) {
                        return mInnerAdapter.getItemViewType(position);
                    }
                }
                return TYPE_BOTTOM_REFRESH_ITEM;
            } else {
                if (mInnerAdapter != null) {
                    return mInnerAdapter.getItemViewType(position);
                } else {
                    return super.getItemViewType(position);
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_BOTTOM_REFRESH_ITEM) {
                View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_md_list_footer, parent, false);
                bottomRefreshViewHolder = new BottomRefreshViewHolder(getContext(), view);
                return bottomRefreshViewHolder;
            } else {
                return mInnerAdapter.onCreateViewHolder(parent, viewType);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (!(holder instanceof BottomRefreshViewHolder)) {
                if (mInnerAdapter != null) {
                    int adapterCount = mInnerAdapter.getItemCount();
                    if (position < adapterCount) {
                        mInnerAdapter.onBindViewHolder(holder, position);
                    }
                }
            } else {
                BottomRefreshViewHolder bottomRefreshViewHolder = (BottomRefreshViewHolder) holder;
                bottomRefreshViewHolder.checkPageFilled();
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            if (holder instanceof BottomRefreshViewHolder) {
                // 支持瀑布流布局
                ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                    ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(true);
                }
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            // 对Grid布局进行支持
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridLayoutManager = (GridLayoutManager) manager;
                final GridLayoutManager.SpanSizeLookup lookup = gridLayoutManager.getSpanSizeLookup();
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return isLoadMoreView(position) ? gridLayoutManager.getSpanCount()
                            : lookup.getSpanSize(position);
                    }
                });
            }
        }
    }


    /**
     * bottom refresh View对应的ViewHolder
     */
    public class BottomRefreshViewHolder extends RecyclerViewHolder {

        @BindView(R.id.pb_loading)
        public ProgressBar progressBar;
        @BindView(R.id.tv_tip)
        public TextView textView;

        public BottomRefreshViewHolder(Context ctx, View itemView) {
            super(ctx, itemView);

        }

        void checkPageFilled() {
            // 只用当数据填充后再做检查
            if (getAdapter() != null && getAdapter().getItemCount() > 2) {
                // 检查是否不满一页
                int offset = computeVerticalScrollOffset();
                if (offset == 0) {
                    setBottomState(STATE_END);
                } else {
                    setBottomState(STATE_REFRESH);
                }
            } else {
                progressBar.setVisibility(GONE);
                textView.setVisibility(GONE);
            }
        }

        void setBottomState(int state) {
            switch (state) {
                case XRecyclerView.STATE_REFRESH:
                    progressBar.setVisibility(VISIBLE);
                    textView.setVisibility(GONE);
                    break;
                case XRecyclerView.STATE_FAIL:
                    progressBar.setVisibility(GONE);
                    textView.setVisibility(VISIBLE);
                    textView.setText("哎呀，失败了，再试一下？");

                    itemView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressBar.setVisibility(VISIBLE);
                            textView.setVisibility(GONE);
                            if (mBottomRefreshListener != null) {
                                mBottomRefreshListener.onBottomRefresh();
                                mBottomRefreshing = true;
                            }
                        }
                    });
                    break;
                case XRecyclerView.STATE_END:
                    progressBar.setVisibility(GONE);
                    textView.setVisibility(VISIBLE);
                    textView.setText("木得了ಥ_ಥ");
                    break;
            }
        }

        public void setVisibility(int visibility) {
            itemView.setVisibility(visibility);
        }
    }
}