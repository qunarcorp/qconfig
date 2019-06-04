package qunar.tc.qconfig.server.support.context;

import org.springframework.stereotype.Service;

/**
 * @author zhenyu.nie created on 2014 2014/7/11 12:01
 */
@Service
public class ClientInfoServiceImpl implements ClientInfoService {

    private ThreadLocal<String> group = new ThreadLocal<String>();

    private ThreadLocal<String> corp = new ThreadLocal<String>();

    private ThreadLocal<String> ip = new ThreadLocal<String>();

    private ThreadLocal<Integer> port = new ThreadLocal<Integer>();

    private ThreadLocal<String> env = new ThreadLocal<String>();

    private ThreadLocal<String> profile = new ThreadLocal<String>();

    private ThreadLocal<Boolean> noToken = new ThreadLocal<Boolean>();

    private ThreadLocal<String> room = new ThreadLocal<String>();

    @Override
    public String getGroup() {
        return group.get();
    }

    @Override
    public void setGroup(String group) {
        this.group.set(group);
    }

    @Override
    public String getCorp() {
        return corp.get();
    }

    @Override
    public void setCorp(String corp) {
        this.corp.set(corp);
    }

    @Override
    public String getEnv() {
        return env.get();
    }

    @Override
    public void setEnv(String env) {
        this.env.set(env);
    }

    @Override
    public String getProfile() {
        return profile.get();
    }

    @Override
    public void setProfile(String profile) {
        this.profile.set(profile);
    }

    @Override
    public String getIp() {
        return ip.get();
    }

    @Override
    public void setIp(String ip) {
        this.ip.set(ip);
    }

    @Override
    public int getPort() {
        return port.get();
    }

    @Override
    public void setPort(int port) {
        this.port.set(port);
    }

    @Override
    public boolean isNoToken() {
        return Boolean.TRUE == noToken.get();
    }

    @Override
    public void setNoToken(Boolean noToken) {
        this.noToken.set(noToken);
    }

    @Override
    public String getRoom() {
        return this.room.get();
    }

    @Override
    public void setRoom(String room) {
        this.room.set(room);
    }

    @Override
    public void clear() {
        corp.remove();
        group.remove();
        ip.remove();
        port.remove();
        env.remove();
        profile.remove();
        noToken.remove();
        room.remove();
    }
}