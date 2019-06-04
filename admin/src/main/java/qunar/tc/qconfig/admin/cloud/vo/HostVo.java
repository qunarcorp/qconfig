package qunar.tc.qconfig.admin.cloud.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import qunar.tc.qconfig.admin.support.HostnameUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HostVo {
    private String ip;
    private String hostname;

    public HostVo() {
    }

    public HostVo(String ip) {
        this.ip = ip;
        this.hostname = HostnameUtil.getHostnameFromIp(ip);
    }

    public HostVo(String ip, String hostname) {
        this.ip = ip;
        this.hostname = hostname;
    }

    public HostVo(String ip, int port, String hostname) {
        this.ip = ip + port;
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return "HostVo{" +
                "ip='" + ip + '\'' +
                ", hostname='" + hostname + '\'' +
                '}';
    }
}
