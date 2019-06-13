package qunar.tc.qconfig.common.bean;

import qunar.tc.qconfig.common.enums.AppServerType;

public class AppServerConfig {

    private final String name;

    private final String token;

    private final String env;

    private final String ip;

    private final AppServerType type;

    private final int port;

    private final String profile;

    private final String subEnv;

    private final String room;

    public AppServerConfig(String name, String env, String token, String ip, int port, AppServerType type, String profile, String subEnv, String room) {
        this.name = name;
        this.token = token;
        this.env = env;
        this.ip = ip;
        this.type = type;
        this.port = port;
        this.profile = profile;
        this.subEnv = subEnv;
        this.room = room;
    }

    public String getRoom() {
        return room;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public String getEnv() {
        return env;
    }

    public String getIp() {
        return ip;
    }

    public AppServerType getType() {
        return type;
    }

    public int getPort() {
        return port;
    }

    public String getProfile() {
        return profile;
    }

    public String getSubEnv() {
        return subEnv;
    }
}
