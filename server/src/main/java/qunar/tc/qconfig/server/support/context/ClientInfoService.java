package qunar.tc.qconfig.server.support.context;

/**
 * @author zhenyu.nie created on 2014 2014/7/11 12:00
 */
public interface ClientInfoService {

    String getGroup();

    void setGroup(String group);

    String getCorp();

    void setCorp(String corp);

    String getEnv();

    void setEnv(String env);

    String getProfile();

    void setProfile(String profile);

    String getIp();

    void setIp(String ip);

    int getPort();

    void setPort(int port);

    boolean isNoToken();

    void setNoToken(Boolean noToken);

    String getRoom();

    void setRoom(String room);

    void clear();
}
