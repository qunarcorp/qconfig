package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.GroupOpLog;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/22 20:05
 */
public interface GroupOpLogDao {

    int insert(GroupOpLog groupOpLog);

    List<GroupOpLog> selectRecentByGroup(String group);
}
