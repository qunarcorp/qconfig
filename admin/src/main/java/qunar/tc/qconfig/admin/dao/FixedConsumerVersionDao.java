package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.dto.ConsumerVersionDto;
import qunar.tc.qconfig.admin.model.FixedVersionRecord;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author yunfeng.yang
 * @since 2017/5/16
 */
public interface FixedConsumerVersionDao {

    List<ConsumerVersionDto> find(ConfigMeta configMeta);

    List<FixedVersionRecord> scan(long start, long limit);

    int insertOrUpdateBeta(FixedVersionRecord record);

    int add(ConfigMeta configMeta, String ip, long version, String operator);

    int delete(ConfigMeta configMeta, String ip);
}
