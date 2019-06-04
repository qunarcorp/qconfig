package qunar.tc.qconfig.servercommon.bean;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-7
 * Time: 上午11:48
 */
public class Config implements Serializable {
    private static final long serialVersionUID = 4865266153359308721L;

    private final String group;

    private final String dataId;

    private final String profile;

    private final String data;

    public Config(String group, String dataId, String profile, String data) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.data = data;
    }

    public String getGroup() {
        return group;
    }

    public String getDataId() {
        return dataId;
    }

    public String getProfile() {
        return profile;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Config{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                '}';
    }
}
