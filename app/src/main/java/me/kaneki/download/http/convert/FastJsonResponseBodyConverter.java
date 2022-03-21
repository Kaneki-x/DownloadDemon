package me.kaneki.download.http.convert;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;

import com.alibaba.fastjson.JSONReader;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * @author jianbo
 * @Desctription
 * @Date 2017/12/01
 * @Email kaneki.cjb@alibaba-inc.com
 */
final class FastJsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

  private final Type type;

  FastJsonResponseBodyConverter(Type type) {
    this.type = type;
  }

  @Override
  public T convert(ResponseBody value) {
    Reader reader = new InputStreamReader(value.byteStream());
    JSONReader jsonReader = new JSONReader(reader);
    return jsonReader.readObject(type);
  }
}
