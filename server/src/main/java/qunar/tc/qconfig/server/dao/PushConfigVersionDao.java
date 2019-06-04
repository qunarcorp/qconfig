package qunar.tc.qconfig.server.dao;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.PushConfigVersionItem;

import java.util.List;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 12:14
 */
public interface PushConfigVersionDao {

    List<PushConfigVersionItem> select();

    PushConfigVersionItem select(ConfigMeta meta, String ip);
}
