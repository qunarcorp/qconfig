package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author zhenyu.nie created on 2018 2018/2/1 15:59
 */
public class RelativeReference {

    private ConfigMeta origin;

    private List<ConfigMeta> references;

    public RelativeReference(ConfigMeta origin, List<ConfigMeta> references) {
        this.origin = origin;
        this.references = references;
    }

    public ConfigMeta getOrigin() {
        return origin;
    }

    public List<ConfigMeta> getReferences() {
        return references;
    }

    @Override
    public String toString() {
        return "RelativeReference{" +
                "origin=" + origin +
                ", references=" + references +
                '}';
    }
}
