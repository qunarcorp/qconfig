package qunar.tc.qconfig.admin.model;

import java.sql.Timestamp;

/**
 * @author zhenyu.nie created on 2017 2017/4/18 17:22
 */
public class TemplateInfo {

    private String group;
    private String template;
    private TemplateType type;
    private String detail;
    private String description;
    private int version;
    private Timestamp updateTime;
    private String operator;

    public TemplateInfo() {
    }

    public TemplateInfo(String group, String template, TemplateType type, String detail, String description, int version, Timestamp updateTime) {
        this.group = group;
        this.template = template;
        this.type = type;
        this.detail = detail;
        this.description = description;
        this.version = version;
        this.updateTime = updateTime;
    }

    public TemplateInfo(String group, String template, TemplateType type, String detail, String description,
            int version, Timestamp updateTime, String operator) {
        this.group = group;
        this.template = template;
        this.type = type;
        this.detail = detail;
        this.description = description;
        this.version = version;
        this.updateTime = updateTime;
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public TemplateType getType() {
        return type;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "TemplateInfo{" +
                "group='" + group + '\'' +
                ", template='" + template + '\'' +
                ", detail='" + detail + '\'' +
                ", description='" + description + '\'' +
                ", version=" + version +
                '}';
    }
}
