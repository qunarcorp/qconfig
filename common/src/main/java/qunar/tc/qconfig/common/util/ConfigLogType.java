package qunar.tc.qconfig.common.util;

/**
 * @author zhenyu.nie created on 2014 2014/6/10 16:11
 */
public enum ConfigLogType {

    // 定义的顺序表明了在显示时时间相同情况下排序的顺序
    PULL_SUCCESS(1, "拉取成功"),
    PULL_ERROR(2, "拉取失败"),
    PARSE_REMOTE_ERROR(3, "解析远程文件失败"),
    USE_OVERRIDE(4, "使用本地文件覆盖"),
    USE_REMOTE_FILE(5, "使用远程文件");
    private int code;

    private String text;

    private ConfigLogType(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public static ConfigLogType codeOf(int code) {
        for (ConfigLogType type : ConfigLogType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("illegal code: " + code);
    }
}
