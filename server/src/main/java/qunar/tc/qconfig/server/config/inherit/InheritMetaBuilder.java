package qunar.tc.qconfig.server.config.inherit;

import com.google.common.base.Preconditions;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 12:20
 */
public class InheritMetaBuilder {

    public static InheritMetaBuilder builder() {
        return new InheritMetaBuilder();
    }

    private ConfigMeta child;

    private ConfigMeta parent;

    private InheritMetaBuilder() {

    }

    public InheritMetaBuilder child(ConfigMeta meta) {
        this.child = meta;
        return this;
    }

    public InheritMetaBuilder parent(ConfigMeta meta) {
        this.parent = meta;
        return this;
    }

    public InheritMeta build() {
        Preconditions.checkNotNull(parent, "parent can not be null");
        Preconditions.checkNotNull(child, "child can not be null");
        return new InheritMeta(parent, child);
    }
}
