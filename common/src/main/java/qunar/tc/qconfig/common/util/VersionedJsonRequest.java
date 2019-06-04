package qunar.tc.qconfig.common.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author zhenyu.nie created on 2017 2017/2/20 20:03
 */
public class VersionedJsonRequest<T> {

    private final T data;

    private final int version;

    @JsonCreator
    public VersionedJsonRequest(@JsonProperty("data") T data,
                                @JsonProperty("version") int version) {
        this.data = data;
        this.version = version;
    }

    public T getData() {
        return data;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "VersionedJsonRequest{" +
                "data=" + data +
                ", version=" + version +
                '}';
    }
}
