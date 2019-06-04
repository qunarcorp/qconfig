package qunar.tc.qconfig.server.support.log;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.client.spring.QMapConfig;
import qunar.tc.qconfig.common.support.concurrent.NamedThreadFactory;
import qunar.tc.qconfig.common.util.ConfigLogType;
import qunar.tc.qconfig.common.util.ConfigUsedType;
import qunar.tc.qconfig.server.bean.LogEntry;
import qunar.tc.qconfig.server.dao.ConfigDao;
import qunar.tc.qconfig.server.dao.ConfigLogDao;
import qunar.tc.qconfig.server.dao.ConfigUsedLogDao;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author zhenyu.nie created on 2014 2014/6/12 15:00
 */
@Service
public class LogServiceImpl implements LogService, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);

    private static final String KEY_LOG_BATCH_SAVE_SIZE = "log.batch.save.size";

    private static final String KEY_LOG_QUEUE_CAPACITY = "log.queue.capacity";

    private static final String KEY_LOG_SWITCH = "log.switch.open";

    private static final String CONFIG_FILE = "config.properties";

    private static volatile LinkedBlockingQueue<LogEntry> selectBasedVersionQueue;

    private static volatile LinkedBlockingQueue<LogEntry> configLogSaveQueue;

    private static volatile LinkedBlockingQueue<LogEntry> configUsedLogSaveQueue;

    private static final String KEY_LOG_TASK_THREAD_POOL_SIZE = "log.task.thread.pool.size";

    private static ExecutorService doLogExecutor;

    @Resource
    private ConfigLogDao configLogDao;

    @Resource
    private ConfigUsedLogDao configUsedLogDao;

    @Resource
    private ConfigDao configDao;

    @QMapConfig(value = CONFIG_FILE, key = KEY_LOG_QUEUE_CAPACITY, defaultValue = "20000")
    private int logQueueCapacity;

    @QMapConfig(value = CONFIG_FILE, key = KEY_LOG_BATCH_SAVE_SIZE, defaultValue = "100")
    private int logBatchSaveSize;

    @QMapConfig(value = CONFIG_FILE, key = KEY_LOG_TASK_THREAD_POOL_SIZE, defaultValue = "5")
    private int logTaskThreadPoolSize;

    @QMapConfig(value = CONFIG_FILE, key = KEY_LOG_SWITCH, defaultValue = "true")
    private boolean logSwitch;

    private LinkedBlockingQueue<LogEntry>[] logEntryQueueArray;

    private Function<List<LogEntry>, Void>[] logEntryFunctionArray;

    @Override
    public void afterPropertiesSet() {
        initQueue();
        initThreadPool();
    }

    private void initQueue() {
        selectBasedVersionQueue = new LinkedBlockingQueue<>(logQueueCapacity);
        configLogSaveQueue = new LinkedBlockingQueue<>(logQueueCapacity);
        configUsedLogSaveQueue = new LinkedBlockingQueue<>(logQueueCapacity);

        logEntryQueueArray = new LinkedBlockingQueue[]{selectBasedVersionQueue, configLogSaveQueue, configUsedLogSaveQueue};
        logEntryFunctionArray = new Function[]{
                new SelectBasedVersionFunction(configDao),
                new ConfigLogSaveFunction(configLogDao),
                new ConfigUsedLogSaveFunction(configUsedLogDao)
        };
    }

    private void initThreadPool() {
        if (logTaskThreadPoolSize < logEntryQueueArray.length) {
            logTaskThreadPoolSize = logEntryQueueArray.length;
        }
        doLogExecutor = Executors.newFixedThreadPool(logTaskThreadPoolSize, new NamedThreadFactory("log-task-thread", false));
        for (int i = 0; i < logTaskThreadPoolSize; i++) {
            final int tmpIndex = i % logEntryQueueArray.length;
            doLogExecutor.submit(new LogTask(logEntryQueueArray[tmpIndex], logEntryFunctionArray[tmpIndex]));
        }
        logger.info("log task thread pool has started!");
    }

    private class SelectBasedVersionFunction implements Function<List<LogEntry>, Void> {

        private ConfigDao configDao;

        public SelectBasedVersionFunction(ConfigDao configDao) {
            this.configDao = configDao;
        }

        @Override
        public Void apply(List<LogEntry> logEntries) {
            for (LogEntry logEntry : logEntries) {
                Long basedVersion = configDao.selectBasedVersion(VersionData.of(logEntry.getLog().getVersion(), logEntry.getRealMeta()));
                if (basedVersion != null) {
                    logEntry.setBasedVersion(basedVersion);
                }
                doLog(logEntry);
            }
            return null;
        }
    }

    private static class ConfigLogSaveFunction implements Function<List<LogEntry>, Void> {

        private ConfigLogDao configLogDao;

        public ConfigLogSaveFunction(ConfigLogDao configLogDao) {
            this.configLogDao = configLogDao;
        }

        @Override
        public Void apply(List<LogEntry> input) {
            configLogDao.batchSave(input);
            return null;
        }
    }

    private static class ConfigUsedLogSaveFunction implements Function<List<LogEntry>, Void> {
        private ConfigUsedLogDao configUsedLogDao;

        public ConfigUsedLogSaveFunction(ConfigUsedLogDao configUsedLogDao) {
            this.configUsedLogDao = configUsedLogDao;
        }

        @Override
        public Void apply(List<LogEntry> input) {
            configUsedLogDao.batchSave(input);
            return null;
        }
    }

    private class LogTask implements Runnable {

        private LinkedBlockingQueue<LogEntry> logEntryQueue;

        private Function<List<LogEntry>, Void> function;

        public LogTask(LinkedBlockingQueue<LogEntry> logEntryQueue, Function<List<LogEntry>, Void> function) {
            this.logEntryQueue = logEntryQueue;
            this.function = function;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    function.apply(getLogEntryList());
                } catch (Throwable e) {
                    logger.error("log task执行失败！", e);
                }
            }
        }

        private List<LogEntry> getLogEntryList() {
            List<LogEntry> configLogEntryList = Lists.newLinkedList();
            LogEntry configLogEntry = null;
            try {
                configLogEntry = logEntryQueue.take();
            } catch (InterruptedException e) {
                logger.error("take from queue was interrupted", e);
            }
            configLogEntryList.add(configLogEntry);
            logEntryQueue.drainTo(configLogEntryList, logBatchSaveSize);
            return configLogEntryList;
        }
    }

    @PreDestroy
    private void destory() {
        if (doLogExecutor != null) {
            doLogExecutor.shutdown();
            logger.info("log task thread pool is shutdown!");
        }
    }

    private void doLog(LogEntry logEntry) {
        // 拉取不存在的版本的时候不记录
        if (logEntry.getBasedVersion() >= 0) {
            try {
                configLogSaveQueue.add(logEntry);
            } catch (IllegalStateException e) {
                logger.warn("config log queue is full!");
            }
        } else if (logEntry.getLog().getType() != ConfigLogType.USE_OVERRIDE) {
            logger.warn("client pull unknown version, {}, real meta {}", logEntry.getLog(), logEntry.getRealMeta());
        }

        // 如果是引用的来源并且引用已经不存在
        if (logEntry.getSourceMeta() == null) {
            return;
        }

        saveConfigUsedLog(logEntry);

    }

    private void saveConfigUsedLog(LogEntry logEntry) {
        Log log = logEntry.getLog();
        ConfigMeta sourceMeta = logEntry.getSourceMeta();
        ConfigMeta realMeta = logEntry.getRealMeta();
        switch (log.getType()) {
            case PULL_SUCCESS:
                break;
            case PULL_ERROR:
                configUsedLogDao.updateRemarks(realMeta, sourceMeta, log.getProfile(), log.getIp(), log.getPort(), 0,
                        ConfigUsedType.NO_USE, String.format(PULL_ERROR_TEMPLATE, log.getVersion(), log.getText()));
                break;
            case PARSE_REMOTE_ERROR:
                configUsedLogDao.updateRemarks(realMeta, sourceMeta, log.getProfile(), log.getIp(), log.getPort(), 0,
                        ConfigUsedType.NO_USE, String.format(PARSE_REMOTE_ERROR_TEMPLATE, log.getVersion()));
                break;
            case USE_OVERRIDE:
                configUsedLogDao.update(realMeta, sourceMeta, log.getProfile(), log.getIp(), log.getPort(), 0,
                        ConfigUsedType.USE_OVERRIDE, Strings.isNullOrEmpty(log.getText()) ? "使用本地覆盖文件" : log.getText());
                if (log.getPort() != 0) {
                    removeExpiredData(sourceMeta, log.getIp());
                }
                break;
            case USE_REMOTE_FILE:
                configUsedLogDao.update(realMeta, sourceMeta, log.getProfile(), log.getIp(), log.getPort(),
                        fixVersion(log.getVersion()), ConfigUsedType.USE_REMOTE, Strings.isNullOrEmpty(log.getText()) ? "使用远程文件" : log.getText());
                if (log.getPort() != 0) {
                    removeExpiredData(sourceMeta, log.getIp());
                }
                break;
            default:
                throw new IllegalArgumentException("illegal type: " + log.getType());
        }
    }

    private void removeExpiredData(ConfigMeta sourceMeta, String ip) {
        configUsedLogDao.delete(sourceMeta, ip, 0);
    }

    @Override
    public void log(Log log, ConfigMeta sourceMeta, ConfigMeta realMeta) {
        if (!logSwitch) {
            return;
        }

        Long basedVersion = -1L;
        LogEntry logEntry = new LogEntry(log, sourceMeta, realMeta, basedVersion);
        logEntry.setConfigLogType(log.getType());

        if (log.getVersion() > 0) {
            try {
                selectBasedVersionQueue.add(logEntry);
                return;
            } catch (IllegalStateException e) {
                logger.warn("select config queue is full");
            }
        } else {
            doLog(logEntry);
        }
    }

    private static final String PULL_ERROR_TEMPLATE = "拉取版本%s失败:%s";

    private static final String PARSE_REMOTE_ERROR_TEMPLATE = "解析版本%s失败";

    private long fixVersion(long version) {
        return version < 0 ? 0 : version;
    }
}
