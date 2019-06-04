package qunar.tc.qconfig.admin.model;



import java.sql.Timestamp;

public class VersionDetail implements Comparable<VersionDetail>{

    private Long version;

    private String operator;

    private Timestamp createTime;

    private String description;

    public VersionDetail() {
    }

    public VersionDetail(Long version, String operator, Timestamp createTime) {
        this.version = version;
        this.operator = operator;
        this.createTime = createTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "VersionDetail{" +
                "version=" + version +
                ", operator='" + operator + '\'' +
                ", createTime=" + createTime +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public int compareTo(VersionDetail o) {
        return this.version.compareTo(o.version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionDetail that = (VersionDetail) o;

        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (operator != null ? !operator.equals(that.operator) : that.operator != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        return description != null ? description.equals(that.description) : that.description == null;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
