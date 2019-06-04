package qunar.tc.qconfig.server.config.qfile.impl;

import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.exception.ConfigNotFoundException;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2014 2014/10/29 1:20
 */
public class ShareQFileV2 extends AbstractShareQFile implements QFile {

    public ShareQFileV2(ConfigMeta source, QFile delegate) {
        super(source, delegate);
    }

    // 新版的findConfig请求正常情况下不可能打到ShareQFile上，只有在刚好有文件被删除的时候才有这个可能
    // 当出现这种情况的时候必须得报错，因为客户端findConfig获取的内容和之前checkConfig获取的meta必须是一致的
    @Override
    public ChecksumData<String> findConfig(long version) throws ConfigNotFoundException {
        throw new ConfigNotFoundException();
    }
}
