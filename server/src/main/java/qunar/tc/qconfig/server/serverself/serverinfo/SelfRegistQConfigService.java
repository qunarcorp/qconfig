package qunar.tc.qconfig.server.serverself.serverinfo;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.UploadResult;
import qunar.tc.qconfig.client.Uploader;
import qunar.tc.qconfig.client.impl.ConfigUploader;
import qunar.tc.qconfig.client.impl.Snapshot;
import qunar.tc.qconfig.client.impl.VersionProfile;
import qunar.tc.qconfig.common.util.ApiResponseCode;
import qunar.tc.qconfig.common.util.QConfigAttributesLoader;
import qunar.tc.qconfig.server.serverself.eureka.ServerStore;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2018 2018/3/28 11:59
 */
@Service
// todo: 这里其实有点问题，只用了ip，没区分port，只是现在server一般不会在一个ip部署两台...
public class SelfRegistQConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SelfRegistQConfigService.class);

    private static final int MAX_RETRY = 3;

    private static final String QCONFIG_SERVERS_FILE = "qconfig-servers";

    @Resource
    private ServerStore serverStore;

    @PostConstruct
    public void init() {
//        QCloudUtils.isUniqueApp("qconfig");
        // admin不再通过qconfig-server获取server的机器列表
        // 为了server能完成自举，删除此逻辑
        // registerSelfToQConfigFile();
    }

    private void registerSelfToQConfigFile() {
        if (!QConfigAttributesLoader.getInstance().getRegisterSelfOnStart()) {
            return;
        }
        Uploader uploader = ConfigUploader.getInstance();
        int retry = 0;

        Exception ex = null;
        while (retry++ < MAX_RETRY) {
            try {
                Optional<Snapshot<String>> current = uploader.getCurrent(QCONFIG_SERVERS_FILE);
                if (!current.isPresent()) {
                    throw new RuntimeException("qconfig-servers file not exist");
                }

                VersionProfile version = current.get().getVersion();
                String servers = current.get().getContent();
                if (containsSelf(servers)) {
                    return;
                } else {
                    UploadResult uploadResult = uploader.uploadAtVersion(version, QCONFIG_SERVERS_FILE, servers + "\n" + serverStore.self().getIp());
                    if (uploadResult.getCode() == ApiResponseCode.OK_CODE) {
                        return;
                    } else {
                        throw new RuntimeException("code [" + uploadResult.getCode() + "], error message [" + uploadResult.getMessage() + "]");
                    }
                }
            } catch (Exception e) {
                logger.debug("", e);
                ex = e;
            }
        }

        logger.error("can not register self to qconfig file", ex);
        throw new RuntimeException("can not register self to qconfig file", ex);
    }

    private boolean containsSelf(String content) {
        try {
            ImmutableList<String> servers = CharSource.wrap(content).readLines();
            for (String serverIp : servers) {
                serverIp = serverIp.trim();
                if (serverIp.equals(serverStore.self().getIp())) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException("unexpected error", e);
        }
    }
}
