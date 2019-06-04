package qunar.tc.qconfig.server.config.inherit;

import com.google.common.base.Optional;
import qunar.tc.qconfig.server.config.ConfigInfoService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 11:37
 */
public class InheritJudgementAdaptor implements InheritJudgement {

    public static InheritJudgement create(ConfigInfoService configInfoService) {
        return new InheritJudgementAdaptor(configInfoService);
    }

    private ConfigInfoService configInfoService;

    private InheritJudgementAdaptor(ConfigInfoService configInfoService) {
        this.configInfoService = configInfoService;
    }

    @Override
    public boolean exist(InheritMeta meta) {
        Optional<ConfigMeta> parent = configInfoService.getParent(meta.getChild());
        return parent.isPresent() && parent.get().equals(meta.getParent());
    }
}
