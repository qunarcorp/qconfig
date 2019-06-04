package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.EncryptKey;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/12/30 11:15
 */
public interface EncryptKeyDao {

    List<String> selectEncryptedKeys(String group, String dataId);

    List<EncryptKey> select(String group, String dataId);

    void insertOrUpdate(String group, String dataId, String operator, List<EncryptKey> keys);
}
