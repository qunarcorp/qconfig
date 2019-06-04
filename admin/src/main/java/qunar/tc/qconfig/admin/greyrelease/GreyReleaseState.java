package qunar.tc.qconfig.admin.greyrelease;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;

public enum GreyReleaseState {

    WAIT_PUBLISH(0, "wait_publish"),
    DELAY_PUBLISH(1, "delay_publish"),
    PUBLISHING(2, "publishing"),
    FINISH(3, "finish"),
    CANCEL(4, "cancel");

    private int code;
    private String text;

    GreyReleaseState(int code, String text) {
        this.code = code;
        this.text = text;
    }

    private static final Map<Integer, GreyReleaseState> CODE_MAPPING = new HashMap<>();
    private static final Map<String, GreyReleaseState> NAME_MAPPING = new HashMap<>();

    static {
        for (GreyReleaseState type : values()) {
            CODE_MAPPING.put(type.code, type);
            NAME_MAPPING.put(type.text.toUpperCase(), type);
        }
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static GreyReleaseState codeOf(int code) {
        GreyReleaseState type = CODE_MAPPING.get(code);
        Preconditions.checkNotNull(type, "BatchPushTaskStatus非法的code:" + code);
        return type;
    }

    public static GreyReleaseState textOf(String text) {
        GreyReleaseState type = NAME_MAPPING.get(Strings.nullToEmpty(text).toUpperCase());
        Preconditions.checkNotNull(type, "BatchPushTaskStatus非法的text:" + text);
        return type;
    }
}
