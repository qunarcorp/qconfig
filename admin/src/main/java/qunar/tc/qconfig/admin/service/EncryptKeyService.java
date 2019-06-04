package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.EncryptKey;

import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2014 2014/12/30 18:04
 */
public interface EncryptKeyService {

    boolean isEncryptedKey(List<EncryptKey> encryptKeys, String key);

    Map<String, Boolean> getEditableEncryptKeys(String group, String dataId, String profile, int editVersion);

    void insertOrUpdate(String group, String dataId, String operator, List<EncryptKey> keys);
}
