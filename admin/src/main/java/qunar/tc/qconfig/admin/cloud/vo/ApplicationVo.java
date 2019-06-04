package qunar.tc.qconfig.admin.cloud.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class ApplicationVo {

    private String appcode;

    private String name;

    private List<EnvironmentVo> envs;

    //自定义排序使用
    @JsonIgnore
    private Integer order = Integer.MAX_VALUE;

    public ApplicationVo() {
    }

    public ApplicationVo(String appcode, String name, List<EnvironmentVo> envs) {
        this.appcode = appcode;
        this.name = name;
        this.envs = envs;
    }

    public String getAppcode() {
        return appcode;
    }

    public void setAppcode(String appcode) {
        this.appcode = appcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EnvironmentVo> getEnvs() {
        return envs;
    }

    public void setEnvs(List<EnvironmentVo> envs) {
        this.envs = envs;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "ApplicationVo{" +
                "appcode='" + appcode + '\'' +
                ", name='" + name + '\'' +
                ", envs=" + envs +
                '}';
    }
}
