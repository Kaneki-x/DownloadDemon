package me.kaneki.download.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import me.kaneki.download.R;
import me.kaneki.download.adapter.holder.AppDownloadViewHolder;
import me.kaneki.download.http.entity.AppEntity;
import me.kaneki.download.ui.base.recycler.adapter.BaseRecyclerAdapter;
import me.kaneki.download.ui.base.recycler.holder.RecyclerViewHolder;

public class AppDownloadListAdapter extends BaseRecyclerAdapter<AppEntity> {

    private List<AppEntity> appDownloadList = new ArrayList<>();

    public AppDownloadListAdapter(Context ctx, List<AppEntity> list) {
        super(ctx, list);
    }

    public void addApp(AppEntity appEntity) {
        appDownloadList.add(appEntity);
    }

    public void addAppDownloadList(List<AppEntity> list) {
        appDownloadList.addAll(list);
    }

    public void removeApp(AppEntity appEntity) {
        for (AppEntity object : appDownloadList) {
            if (appEntity.getPackage_name().equals(object.getPackage_name())) {
                appDownloadList.remove(object);
            }
        }
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_download_list;
    }

    @Override
    public RecyclerViewHolder createViewHolder(Context context, View view, int viewType) {
        return new AppDownloadViewHolder(context, view);
    }

    @Override
    public void bindData(RecyclerViewHolder holder, int position, AppEntity item) {
        if (holder instanceof AppDownloadViewHolder) {
            AppDownloadViewHolder appDownloadViewHolder = (AppDownloadViewHolder) holder;
            appDownloadViewHolder.bindView(item);
        }
    }
}
