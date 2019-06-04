package qunar.tc.qconfig.common.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonV2<T> {

    public final int status;
    public final String message;
    public final T data;

    @JsonCreator
    public JsonV2(@JsonProperty("status") int status,
                  @JsonProperty("message") String message,
                  @JsonProperty("data") T data) {

        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static JsonV2<?> successOf(Object data) {
        return new JsonV2<>(0, "success", data);
    }

    public static JsonV2<?> successOf(String message) {
        return new JsonV2<>(0, message, null);
    }

    public static JsonV2<?> success() {
        return new JsonV2<>(0, "success", null);
    }

    public static JsonV2<?> failOf(String message) {
        return new JsonV2<>(-1, message, null);
    }

    public static JsonV2<?> failOf(String message, Object object) {
        return new JsonV2<>(-1, message, object);
    }

    public static JsonV2<?> fail() {
        return new JsonV2<>(-1, "fail", null);
    }

    public static JsonV2 successOf(String message, Object data) {
        return new JsonV2<>(0, message, data);
    }


    public static JsonV2 successOf(int status, String message, Object data) {
        return new JsonV2<>(status, message, data);
    }
}