package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 16:43
 */
public interface PushConfigVersionService {

    void update(ConfigMeta meta, List<Host> hosts, long version);

    void asyncDelete(ConfigMeta meta, long maxVersion);
}
