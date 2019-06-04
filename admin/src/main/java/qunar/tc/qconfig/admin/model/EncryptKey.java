package qunar.tc.qconfig.admin.model;

/**
 * @author zhenyu.nie created on 2014 2014/12/30 15:43
 */
public class EncryptKey {

    private String key;

    private EncryptKeyStatus status;

    public EncryptKey() {
    }

    public EncryptKey(String key, EncryptKeyStatus status) {
        this.key = key;
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public EncryptKeyStatus getStatus() {
        return status;
    }

    public void setStatus(EncryptKeyStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "EncryptKey{" +
                "key='" + key + '\'' +
                ", status=" + status +
                '}';
    }
}
