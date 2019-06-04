package qunar.tc.qconfig.admin.web.bean;

import com.google.common.collect.*;
import qunar.tc.qconfig.common.util.Environment;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/5/29 11:29
 */
public class GroupItem implements Comparable<GroupItem> {

    private String group;

    private String name;

    private Map<String, Set<String>> totalEnvs = Maps.newHashMap();

    private List<String> prod = Lists.newArrayList();

    private List<String> beta = Lists.newArrayList();

    private List<String> dev = Lists.newArrayList();

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GroupItem(String group) {
        this.group = group;
    }

    public GroupItem(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setTotalEnvs(Map<String, Set<String>> totalEnvs) {
        this.totalEnvs = totalEnvs;
    }

    public Map<String, Set<String>> getTotalEnvs() {
        return totalEnvs;
    }

    public Set<String> getSubEnvs(String env) {
        Set<String> subEnvs = totalEnvs.get(env);
        return subEnvs.isEmpty() ? ImmutableSet.<String>of() : subEnvs;
    }

    public List<String> getProd() {
        return prod;
    }

    public List<String> getBeta() {
        return beta;
    }

    public List<String> getDev() {
        return dev;
    }

    public void add(String profile) {
        Environment env = Environment.fromProfile(profile);
        String buildGroup = env.subEnv();

        if (env.isProd()) {
            prod.add(buildGroup);
        } else if (env.isBeta()) {
            beta.add(buildGroup);
        } else if (env.isDev()) {
            dev.add(buildGroup);
        }

    }

    public void sort() {
        this.dev = Ordering.natural().sortedCopy(dev);
        this.beta = Ordering.natural().sortedCopy(beta);
        this.prod = Ordering.natural().sortedCopy(prod);
    }

    @Override
    public int compareTo(GroupItem o) {
        return this.group.toLowerCase().compareTo(o.group.toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupItem groupItem = (GroupItem) o;

        if (group != null ? !group.equals(groupItem.group) : groupItem.group != null) return false;
        if (name != null ? !name.equals(groupItem.name) : groupItem.name != null) return false;
        if (totalEnvs != null ? !totalEnvs.equals(groupItem.totalEnvs) : groupItem.totalEnvs != null) return false;
        if (prod != null ? !prod.equals(groupItem.prod) : groupItem.prod != null) return false;
        if (beta != null ? !beta.equals(groupItem.beta) : groupItem.beta != null) return false;
        return dev != null ? dev.equals(groupItem.dev) : groupItem.dev == null;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (totalEnvs != null ? totalEnvs.hashCode() : 0);
        result = 31 * result + (prod != null ? prod.hashCode() : 0);
        result = 31 * result + (beta != null ? beta.hashCode() : 0);
        result = 31 * result + (dev != null ? dev.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GroupItem{" +
                "group='" + group + '\'' +
                ", name='" + name + '\'' +
                ", totalEnvs=" + totalEnvs +
                '}';
    }
}
