package qunar.tc.qconfig.admin.model;

/**
 * Created by pingyang.yang on 2018/11/26
 */
public enum AccessRoleType {

    ADMIN(1,"管理员"),

    OWNER(2,"所有人"),

    DEV(3,"开发人员");

    private int code;
    private String text;

    AccessRoleType(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static AccessRoleType codeOf(int code) {
        for (AccessRoleType statusType : AccessRoleType.values()) {
            if (statusType.code == code) {
                return statusType;
            }
        }
        throw new IllegalArgumentException("invalid status code [" + code + "] to generate " + AccessRoleType.class.getName());
    }

    public static AccessRoleType fromText(String text) {
        for (AccessRoleType statusType : AccessRoleType.values()) {
            if (statusType.text().equals(text)) {
                return statusType;
            }
        }
        throw new IllegalArgumentException("invalid status text [" + text + "] to generate " + AccessRoleType.class.getName());
    }

    public int code() {
        return code;
    }

    public String text() {
        return text;
    }
}
