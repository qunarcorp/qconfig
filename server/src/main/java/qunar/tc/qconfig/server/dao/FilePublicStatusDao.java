package qunar.tc.qconfig.server.dao;

import com.google.common.base.Optional;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import qunar.tc.qconfig.servercommon.bean.PublicConfigInfo;

import java.util.List;

/**
 * @author zhenyu.nie created on 2016 2016/5/3 15:01
 */
public interface FilePublicStatusDao {

    List<PublicConfigInfo> loadAll();

    boolean exist(PublicConfigInfo file);

    Optional<PublicConfigInfo> loadPublicInfo(ConfigMetaWithoutProfile configMetaWithoutProfile);

}
