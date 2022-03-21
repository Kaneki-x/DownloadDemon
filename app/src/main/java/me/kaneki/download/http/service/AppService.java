package me.kaneki.download.http.service;

import java.util.List;

import me.kaneki.download.http.entity.AppEntity;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @author jianbo
 * @Desctription
 * @Date 2017/12/12
 * @Email kaneki.cjb@alibaba-inc.com
 */
public interface AppService {

    @GET("/demo/1.0/apps")
    Call<List<AppEntity>> getDownloadApps();

}
