package me.kaneki.download.ui.base.recycler.adapter;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.kaneki.download.ui.base.recycler.holder.RecyclerViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cginechen
 * @date 2016-10-19
 */

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerViewHolder> {

    private final List<T> mData;
    private final Context mContext;
    private LayoutInflater mInflater;
    private OnItemClickListener mClickListener;
    private OnItemLongClickListener mLongClickListener;

    public BaseRecyclerAdapter(Context ctx, List<T> list) {
        mData = (list != null) ? list : new ArrayList<T>();
        mContext = ctx;
        mInflater = LayoutInflater.from(ctx);
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerViewHolder holder =
            createViewHolder(mContext, mInflater.inflate(getItemLayoutId(viewType), parent, false), viewType);
        if (mClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(holder.itemView, holder.getLayoutPosition());
                }
            });
        }
        if (mLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mLongClickListener.onItemLongClick(holder.itemView, holder.getLayoutPosition());
                    return true;
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        bindData(holder, position, mData.get(position));
    }

    public T getItem(int pos) {
        return mData.get(pos);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void add(int pos, T item) {
        mData.add(pos, item);
        notifyItemInserted(pos);
    }

    public void add(T item) {
        int pos = mData.size();
        mData.add(item);
        notifyItemInserted(pos);
    }

    public void remove(int pos) {
        mData.remove(pos);
        notifyItemRemoved(pos);
    }

    public void remove(T item) {
        int pos = mData.indexOf(item);
        mData.remove(item);
        notifyItemRemoved(pos);
    }

    public void addList(List<T> datas) {
        int pos = mData.size();
        mData.addAll(datas);
        notifyItemInserted(pos);
    }

    public void refresh(List<T> datas) {
        mData.clear();
        mData.addAll(datas);
        notifyDataSetChanged();
    }

    public void merge(final List<T> datas) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mData.size();
            }

            @Override
            public int getNewListSize() {
                return datas.size();
            }

            @Override
            public boolean areItemsTheSame(int i, int i1) {
                return mData.get(i).equals(datas.get(i1));
            }

            @Override
            public boolean areContentsTheSame(int i, int i1) {
                return false;
            }
        }, true);
        diffResult.dispatchUpdatesTo(this);
    }

    public List<T> getDatas() {
        return mData;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    @SuppressWarnings("SameReturnValue")
    abstract public int getItemLayoutId(int viewType);

    abstract public RecyclerViewHolder createViewHolder(Context context, View view, int viewType);

    abstract public void bindData(RecyclerViewHolder holder, int position, T item);

    public interface OnItemClickListener {
        void onItemClick(View itemView, int pos);
    }


    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int pos);
    }
}
