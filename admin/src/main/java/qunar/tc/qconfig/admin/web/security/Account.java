package qunar.tc.qconfig.admin.web.security;

/**
 * @author zhenyu.nie created on 2017 2017/10/30 19:29
 */
public class Account {

    private String userId;

    private String type;

    public Account() {
    }

    public Account(String userId) {
        this(userId, "user");
    }

    public Account(String userId, String type) {
        this.userId = userId;
        this.type = type;
    }


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Account{" +
                ", userId='" + userId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
