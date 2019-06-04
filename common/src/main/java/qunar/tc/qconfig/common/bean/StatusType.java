package qunar.tc.qconfig.common.bean;

/**
 * @author zhenyu.nie created on 2014 2014/5/16 18:22
 */
public enum StatusType {

    PENDING(1, "待审核"),
    PASSED(2, "审核通过"),
    PUBLISH(3, "已发布"),
    REJECT(4, "审核未通过"),
    CANCEL(5, "回退审核"),
    DELETE(6, "已删除");

    private int code;
    private String text;

    StatusType(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static StatusType codeOf(int code) {
        for (StatusType statusType : StatusType.values()) {
            if (statusType.code == code) {
                return statusType;
            }
        }
        throw new IllegalArgumentException("invalid status code [" + code + "] to generate " + StatusType.class.getName());
    }

    public static StatusType fromText(String text) {
        for (StatusType statusType : StatusType.values()) {
            if (statusType.text().equals(text)) {
                return statusType;
            }
        }
        throw new IllegalArgumentException("invalid status text [" + text + "] to generate " + StatusType.class.getName());
    }

    public boolean isPublish() {
        return this == PUBLISH;
    }

    public boolean isPassed() {
        return this == PASSED;
    }

    public int code() {
        return code;
    }

    public String text() {
        return text;
    }
}
