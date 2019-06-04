package qunar.tc.qconfig.server.feature;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.util.FileUtil;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutDataId;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhenyu.nie created on 2018 2018/1/29 12:06
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    private static final File configStatisticsFile = new File(FileUtil.getFileStore(), "configStatistics");

    private Cache<ConfigMetaWithoutDataId, Integer> cache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();

    private ReentrantLock lock = new ReentrantLock();

    @Override
    public void recordConfigs(ConfigMetaWithoutDataId meta, int num) {
        cache.put(meta, num);
    }

    @Override
    public boolean doConfigStatistics() {
        if (lock.tryLock()) {
            try (PrintWriter writer = new PrintWriter(configStatisticsFile)) {
                for (Map.Entry<ConfigMetaWithoutDataId, Integer> entry : cache.asMap().entrySet()) {
                    writer.print(entry.getKey().getGroup());
                    writer.print("\t");
                    writer.print(entry.getKey().getProfile());
                    writer.print("\t");
                    writer.println(entry.getValue());
                }
                return true;
            } catch (Throwable e) {
                logger.error("do configs statistics error", e);
                throw new RuntimeException(e);
            }
        } else {
            return false;
        }
    }
}
