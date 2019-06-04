package qunar.tc.qconfig.admin.web.bean;

/**
 * @author songxue created on 2015 15-1-5
 * @version 1.0.0
 */
public class EncryptInfoBean {
    private String key;
    private boolean encrypted;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }
}
