package me.kaneki.download.ui.activity;

import java.util.List;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.kaneki.download.R;
import me.kaneki.download.adapter.AppDownloadListAdapter;
import me.kaneki.download.http.RetrofitHelper;
import me.kaneki.download.http.entity.AppEntity;
import me.kaneki.download.http.service.AppService;
import me.kaneki.download.ui.base.recycler.XRecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.rlv_md_list)
    XRecyclerView xRecyclerView;

    @BindView(R.id.pb_md_loading)
    ProgressBar loadingProgress;

    @BindView(R.id.tv_md_error)
    TextView errorTextView;

    AppDownloadListAdapter appDownloadListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        loadData();
    }

    private void initView() {
        xRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        errorTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }

    private void loadData() {
        AppService appService = RetrofitHelper.createApi(AppService.class);
        Call<List<AppEntity>> getDownloadAppsCall = appService.getDownloadApps();
        getDownloadAppsCall.enqueue(new Callback<List<AppEntity>>() {
            @Override
            public void onResponse(Call<List<AppEntity>> call, Response<List<AppEntity>> response) {
                loadingProgress.setVisibility(View.GONE);
                List<AppEntity> appEntityList = response.body();
                appDownloadListAdapter = new AppDownloadListAdapter(MainActivity.this, appEntityList);
                xRecyclerView.setAdapter(appDownloadListAdapter);
            }

            @Override
            public void onFailure(Call<List<AppEntity>> call, Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                errorTextView.setVisibility(View.VISIBLE);
            }
        });
    }
}