package qunar.tc.qconfig.server.domain;

import com.google.common.base.Strings;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2018 2018/3/28 15:28
 */
public class RelationMeta {

    private final ConfigMeta source;

    private final ConfigMeta target;

    public RelationMeta(ConfigMeta source, ConfigMeta target) {
        this.source = source;
        this.target = target;
    }

    public ConfigMeta getSource() {
        return source;
    }

    public ConfigMeta getTarget() {
        return target;
    }

    public boolean isLegal() {
        return !Strings.isNullOrEmpty(source.getGroup())
                && !Strings.isNullOrEmpty(source.getDataId())
                && !Strings.isNullOrEmpty(source.getProfile())
                && !Strings.isNullOrEmpty(target.getGroup())
                && !Strings.isNullOrEmpty(target.getProfile())
                && !Strings.isNullOrEmpty(target.getDataId());
    }

    @Override
    public String toString() {
        return "RelationMeta{" +
                "source=" + source +
                ", target=" + target +
                '}';
    }
}
