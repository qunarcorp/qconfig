package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.FileContentMD5;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * Created by pingyang.yang on 2018/10/23
 */
public interface FileContentMD5Dao {

    int insert(FileContentMD5 fileContent);

    String selectMD5(ConfigMeta configMeta, int version);

    int selectVersionByMD5(ConfigMeta configMeta, String MD5);

    int completeDelete(ConfigMeta meta);
}
