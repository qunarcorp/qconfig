package qunar.tc.qconfig.admin.cloud.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class EnvironmentVo {

    // env
    private String name;

    private List<String> buildGroups;

    // 排序使用
    @JsonIgnore
    private Integer order = Integer.MAX_VALUE;

    public EnvironmentVo() {
    }

    public EnvironmentVo(String env, List<String> buildGroups) {
        this.name = env;
        this.buildGroups = buildGroups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getBuildGroups() {
        return buildGroups;
    }

    public void setBuildGroups(List<String> buildGroups) {
        this.buildGroups = buildGroups;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }


    @Override
    public String toString() {
        return "EnvironmentVo{" +
                "name='" + name + '\'' +
                ", buildGroups=" + buildGroups +
                '}';
    }
}
