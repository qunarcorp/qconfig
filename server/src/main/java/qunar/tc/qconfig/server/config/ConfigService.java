package qunar.tc.qconfig.server.config;

import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 3:37
 */
public interface ConfigService {

    Collection<Changed> checkChangedList(QFileFactory qFileFactory, List<CheckRequest> requests, String ip, boolean needPurge);

    Map.Entry<QFile, ChecksumData<String>> findConfig(QFileFactory qFileFactory, VersionData<ConfigMeta> configId) throws ConfigNotFoundException;

    Map.Entry<QFile, VersionData<ChecksumData<String>>> forceLoad(QFileFactory qFileFactory, String ip, VersionData<ConfigMeta> configId) throws ConfigNotFoundException;

}
