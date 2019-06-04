package qunar.tc.qconfig.admin.model;

/**
 * @author zhenyu.nie created on 2016 2016/4/7 13:58
 */
public class TemplateMeta implements Comparable<TemplateMeta> {

    private  String group;
    private final String template;
    private final String description;
    private final TemplateType type;

    public TemplateMeta(String group, String template, String description, TemplateType type) {
        this.group = group;
        this.template = template;
        this.description = description;
        this.type = type;
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

    public String getDescription() {
        return description;
    }

    public TemplateType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateMeta)) return false;

        TemplateMeta that = (TemplateMeta) o;

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
    public int compareTo(TemplateMeta o) {
        int result = this.group.compareTo(o.getGroup());
        if (result != 0) {
            return result;
        }

        return this.template.compareTo(o.getTemplate());
    }
}
