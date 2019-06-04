package qunar.tc.qconfig.admin.event;

/**
 * @author zhenyu.nie created on 2014 2014/5/22 22:04
 */
public enum ConfigOperationEvent {

    NEW(1, "新建"),
    UPDATE(2, "更新"),
    REJECT(3, "审核拒绝"),
    APPROVE(4, "审核通过"),
    PUBLISH(5, "发布"),
    CANCEL(6, "回退审核"),
    PUSH(7, "推送"),
    MAKE_PUBLIC(8, "公开文件"),
    MAKE_PRIVATE(9, "撤销公开文件"),
    DELETE(10, "删除"),
    MAKE_INHERIT(11, "继承文件"),
    MAKE_REST(12, "REST文件"),
    COPY(13, "拷贝文件");

    private int code;
    private String text;

    ConfigOperationEvent(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int code() {
        return code;
    }

    public String text() {
        return this.text;
    }

    public static ConfigOperationEvent of(int code) {
        for (ConfigOperationEvent type : ConfigOperationEvent.values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("illegal code " + code + " to generate config operation type");
    }
}
