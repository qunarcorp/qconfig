package qunar.tc.qconfig.admin.web.bean;

public enum PushType {
    GERY_RELEASE(1, "灰度发布"),
    NORMAL_PUSH(2, "推送"),
    EDIT_PUSH(3, "编辑后推送");

    private int code;

    private String text;

    public static PushType codeOf(int code) {
        for (PushType pushType : PushType.values()) {
            if (pushType.code == code) {
                return pushType;
            }
        }
        throw new IllegalArgumentException("invalid status code [" + code + "] to generate " + PushType.class.getName());
    }

    PushType(int code, String text) {
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
