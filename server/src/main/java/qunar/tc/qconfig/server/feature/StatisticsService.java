package qunar.tc.qconfig.server.feature;

import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutDataId;

/**
 * @author zhenyu.nie created on 2018 2018/1/29 12:06
 */
public interface StatisticsService {

    void recordConfigs(ConfigMetaWithoutDataId meta, int num);

    boolean doConfigStatistics();
}
