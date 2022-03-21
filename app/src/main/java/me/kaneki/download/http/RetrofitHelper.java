package me.kaneki.download.http;

import java.io.File;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import me.kaneki.download.DownloadApplication;
import me.kaneki.download.R;
import me.kaneki.download.http.convert.FastJsonConverterFactory;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * @author jianbo
 * @Desctription
 * @Date 2017/12/12
 * @Email kaneki.cjb@alibaba-inc.com
 */
public class RetrofitHelper {

    private static final String BASE_URL_APPS = "https://api.dev.al-array.com";

    private volatile static Retrofit retrofitInstance = null;

    /**
     * 创建Retrofit请求Api
     * @param clazz   Retrofit Api接口
     * @return api实例
     */
    public static <T> T createApi(Class<T> clazz){
        return getInstance().create(clazz);
    }


    // ===============================================================
    // private methods =================================================
    /**
     * 获取Retrofit实例
     * @return Retrofit
     */
    private static Retrofit getInstance(){
        if(null == retrofitInstance){
            synchronized (Retrofit.class){
                if(null == retrofitInstance){ // 双重检验锁,仅第一次调用时实例化
                    retrofitInstance = new Retrofit.Builder()
                        // baseUrl总是以/结束，@URL不要以/开头
                        .baseUrl(BASE_URL_APPS)
                        // 使用OkHttp Client
                        .client(buildOKHttpClient())
                        // 集成FastJson转换器
                        .addConverterFactory(FastJsonConverterFactory.create())
                        .build();
                }
            }
        }
        return retrofitInstance;
    }

    /**
     * 构建OkHttpClient
     * @return OkHttpClient
     */
    private static OkHttpClient buildOKHttpClient() {
        // Install the all-trusting trust manager
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts,
                new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext
                .getSocketFactory();

            return new OkHttpClient.Builder()
                .cache(getCache())// 设置缓存文件
                .sslSocketFactory(sslSocketFactory)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .retryOnConnectionFailure(true)                    // 自动重连
                .connectTimeout(15, TimeUnit.SECONDS)       // 15秒连接超时
                .readTimeout(20, TimeUnit.SECONDS)          // 20秒读取超时
                .writeTimeout(20, TimeUnit.SECONDS)         // 20秒写入超时
                .build();
        } catch (Exception e) {
            return new OkHttpClient.Builder()
                .cache(getCache())// 设置缓存文件
                .retryOnConnectionFailure(true)                    // 自动重连
                .connectTimeout(15, TimeUnit.SECONDS)       // 15秒连接超时
                .readTimeout(20, TimeUnit.SECONDS)          // 20秒读取超时
                .writeTimeout(20, TimeUnit.SECONDS)         // 20秒写入超时
                .build();
        }
    }
    /**
     * 获取缓存对象
     * @return Cache
     */
    private static Cache getCache(){
        // 获取缓存目标,SD卡
        File cacheFile = new File(DownloadApplication.getInstance().getCacheDir(), DownloadApplication.getInstance().getResources().getString(R.string.app_name));
        // 创建缓存对象,最大缓存50m
        return new Cache(cacheFile, 1024 * 1024 * 20);
    }
}
