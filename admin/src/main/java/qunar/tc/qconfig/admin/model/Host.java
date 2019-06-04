package qunar.tc.qconfig.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import qunar.tc.qconfig.admin.support.HostnameUtil;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 10:41
 */
public class Host {

    @JsonIgnore
    private String hostname;

    private String ip;

    public Host() {
    }

    public Host(String ip) {
        this.hostname = HostnameUtil.getHostnameFromIp(ip);
        this.ip = ip;
    }

    public Host(String hostname, String ip) {
        this.hostname = hostname;
        this.ip = ip;
    }

    @JsonDeserialize
    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Host host = (Host) o;

        return ip != null ? ip.equals(host.ip) : host.ip == null;
    }

    @Override
    public int hashCode() {
        return ip != null ? ip.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Host{" +
                "hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
