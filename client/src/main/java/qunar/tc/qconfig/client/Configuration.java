package qunar.tc.qconfig.client;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-5-9.
 */
public interface Configuration<T> {

    ListenableFuture<Boolean> initFuture();

    T emptyData();

    T parse(String data) throws IOException;

    void addListener(ConfigListener<T> listener);

    interface ConfigListener<T> {
        void onLoad(T conf);
    }


}
