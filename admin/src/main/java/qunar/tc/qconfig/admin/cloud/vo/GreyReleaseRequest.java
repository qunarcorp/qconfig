package qunar.tc.qconfig.admin.cloud.vo;

public class GreyReleaseRequest {

    private String uuid;

    private int lockVersion;

    public GreyReleaseRequest() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(int lockVersion) {
        this.lockVersion = lockVersion;
    }

    @Override
    public String toString() {
        return "GreyReleaseRequest{" +
                "uuid='" + uuid + '\'' +
                ", lockVersion=" + lockVersion +
                '}';
    }
}
