package qunar.tc.qconfig.admin.service.impl;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.cloud.enums.UserFavoriteType;
import qunar.tc.qconfig.admin.dao.ConfigOpLogDao;
import qunar.tc.qconfig.admin.dao.UserFavoritesDao;
import qunar.tc.qconfig.admin.model.ConfigOpLog;
import qunar.tc.qconfig.admin.model.PublishedConfigInfo;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.service.UserBehaviorService;
import qunar.tc.qconfig.common.bean.PaginationResult;
import qunar.tc.qconfig.common.support.Application;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserBehaviorServiceImpl implements UserBehaviorService {

    private final Logger logger = LoggerFactory.getLogger(UserBehaviorServiceImpl.class);

    @Resource
    private UserFavoritesDao userFavoritesDao;

    @Resource
    private ConfigService configService;

    @Resource
    private ConfigOpLogDao configOpLogDao;

    @Resource
    private UserContextService userContext;

    public PaginationResult<PublishedConfigInfo> listFavoriteFileInfo(String user, int page, int pageSize) {
        List<ConfigMeta> configMetas = userFavoritesDao.listFavoriteFiles(user, page, pageSize);
        int totalCount = userFavoritesDao.countFavoriteItems(user, UserFavoriteType.FILE);
        List<PublishedConfigInfo> userFavoriteConfigInfo = Lists.newArrayListWithCapacity(configMetas.size());
        for (ConfigMeta meta : configMetas) {
            PublishedConfigInfo configInfo = configService.getConfigInfo(meta.getGroup(), meta.getProfile(), meta.getDataId());
            if (configInfo != null) {
                userFavoriteConfigInfo.add(configInfo);
                Application app = userContext.getApplication(meta.getGroup());
                if (app != null) {
                    configInfo.setGroupName(app.getName());
                }
                configInfo.setFavoriteFile(true);
            }
        }
        PaginationResult<PublishedConfigInfo> result = new PaginationResult<>();
        result.setData(userFavoriteConfigInfo);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setTotal(totalCount);
        result.setTotalPage((totalCount - 1) / pageSize + 1);
        return result;
    }

    public List<String> listFavoriteGroups(String user, int page, int pageSize) {
        return userFavoritesDao.listFavoriteGroups(user, page, pageSize);
    }

    public boolean isFavoriteGroup(String group, String user) {
        return userFavoritesDao.isFavoriteGroup(group, user);
    }

    public boolean isFavoriteFile(ConfigMeta meta, String user) {
        return userFavoritesDao.isFavoriteFile(meta, user);
    }

    public void insertFavoriteGroup(String group, String user) {
        logger.info("insert user favorite group, group:[{}], user:[{}]", group, user);
        try {
            userFavoritesDao.insertFavoriteGroup(group, user);
        } catch (Exception e) {
            logger.error("insert user favorite group failed, group:[{}], user:[{}]", group, user);
            throw new RuntimeException("insert favorite group failed");
        }
    }

    public void insertFavoriteFile(ConfigMeta meta, String user) {
        logger.info("insert user favorite file, configMeta:[{}], user:[{}]", meta, user);
        try {
            userFavoritesDao.insertFavoriteFile(meta, user);
        } catch (Exception e) {
            logger.error("insert user favorite file failed, configMeta:[{}], user:[{}]", meta, user);
            throw new RuntimeException("insert user favorite file failed");
        }
    }

    public void deleteFavoriteGroup(String group, String user) {
        logger.info("delete user favorite group, group:[{}], user:[{}]", group, user);
        try {
            userFavoritesDao.deleteFavoriteGroup(group, user);
        } catch (Exception e) {
            logger.error("delete user favorite group failed, group:[{}], user:[{}]", group, user);
            throw new RuntimeException("delete user favorite group failed");
        }
    }

    public void deleteFavoriteFile(ConfigMeta meta, String user) {
        logger.info("delete user favorite file, configMeta:[{}], user:[{}]", meta, user);
        try {
            userFavoritesDao.deleteFavoriteFile(meta, user);
        } catch (Exception e) {
            logger.error("delete user favorite file failed, configMeta:[{}], user:[{}]", meta, user);
            throw new RuntimeException("delete user favorite file failed");
        }
    }

    @Override
    public void deleteFavorites(ConfigMeta meta) {
        logger.info("delete favorites related to configMeta:{}", meta);
        try {
            userFavoritesDao.deleteFavorites(meta);
        } catch (Exception e) {
            logger.error("delete favorites related to configMeta:[{}] failed!", meta);
            throw new RuntimeException("delete favorites failed");
        }
    }

    @Override
    public List<PublishedConfigInfo> listUserLastModifiedFile(String user, int size) {
        int offset = 0;
        int metaSize = 0;
        List<ConfigMeta> allConfigMetas = new ArrayList<>();
        final Set<ConfigMeta> distinctMetaSet = new HashSet<>();
        final int limit = size * 4; //每批查询数，ConfigMeta有重复的，多查一些再过滤。
        while(metaSize < size) {
            List<ConfigOpLog> configOpLogs = configOpLogDao.selectRecent(user, offset, limit);
            for (ConfigOpLog configOpLog : configOpLogs) {
                ConfigMeta meta = new ConfigMeta(configOpLog.getGroup(), configOpLog.getDataId(), configOpLog.getProfile());
                if (distinctMetaSet.add(meta)) {
                    allConfigMetas.add(meta);
                    metaSize++;
                }
            }
            offset += limit;
            if (configOpLogs.size() < limit) {
                break;
            }
        }
        allConfigMetas = metaSize > size ? allConfigMetas.subList(0, size) : allConfigMetas;
        List<PublishedConfigInfo> configInfoList = configService.getConfigInfo(allConfigMetas);
        for (PublishedConfigInfo configInfo : configInfoList) {
            Application app = userContext.getApplication(configInfo.getConfigMeta().getGroup());
            if (app != null) {
                configInfo.setGroupName(app.getName());
            }
            configInfo.setFavoriteFile(isFavoriteFile(configInfo.getConfigMeta(), userContext.getRtxId()));
        }
        return configInfoList;
    }
}
