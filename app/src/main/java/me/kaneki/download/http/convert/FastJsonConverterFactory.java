package me.kaneki.download.http.convert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * @author jianbo
 * @Desctription
 * @Date 2017/12/01
 * @Email kaneki.cjb@alibaba-inc.com
 */
public final class FastJsonConverterFactory extends Converter.Factory {

  public static FastJsonConverterFactory create() {
    return new FastJsonConverterFactory();
  }

  private FastJsonConverterFactory() {
  }

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                          Retrofit retrofit) {
    return new me.kaneki.download.http.convert.FastJsonResponseBodyConverter<>(type);
  }

  @Override
  public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                        Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {

    return new me.kaneki.download.http.convert.FastJsonRequestBodyConverter<>();
  }
}
