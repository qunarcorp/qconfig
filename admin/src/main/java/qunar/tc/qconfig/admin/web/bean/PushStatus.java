package qunar.tc.qconfig.admin.web.bean;

/**
 * Created by pingyang.yang on 2018/10/23
 */
public enum PushStatus {
    PUSHING(1, "推送中"),
    GREY_RELEASING(2, "灰度发布中"),
    SUCCESS(3, "成功"),
    FAILED(4, "失败"),
    CANCEL(5, " 取消");

    private int code;

    private String text;

    public static PushStatus codeOf(int code) {
        for (PushStatus pushStatus : PushStatus.values()) {
            if (pushStatus.code == code) {
                return pushStatus;
            }
        }
        throw new IllegalArgumentException("invalid status code [" + code + "] to generate " + PushStatus.class.getName());
    }

    PushStatus(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int code() {
        return code;
    }

    public String text() {
        return text;
    }
}
