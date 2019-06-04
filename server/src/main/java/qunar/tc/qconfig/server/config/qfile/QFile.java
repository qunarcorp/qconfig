package qunar.tc.qconfig.server.config.qfile;

import com.google.common.base.Optional;
import qunar.tc.qconfig.server.config.longpolling.Listener;
import qunar.tc.qconfig.server.config.longpolling.impl.AsyncContextHolder;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.server.support.log.Log;
import qunar.tc.qconfig.server.support.log.LogService;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 1:38
 */
public interface QFile {

    // 代表当前文件
    ConfigMeta getSourceMeta();

    // 如果当前文件是共享了上一级文件的情况，那么返回上一级文件，否则返回自身
    ConfigMeta getSharedMeta();

    // 返回最终的文件，也就是文件内容真正所在的那一个
    ConfigMeta getRealMeta();

    void log(Log log);

    Optional<Changed> checkChange(CheckRequest request, String ip);

    ChecksumData<String> findConfig(long version) throws ConfigNotFoundException;

    VersionData<ChecksumData<String>> forceLoad(String ip, long version) throws ConfigNotFoundException;

    Listener createListener(CheckRequest request, AsyncContextHolder contextHolder);

    LogService getLogService();
}
