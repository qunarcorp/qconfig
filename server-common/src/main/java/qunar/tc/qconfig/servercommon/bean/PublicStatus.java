package qunar.tc.qconfig.servercommon.bean;

/**
 * @author zhenyu.nie created on 2014 2014/6/27 14:39
 */
public enum PublicStatus {

    INUSE(0, "使用中"), PUBLIC(1, "deprecated"), DELETE(2, "删除");

    private int code;

    private String text;

    PublicStatus(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int code() {
        return code;
    }

    public String text() {
        return text;
    }

    public static PublicStatus codeOf(int status) {
        for (PublicStatus statusType : PublicStatus.values()) {
            if (statusType.code == status) {
                return statusType;
            }
        }
        throw new IllegalArgumentException("invalid status code: " + status + " to generate " + PublicStatus.class.getName());
    }

    public static boolean isInUse(int code) {
        return code != DELETE.code;
    }
}
