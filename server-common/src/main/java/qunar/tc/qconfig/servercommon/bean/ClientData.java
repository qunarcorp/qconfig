package qunar.tc.qconfig.servercommon.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by chenjk on 2018/8/2.
 */
public class ClientData implements IpPort{


    @JsonIgnore
    private String hostname;

    private String ip;

    private int port;

    private long version;

    private boolean isFixed;

    private long fixedVersion;

    public ClientData() {

    }

    public ClientData(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public ClientData(String ip, long version) {
        this.ip = ip;
        this.version = version;
    }

    public ClientData(String ip, int port, long version) {
        this.ip = ip;
        this.port = port;
        this.version = version;
    }

    public ClientData(String hostname, String ip, int port, long version, boolean isFixed, long fixedVersion) {
        this.hostname = hostname;
        this.ip = ip;
        this.port = port;
        this.version = version;
        this.isFixed = isFixed;
        this.fixedVersion = fixedVersion;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @JsonSerialize
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public void setFixed(boolean fixed) {
        isFixed = fixed;
    }

    public long getFixedVersion() {
        return fixedVersion;
    }

    public void setFixedVersion(long fixedVersion) {
        this.fixedVersion = fixedVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientData that = (ClientData) o;

        if (port != that.port) return false;
        if (version != that.version) return false;
        if (isFixed != that.isFixed) return false;
        if (fixedVersion != that.fixedVersion) return false;
        if (hostname != null ? !hostname.equals(that.hostname) : that.hostname != null) return false;
        return ip != null ? ip.equals(that.ip) : that.ip == null;
    }

    @Override
    public int hashCode() {
        int result = hostname != null ? hostname.hashCode() : 0;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (int) (version ^ (version >>> 32));
        result = 31 * result + (isFixed ? 1 : 0);
        result = 31 * result + (int) (fixedVersion ^ (fixedVersion >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ClientData{" +
                "hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", version=" + version +
                ", isFixed=" + isFixed +
                ", fixedVersion=" + fixedVersion +
                '}';
    }

    public String ipPortStr() {
        return ip + ":" + port;
    }
}
