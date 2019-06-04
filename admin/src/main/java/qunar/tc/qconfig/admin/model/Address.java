package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.admin.support.HostnameUtil;

/**
 * @author zhenyu.nie created on 2018 2018/5/18 16:53
 */
public class Address {

    private final String hostname;

    private final String ip;

    private final int port;

    public Address(String ip, int port) {
        this.hostname = HostnameUtil.getHostnameFromIp(ip);
        this.ip = ip;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (port != address.port) return false;
        return ip != null ? ip.equals(address.ip) : address.ip == null;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "Address{" +
                "hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}
