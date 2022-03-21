package me.kaneki.download.adapter.holder;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.bumptech.glide.Glide;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.StatusUtil.Status;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend.Listener4SpeedModel;
import me.kaneki.download.R;
import me.kaneki.download.http.entity.AppEntity;
import me.kaneki.download.ui.base.recycler.holder.RecyclerViewHolder;
import me.kaneki.download.utils.DemoUtil;
import me.kaneki.download.utils.StringUtil;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AppDownloadViewHolder extends RecyclerViewHolder {

    @BindView(R.id.iv_download_cover)
    ImageView coverImage;
    @BindView(R.id.tv_download_title)
    TextView titleTextView;
    @BindView(R.id.tv_download_status)
    TextView statusTextView;
    @BindView(R.id.tv_download_size)
    TextView sizeTextView;
    @BindView(R.id.number_progress_bar)
    NumberProgressBar numberProgressBar;
    @BindView(R.id.btn_control)
    Button controlButton;

    DownloadTask task;
    AppEntity appEntity;

    public AppDownloadViewHolder(Context context, View itemView) {
        super(context, itemView);
    }

    public void bindView(final AppEntity appEntity) {
        this.appEntity = appEntity;

        initDefaultView();
        initTask();
        initStatus(StatusUtil.getStatus(task));
        setListener();
    }

    private void initDefaultView() {
        Glide.with(context)
            .load(appEntity.getIcon())
            .transition(withCrossFade())
            .thumbnail(0.25f)
            .into(coverImage);
        titleTextView.setText(appEntity.getTitle());
        titleTextView.setTag(appEntity.getPackage_name());

        numberProgressBar.setVisibility(View.INVISIBLE);
        numberProgressBar.setMax(100);
        sizeTextView.setText(StringUtil.formatFileSize(appEntity.getFile_size()));
    }

    private void initTask() {
        task = new DownloadTask.Builder(appEntity.getDownload_link(), DemoUtil.getParentFile(context))
            .setFilename(appEntity.getTitle())
            .setMinIntervalMillisCallbackProcess(50)
            .setPassIfAlreadyCompleted(false)
            .build();
        task.setTag(appEntity.getPackage_name());
    }

    private void initStatus(Status status) {
        if (status == Status.COMPLETED) {
            sizeTextView.setText(StringUtil.formatFileSize(appEntity.getFile_size()));
            numberProgressBar.setVisibility(View.INVISIBLE);
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.setText("已下载");
            controlButton.setText("重新下载");
        } else if (status == Status.UNKNOWN) {
            sizeTextView.setText(StringUtil.formatFileSize(appEntity.getFile_size()));
            statusTextView.setVisibility(View.GONE);
            numberProgressBar.setVisibility(View.INVISIBLE);
            controlButton.setText("下载");
        } else if (status == Status.IDLE) {
            statusTextView.setVisibility(View.VISIBLE);
            numberProgressBar.setVisibility(View.VISIBLE);
            statusTextView.setText("下载暂停");
            controlButton.setText("继续");
        }  else if (status == Status.PENDING) {
            statusTextView.setVisibility(View.VISIBLE);
            numberProgressBar.setVisibility(View.VISIBLE);
            statusTextView.setText("等待下载");
            controlButton.setText("取消");
        }
    }

    private void initStatus(Exception exception, EndCause endCause) {
        if (exception != null ||
            endCause == EndCause.ERROR ||
            endCause == EndCause.FILE_BUSY ||
            endCause == EndCause.PRE_ALLOCATE_FAILED ||
            endCause == EndCause.SAME_TASK_BUSY) {
            statusTextView.setVisibility(View.VISIBLE);
            numberProgressBar.setVisibility(View.INVISIBLE);
            statusTextView.setText("下载出错");
            controlButton.setText("重试");
        } else if (endCause == EndCause.CANCELED) {
            statusTextView.setVisibility(View.VISIBLE);
            numberProgressBar.setVisibility(View.VISIBLE);
            statusTextView.setText("下载暂停");
            controlButton.setText("继续");
        } else if (endCause == EndCause.COMPLETED) {
            sizeTextView.setText(StringUtil.formatFileSize(appEntity.getFile_size()));
            numberProgressBar.setVisibility(View.INVISIBLE);
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.setText("已下载");
            controlButton.setText("重新下载");
        }
    }

    private void setListener() {
        controlButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Status status = StatusUtil.getStatus(task);
                if (status == Status.UNKNOWN || status == Status.COMPLETED || status == Status.IDLE) {
                    task.enqueue(new DownloadListener4WithSpeed() {
                        @Override
                        public void taskStart(@NonNull DownloadTask task) {
                            System.out.println("taskStart");
                            if (titleTextView.getTag().equals(task.getTag())) {
                                controlButton.setText("暂停");
                                numberProgressBar.setVisibility(View.VISIBLE);
                                statusTextView.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void connectStart(@NonNull DownloadTask task, int blockIndex,
                            @NonNull Map<String, List<String>> requestHeaderFields) {

                        }

                        @Override
                        public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode,
                            @NonNull Map<String, List<String>> responseHeaderFields) {
                            System.out.println("response code:" + responseCode);
                        }

                        @Override
                        public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info,
                            boolean fromBreakpoint, @NonNull Listener4SpeedModel model) {

                        }

                        @Override
                        public void progressBlock(@NonNull DownloadTask task, int blockIndex, long currentBlockOffset,
                            @NonNull SpeedCalculator blockSpeed) {

                        }

                        @Override
                        public void progress(@NonNull DownloadTask task, long currentOffset,
                            @NonNull SpeedCalculator taskSpeed) {
                            if (titleTextView.getTag().equals(task.getTag())) {
                                statusTextView.setVisibility(View.VISIBLE);
                                numberProgressBar.setVisibility(View.VISIBLE);
                                DemoUtil.calcProgressToView(numberProgressBar, currentOffset, appEntity.getFile_size());
                                sizeTextView.setText(StringUtil.formatFileSize(currentOffset) + "/" + StringUtil
                                    .formatFileSize(appEntity.getFile_size()));
                                statusTextView.setText(taskSpeed.instantSpeed());
                            }
                        }

                        @Override
                        public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info,
                            @NonNull SpeedCalculator blockSpeed) {

                        }

                        @Override
                        public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause,
                            @Nullable Exception realCause, @NonNull SpeedCalculator taskSpeed) {
                            System.out.println(realCause);
                            if (titleTextView.getTag().equals(task.getTag())) {
                                System.out.println("task end");
                                initStatus(realCause, cause);
                            }
                        }
                    });
                } else if (status == Status.PENDING || status == Status.RUNNING) {
                    task.cancel();
                    initStatus(Status.IDLE);
                }
            }
        });
    }
}
