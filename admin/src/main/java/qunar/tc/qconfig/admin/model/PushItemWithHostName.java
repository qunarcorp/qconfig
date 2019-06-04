package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.admin.support.HostnameUtil;

/**
 * @author zhenyu.nie created on 2014 2014/6/18 12:45
 */
public class PushItemWithHostName {

    private String hostname;

    private String ip;

    private int port;

    private String sourceGroup;

    private String sourceDataId;

    private String sourceProfile;

    public PushItemWithHostName(String ip, int port, String sourceGroup, String sourceDataId, String sourceProfile) {
        this.hostname = HostnameUtil.getHostnameFromIp(ip);
        this.ip = ip;
        this.port = port;
        this.sourceGroup = sourceGroup;
        this.sourceDataId = sourceDataId;
        this.sourceProfile = sourceProfile;
    }

    public PushItemWithHostName(String hostname, String ip, int port, String sourceGroup, String sourceDataId, String sourceProfile) {
        if (!ip.equals(hostname)) {
            HostnameUtil.setHostName(ip, hostname);
        }

        this.hostname = HostnameUtil.getHostnameFromIp(ip);
        this.ip = ip;
        this.port = port;

        this.sourceGroup = sourceGroup;
        this.sourceDataId = sourceDataId;
        this.sourceProfile = sourceProfile;
    }

    public String getHostname() {
        if (ip.equals(hostname)) {
            hostname = HostnameUtil.getHostnameFromIp(ip);
        }
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getSourceGroup() {
        return sourceGroup;
    }

    public String getSourceDataId() {
        return sourceDataId;
    }

    public String getSourceProfile() {
        return sourceProfile;
    }

    @Override
    public String toString() {
        return "PushItemWithHostName{" +
                "hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", sourceGroup='" + sourceGroup + '\'' +
                ", sourceDataId='" + sourceDataId + '\'' +
                ", sourceProfile='" + sourceProfile + '\'' +
                '}';
    }
}
