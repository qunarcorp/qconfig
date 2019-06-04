package qunar.tc.qconfig.admin.model;

/**
 * Date: 14-7-9
 * Time: 上午11:57
 *
 * @author: xiao.liang
 * @description:
 */
public enum ConfigUsedLogStatus {

    NORMAL(0, "正常"), DELETE(1, "删除");

    private int code;

    private String text;

    ConfigUsedLogStatus(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int code() {
        return code;
    }

    public String text() {
        return text;
    }

    public static ConfigUsedLogStatus codeOf(int status) {
        for (ConfigUsedLogStatus configUsedLogStatus : ConfigUsedLogStatus.values()) {
            if (configUsedLogStatus.code == status) {
                return configUsedLogStatus;
            }
        }
        throw new IllegalArgumentException("invalid status code: " + status + " to generate " + ConfigUsedLogStatus.class.getName());
    }
}
