package qunar.tc.qconfig.server.task;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.application.ServerManager;
import qunar.tc.qconfig.common.bean.AppServerConfig;
import qunar.tc.qconfig.common.support.concurrent.NamedThreadFactory;
import qunar.tc.qconfig.server.dao.FileConfigDao;
import qunar.tc.qconfig.server.dao.FileDeleteDao;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2017 2017/5/15 17:00
 */
@Service
public class FileDeleteTask {

    private static final Logger logger = LoggerFactory.getLogger(FileDeleteTask.class);

    @Resource
    private FileDeleteDao deleteDao;

    @Resource
    private FileConfigDao fileConfigDao;

    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("file-delete-task"));

    private final String ip;

    public FileDeleteTask() {
        AppServerConfig appConfig = ServerManager.getInstance().getAppServerConfig();
        if (appConfig == null ||  Strings.isNullOrEmpty(appConfig.getIp())) {
            logger.error("无法连接应用中心");
            throw new RuntimeException("无法连接应用中心");
        }
        ip = appConfig.getIp();
    }

    @PostConstruct
    public void init() {
        scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                logger.debug("start file delete");
                deleteFiles();
                logger.debug("end file delete");
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    void deleteFiles() {
        try {
            List<ConfigMeta> metas = deleteDao.select(ip);
            if (!metas.isEmpty()) {
                for (ConfigMeta meta : metas) {
                    deleteFile(meta);
                }
            }
        } catch (Exception e) {
            logger.error("select file_delete table exception, {}", e);
        }
    }

    private void deleteFile(ConfigMeta meta) {
        logger.info("file delete, {}", meta);
        try {
            boolean delete = fileConfigDao.delete(meta);
            if (delete) {
                deleteDao.delete(meta, ip);
            }
        } catch (Exception e) {
            logger.error("delete file error, {}", meta, e);
        }
    }
}
