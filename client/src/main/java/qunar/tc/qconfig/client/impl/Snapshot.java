package qunar.tc.qconfig.client.impl;

import qunar.tc.qconfig.client.FeatureRemote;
import qunar.tc.qconfig.common.bean.StatusType;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-5-12.
 */
public class Snapshot<T> {

    private VersionProfile version;
    private T content;
    private StatusType statusType;
    private String group;
    private String dataId;

    private FeatureRemote featureRemote = FeatureRemote.DEFAULT;

    public Snapshot() {

    }

    public Snapshot(String profile, long version, T content) {
        this.version = new VersionProfile(version, profile);
        this.content = content;
    }

    public Snapshot(VersionProfile version, T content, FeatureRemote featureRemote) {
        this.version = version;
        this.content = content;
        this.featureRemote = featureRemote;
    }

    public Snapshot(String profile, long version, T content, StatusType statusType) {
        this.version = new VersionProfile(version, profile);
        this.content = content;
        this.statusType = statusType;
    }

    public Snapshot(String group, String dataId, VersionProfile version, StatusType statusType, T content) {
        this.version = version;
        this.content = content;
        this.statusType = statusType;
        this.group = group;
        this.dataId = dataId;
    }

    public FeatureRemote getFeatureRemote() {
        return featureRemote;
    }

    public void setFeatureRemote(FeatureRemote featureRemote) {
        this.featureRemote = featureRemote;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public T getContent() {
        return content;
    }

    public VersionProfile getVersion() {
        return version;
    }

    public void setVersion(VersionProfile version) {
        this.version = version;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public StatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(StatusType statusType) {
        this.statusType = statusType;
    }

    @Override
    public String toString() {
        return "Snapshot{" +
                "version=" + version +
                ", content=" + content +
                ", statusType=" + statusType +
                ", group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", featureRemote=" + featureRemote +
                '}';
    }
}
