package me.kaneki.download.ui.base.recycler.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.ButterKnife;

/**
 * @author cginechen
 * @date 2016-10-19
 */

public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    protected Context context;

    public RecyclerViewHolder(Context ctx, View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        context = ctx;
    }

}
