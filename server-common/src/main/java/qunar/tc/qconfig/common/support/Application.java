package qunar.tc.qconfig.common.support;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by pingyang.yang on 2018/11/15
 */
public class Application implements Serializable {
    private int id;
    private String code;
    private String name;
    private String groupCode;
    private List<String> developer;
    private List<String> owner;
    private List<String> mailGroup;

    private Status status;
    private String creator;
    private Date createTime;

    public Application() {
    }

    public Application(String code) {
        this.code = code;
    }

    public Application(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Application(String code, String name, String groupCode, List<String> developer, List<String> owner, List<String> mailGroup, Status status, String creator, Date createTime) {
        this.code = code;
        this.name = name;
        this.groupCode = groupCode;
        this.developer = developer;
        this.owner = owner;
        this.mailGroup = mailGroup;
        this.status = status;
        this.creator = creator;
        this.createTime = createTime;
    }

    public Application(String code, String name, String groupCode, Status status, String creator) {
        this.code = code;
        this.name = name;
        this.groupCode = groupCode;
        this.status = status;
        this.creator = creator;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupCode() {
        return this.groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public List<String> getDeveloper() {
        return this.developer;
    }

    public void setDeveloper(List<String> developer) {
        this.developer = developer;
    }

    public List<String> getOwner() {
        return this.owner;
    }

    public void setOwner(List<String> owner) {
        this.owner = owner;
    }

    public List<String> getMailGroup() {
        return this.mailGroup;
    }

    public void setMailGroup(List<String> mailGroup) {
        this.mailGroup = mailGroup;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCreator() {
        return this.creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public static enum Status {
        unaudit(0, "未审核"),
        pass(1, "审核通过"),
        reject(2, "申请被拒绝"),
        discard(3, "已废弃");

        private int code;

        private String text;

        private Status(int code, String text) {
            this.code = code;
            this.text = text;
        }

        public int code() {
            return this.code;
        }

        public String text() {
            return this.text;
        }

        public static Status codeOf(int code) {
            for (Status statusType : Status.values()) {
                if (statusType.code == code) {
                    return statusType;
                }
            }
            throw new IllegalArgumentException("invalid status code [" + code + "] to generate " + Status.class.getName());
        }
    }
}
