package qunar.tc.qconfig.admin.service.impl;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.PushConfigVersionDao;
import qunar.tc.qconfig.admin.model.DbEnv;
import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.admin.service.PushConfigVersionService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 16:43
 */
@Service
public class PushConfigVersionServiceImpl implements PushConfigVersionService {

    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static final int BATCH_NUM = 200;

    @Resource
    private PushConfigVersionDao pushConfigVersionDao;

    @Override
    public void update(ConfigMeta meta, List<Host> hosts, long version) {
        if (hosts.isEmpty()) {
            return;
        }

        List<String> ips = Lists.newArrayListWithCapacity(hosts.size());
        for (Host host : hosts) {
            ips.add(host.getIp());
        }

        pushConfigVersionDao.update(meta, ips, version);
    }

    @Override
    public void asyncDelete(final ConfigMeta meta, final long maxVersion) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Map.Entry<Long, Long>> idVersions =
                        pushConfigVersionDao.selectIdAndVersions(meta, maxVersion);

                int startIndex = 0;
                while (startIndex < idVersions.size()) {
                    int endIndex = Math.min(startIndex + BATCH_NUM, idVersions.size());
                    pushConfigVersionDao.delete(idVersions.subList(startIndex, endIndex));
                    startIndex = endIndex;
                }
            }
        });
    }
}
