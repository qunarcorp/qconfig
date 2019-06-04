package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.model.FilePublicRecord;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import qunar.tc.qconfig.servercommon.bean.PublicConfigInfo;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author zhenyu.nie created on 2016 2016/5/3 15:47
 */
public interface FilePublicStatusDao {

    PublicType getPublicType(ConfigMetaWithoutProfile configMetaWithoutProfile);

    boolean exist(ConfigMetaWithoutProfile configMetaWithoutProfile);

    boolean exist(ConfigMetaWithoutProfile meta, int type);

    int insert(ConfigMetaWithoutProfile meta, int value);

    int insertOrUpdateBeta(ConfigMetaWithoutProfile meta, int type, Timestamp updateTime);

    List<FilePublicRecord> scan(Timestamp startTime, long startId, long limit);

    int batchSetPublic(List<PublicConfigInfo> publicConfigInfos);

    List<String> selectDataIds(String group);

    List<String> findAllPublicGroup(int mask);

    List<String> selectDataIds(String group, int type);

    List<String> selectPublicDataIds(String group);

    void delete(String group, String dataId);
}
