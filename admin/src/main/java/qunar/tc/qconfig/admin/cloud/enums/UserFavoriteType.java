package qunar.tc.qconfig.admin.cloud.enums;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;

public enum UserFavoriteType {

    GROUP(0, "group"),
    FILE(1, "file");

    private int code;
    private String text;

    UserFavoriteType(int code, String text) {
        this.code = code;
        this.text = text;
    }

    private static final Map<Integer, UserFavoriteType> CODE_MAPPING = new HashMap<>();
    private static final Map<String, UserFavoriteType> NAME_MAPPING = new HashMap<>();

    static {
        for (UserFavoriteType type : values()) {
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

    public static UserFavoriteType codeOf(int code) {
        UserFavoriteType type = CODE_MAPPING.get(code);
        Preconditions.checkNotNull(type, "UserFavoriteType非法的code:" + code);
        return type;
    }

    public static UserFavoriteType textOf(String text) {
        UserFavoriteType type = NAME_MAPPING.get(Strings.nullToEmpty(text).toUpperCase());
        Preconditions.checkNotNull(type, "UserFavoriteType非法的text:" + text);
        return type;
    }
}
