package qunar.tc.qconfig.servercommon.bean;

/**
 * @author zhenyu.nie created on 2017 2017/4/1 18:36
 */
public class IpAndPort {

    private final String ip;

    private final int port;

    public IpAndPort(String ip, int port) {
        this.ip = ip;
        this.port = port;
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

        IpAndPort ipAndPort = (IpAndPort) o;

        if (port != ipAndPort.port) return false;
        return ip != null ? ip.equals(ipAndPort.ip) : ipAndPort.ip == null;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "IpAndPort{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
