package qunar.tc.qconfig.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.util.Date;

public class PropertiesEntryDiff extends PropertiesEntry {

    private long id;
    private long lastVersion;
    private String lastValue;
    private String comment;
    private EntryDiffType type;
    private String operator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    public PropertiesEntryDiff(String group, String profile, String dataId, String key) {
        super(key, group, profile, dataId, 0, null);
    }

    public PropertiesEntryDiff(ConfigMeta meta, String key, VersionData<String> current, VersionData<String> last,
                               String comment, EntryDiffType type, String operator) {
        super(key, meta.getGroup(), meta.getProfile(), meta.getDataId(), current.getVersion(), current.getData());
        this.lastVersion = last.getVersion();
        this.lastValue = last.getData();
        this.comment = comment;
        this.type = type;
        this.operator = operator;
    }

    public PropertiesEntryDiff(String key, String groupId, String profile, String dataId, long version, String value,
                               long lastVersion, String lastValue, String comment, EntryDiffType type, String operator, Date createTime) {
        super(key, groupId, profile, dataId, version, value);
        this.lastVersion = lastVersion;
        this.lastValue = lastValue;
        this.comment = comment;
        this.operator = operator;
        this.createTime = createTime;
        this.type = type;
    }

    public PropertiesEntryDiff(long id, String key, String groupId, String profile, String dataId, long version, String value,
                               long lastVersion, String lastValue, String comment, EntryDiffType type, String operator, Date createTime) {
        this(key, groupId, profile, dataId, version, value, lastVersion, lastValue, comment, type, operator, createTime);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOperator() {
        return operator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public EntryDiffType getType() {
        return type;
    }

    public long getLastVersion() {
        return lastVersion;
    }

    public String getLastValue() {
        return lastValue;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "PropertiesEntryDiff{" +
                "id=" + id +
                ", lastVersion=" + lastVersion +
                ", lastValue='" + lastValue + '\'' +
                ", comment='" + comment + '\'' +
                ", type=" + type +
                ", operator='" + operator + '\'' +
                ", createTime=" + createTime +
                "} " + super.toString();
    }

    public enum EntryDiffType {

        INIT(0, "init"),    // 旧文件第一次记录时，记录所有未变更的key为init起点状态，方便查找
        ADDED(1, "added"),
        DELETED(2, "deleted"),
        MODIFIED(3, "modified");

        int code;
        String text;

        EntryDiffType(int code, String text) {
            this.code = code;
            this.text = text;
        }

        public int getCode() {
            return code;
        }

        public String getText() {
            return text;
        }

        public static EntryDiffType codeOf(int code) {
            for (EntryDiffType diffType : EntryDiffType.values()) {
                if (diffType.code == code) {
                    return diffType;
                }
            }
            throw new IllegalArgumentException("EntryDiffType非法的code:" + code);
        }
    }

}
