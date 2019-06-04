package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.ReferenceLog;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * Date: 14-7-7
 * Time: 下午4:46
 *
 * @author: xiao.liang
 * @description:
 */
public interface ReferenceLogDao {

    int create(ReferenceLog referenceLog);
    
    List<ReferenceLog> find(ConfigMeta configMeta, RefType refType);

    List<ReferenceLog> selectRecent(String group, String profile, int length);

    int completeDelete(ConfigMeta meta);
}
