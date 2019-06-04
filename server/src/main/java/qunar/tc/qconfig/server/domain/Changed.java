package qunar.tc.qconfig.server.domain;

import com.google.common.collect.ImmutableList;
import qunar.tc.qconfig.server.web.servlet.AbstractCheckConfigServlet;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * User: zhaohuiyu Date: 5/13/14 Time: 5:49 PM
 */
public class Changed {

    private String group;
    private String dataId;
    private String profile;
    private long newestVersion;

    private volatile String str = null;

    public Changed(ConfigMeta meta, long newestVersion) {
        this.group = meta.getGroup();
        this.dataId = meta.getDataId();
        this.profile = meta.getProfile();
        this.newestVersion = newestVersion;
    }

    public Changed(String group, String dataId, String profile, long newestVersion) {
        this.group = group;
        this.dataId = dataId;
        this.profile = profile;
        this.newestVersion = newestVersion;
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

    public long getNewestVersion() {
        return newestVersion;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setNewestVersion(long newestVersion) {
        this.newestVersion = newestVersion;
    }

    public String str() {
        if (str == null) {
            str = AbstractCheckConfigServlet.formatOutput(ImmutableList.<Changed>of(this));
        }
        return str;
    }

    @Override
    public String toString() {
        return "Changed{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", newestVersion=" + newestVersion +
                '}';
    }
}
