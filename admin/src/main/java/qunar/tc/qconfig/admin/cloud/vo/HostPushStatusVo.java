package qunar.tc.qconfig.admin.cloud.vo;

import qunar.tc.qconfig.admin.greyrelease.MachinePushState;

public class HostPushStatusVo extends HostVo {

    private MachinePushState status;

    public HostPushStatusVo(String ip) {
        super(ip);
    }

    public HostPushStatusVo(String ip, MachinePushState status) {
        super(ip);
        this.status = status;
    }

    public MachinePushState getStatus() {
        return status;
    }

    public void setStatus(MachinePushState status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "HostPushStatusVo{" +
                "status=" + status +
                "} " + super.toString();
    }
}
