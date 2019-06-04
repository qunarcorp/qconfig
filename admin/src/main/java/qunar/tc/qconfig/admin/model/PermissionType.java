package qunar.tc.qconfig.admin.model;

/**
 * Created by chenjk on 2018/1/14.
 */
public enum PermissionType {

    FOLDER(0, "目录"),

    PERMISSION(1, "权限节点");

    private int code;

    private String msg;

    PermissionType(int code ,String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
