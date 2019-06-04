package qunar.tc.qconfig.admin.cloud.vo;

import com.google.common.base.Strings;
import qunar.tc.qconfig.admin.model.TemplateType;

/**
 * Created by pingyang.yang on 2018/11/14
 */
public class TemplateMetaWithName implements Comparable<TemplateMetaWithName> {

    private  String group;
    private final String template;
    private final String description;
    private final TemplateType type;
    private final String fileName;
    private final boolean isNameLimit;

    public TemplateMetaWithName(String group, String template, String description, TemplateType type, String fileName) {
        this.group = group;
        this.template = template;
        this.description = description;
        this.type = type;
        this.fileName = fileName;
        this.isNameLimit = !Strings.isNullOrEmpty(fileName);
    }

    public String getGroup() {
        return group;
    }

    public boolean isNameLimit() {
        return isNameLimit;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTemplate() {
        return template;
    }

    public String getDescription() {
        return description;
    }

    public TemplateType getType() {
        return type;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateMetaWithName)) return false;

        TemplateMetaWithName that = (TemplateMetaWithName) o;

        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        return template != null ? template.equals(that.template) : that.template == null;

    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + (template != null ? template.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TemplateMeta{" +
                "group='" + group + '\'' +
                ", template='" + template + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public int compareTo(TemplateMetaWithName o) {
        int result = this.group.compareTo(o.getGroup());
        if (result != 0) {
            return result;
        }

        return this.template.compareTo(o.getTemplate());
    }
}
