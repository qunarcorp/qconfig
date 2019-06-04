package qunar.tc.qconfig.common.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author zhenyu.nie created on 2017 2017/2/20 12:48
 */
public class VersionedJsonResponse<T> {

    private final int status;
    private final String message;
    private final T data;
    private final int version;

    @JsonCreator
    public VersionedJsonResponse(@JsonProperty("status") int status,
                                 @JsonProperty("message") String message,
                                 @JsonProperty("data") T data,
                                 @JsonProperty("version") int version) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.version = version;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "VersionedJsonResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", version=" + version +
                '}';
    }
}
