package qunar.tc.qconfig.admin.web.bean;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 15:12
 */
public class PermissionInfoBean {

    private String rtxId;

    private boolean edit = false;

    private boolean approve = false;

    private boolean publish = false;

    public PermissionInfoBean() {
    }

    public PermissionInfoBean(String rtxId, boolean edit, boolean approve, boolean publish) {
        this.rtxId = rtxId;
        this.edit = edit;
        this.approve = approve;
        this.publish = publish;
    }

    public String getRtxId() {
        return rtxId;
    }

    public void setRtxId(String rtxId) {
        this.rtxId = rtxId;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isApprove() {
        return approve;
    }

    public void setApprove(boolean approve) {
        this.approve = approve;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    @Override
    public String toString() {
        return "PermissionList{" +
                "rtxId='" + rtxId + '\'' +
                ", edit=" + edit +
                ", approve=" + approve +
                ", publish=" + publish +
                '}';
    }
}
