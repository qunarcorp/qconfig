package qunar.tc.qconfig.admin.event;

/**
 *
 * 优先级
 *
 * Created by chenjk on 2017/6/16.
 */
public enum NocPriority {

    HIGH("高"),
    MID("中"),
    LOW("低");

    private String text;

    NocPriority(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
