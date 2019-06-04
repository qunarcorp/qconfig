package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.DbEnv;

import java.util.List;
import java.util.Map;

/**
 * Created by dongcao on 2018/6/29.
 */
public interface ServerDao {

    /**
     * 从数据库获取所有环境的QConfig Server的ip列表
     */
    List<String> getServers();

    int deleteServer(String ip);

}
