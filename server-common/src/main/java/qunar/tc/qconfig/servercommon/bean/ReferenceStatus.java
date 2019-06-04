package qunar.tc.qconfig.servercommon.bean;

/**
 * Date: 14-7-9
 * Time: 上午11:57
 *
 * @author: xiao.liang
 * @description:
 */
public enum ReferenceStatus {

    NORMAL(0, "正常"), DELETE(1, "删除"), REFER_PRIVATE(2, "被引用文件私有");

    private int code;

    private String text;

    ReferenceStatus(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int code() {
        return code;
    }

    public String text() {
        return text;
    }

    public static ReferenceStatus codeOf(int status) {
        for (ReferenceStatus referenceStatus : ReferenceStatus.values()) {
            if (referenceStatus.code == status) {
                return referenceStatus;
            }
        }
        throw new IllegalArgumentException("invalid status code: " + status + " to generate " + ReferenceStatus.class.getName());
    }
}
