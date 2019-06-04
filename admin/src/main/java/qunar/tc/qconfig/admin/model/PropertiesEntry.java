package qunar.tc.qconfig.admin.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * @author keli.wang
 */
public class PropertiesEntry implements Serializable {

    private static final long serialVersionUID = -8055532086917113267L;

    private final String key;
    private final String groupId;
    private final String profile;
    private final String dataId;
    private final String value;
    private final long version;

    public PropertiesEntry(final String key,
                           final String groupId,
                           final String profile,
                           final String dataId,
                           final long version,
                           final String value) {
        this.key = key;
        this.groupId = groupId;
        this.profile = profile;
        this.dataId = dataId;
        this.version = version;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getProfile() {
        return profile;
    }

    public String getDataId() {
        return dataId;
    }

    public String getValue() {
        return value;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PropertiesEntry that = (PropertiesEntry) o;
        return Objects.equal(key, that.key) &&
                Objects.equal(groupId, that.groupId) &&
                Objects.equal(profile, that.profile) &&
                Objects.equal(dataId, that.dataId) &&
                Objects.equal(value, that.value) &&
                Objects.equal(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key, groupId, profile, dataId, value, version);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                      .add("key", key)
                      .add("groupId", groupId)
                      .add("profile", profile)
                      .add("dataId", dataId)
                      .add("value", value)
                      .add("version", version)
                      .toString();
    }
}
