package me.kaneki.download.http.convert;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import com.alibaba.fastjson.JSONWriter;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;

/**
 * @author jianbo
 * @Desctription
 * @Date 2017/12/01
 * @Email kaneki.cjb@alibaba-inc.com
 */
final class FastJsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
  private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
  private static final Charset UTF_8 = Charset.forName("UTF-8");


  FastJsonRequestBodyConverter() {
  }

  @Override
  public RequestBody convert(T value) {
    Buffer buffer = new Buffer();
    Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
    try {
      JSONWriter jsonWriter = new JSONWriter(writer);
      jsonWriter.writeObject(value);
      jsonWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
  }
}
