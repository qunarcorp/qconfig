package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.DbEnv;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2018 2018/5/18 17:13
 */
public interface ServerListService {


    /**
     * 获取所有部署环境下的qconfig server ip列表
     *
     * @return
     */
    List<String> getServers();

    /**
     * 获取指定环境在线的qconfig servers host
     * host = ip:port
     *
     * @return
     */
    List<String> getOnlineServerHosts();

    boolean contains(String ip);

}
