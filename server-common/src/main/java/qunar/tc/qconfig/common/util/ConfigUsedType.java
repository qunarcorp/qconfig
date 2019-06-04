package qunar.tc.qconfig.common.util;

/**
 * @author zhenyu.nie created on 2014 2014/6/13 11:33
 */
public enum ConfigUsedType {

    NO_USE(0, "没有使用"), USE_REMOTE(1, "使用远程文件"), USE_OVERRIDE(2, "使用本地覆盖文件");

    private int code;

    private String text;

    ConfigUsedType(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static ConfigUsedType codeOf(int code) {
        for (ConfigUsedType type : ConfigUsedType.values()) {
            if (type.code == code) {
                return type;
            }
        }

        throw new IllegalArgumentException("illegal code [" + code + "] to generate " + ConfigUsedType.class.getName());
    }
}
