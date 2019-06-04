package qunar.tc.qconfig.server.config.inherit;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2018 2018/3/28 17:32
 */
public class InheritMeta {

    private final ConfigMeta parent;

    private final ConfigMeta child;

    InheritMeta(ConfigMeta parent, ConfigMeta child) {
        this.parent = parent;
        this.child = child;
    }

    public ConfigMeta getParent() {
        return parent;
    }

    public ConfigMeta getChild() {
        return child;
    }

    @Override
    public String toString() {
        return "InheritMeta{" +
                "parent=" + parent +
                ", child=" + child +
                '}';
    }
}
