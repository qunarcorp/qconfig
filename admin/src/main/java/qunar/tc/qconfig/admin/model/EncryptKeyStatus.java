package qunar.tc.qconfig.admin.model;

/**
 * @author zhenyu.nie created on 2014 2014/12/30 14:04
 */
public enum EncryptKeyStatus {

    ENCRYPTED(0, "需加密"), PUBLIC(1, "公开");

    private int code;

    private String text;

    EncryptKeyStatus(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int code() {
        return code;
    }

    public String text() {
        return text;
    }

    public static EncryptKeyStatus codeOf(int status) {
        for (EncryptKeyStatus encryptKeyStatus : EncryptKeyStatus.values()) {
            if (encryptKeyStatus.code == status) {
                return encryptKeyStatus;
            }
        }
        throw new IllegalArgumentException("invalid status code: " + status + " to generate " + EncryptKeyStatus.class.getName());
    }
}
