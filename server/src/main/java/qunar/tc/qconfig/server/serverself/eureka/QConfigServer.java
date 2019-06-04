package qunar.tc.qconfig.server.serverself.eureka;

/**
 * @author zhenyu.nie created on 2017 2017/8/10 14:57
 */
public class QConfigServer {

    private String ip;

    private int port;

    private String room;

    public QConfigServer(String ip, int port, String room) {
        this.ip = ip;
        this.port = port;
        this.room = room;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return "QConfigServer{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", room='" + room + '\'' +
                '}';
    }
}
