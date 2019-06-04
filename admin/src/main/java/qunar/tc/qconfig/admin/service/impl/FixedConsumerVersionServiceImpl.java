package qunar.tc.qconfig.admin.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qunar.tc.qconfig.admin.dao.ConfigDao;
import qunar.tc.qconfig.admin.dao.FixedConsumerVersionDao;
import qunar.tc.qconfig.admin.dao.impl.InheritConfigDaoImpl;
import qunar.tc.qconfig.admin.dto.ConsumerVersionDto;
import qunar.tc.qconfig.admin.model.ConfigInfoWithoutPublicStatus;
import qunar.tc.qconfig.admin.model.ConfigUsedLog;
import qunar.tc.qconfig.admin.service.FixedConsumerVersionService;
import qunar.tc.qconfig.admin.service.ListeningClientsService;
import qunar.tc.qconfig.admin.service.NotifyService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author yunfeng.yang
 * @since 2017/5/16
 */
@Service
public class FixedConsumerVersionServiceImpl implements FixedConsumerVersionService {

    private final static long VERSION_NOT_FIXED = -1L;

    @Resource
    private FixedConsumerVersionDao fixedConsumerVersionDao;

    @Resource
    private UserContextService userContextService;

    @Resource
    private InheritConfigDaoImpl inheritConfigDao;

    @Resource
    private ConfigDao configDao;

    @Resource
    private ListeningClientsService listeningClientsService;

    @Resource
    private NotifyService notifyService;

    @Override
    public Map<String, Long> findIpAndVersions(ConfigMeta meta) {
        List<ConsumerVersionDto> consumerVersionDtos = fixedConsumerVersionDao.find(meta);
        Map<String, Long> ipAndVersions = Maps.newHashMap();
        for (ConsumerVersionDto consumerVersionDto : consumerVersionDtos) {
            ipAndVersions.put(consumerVersionDto.getIp(), consumerVersionDto.getVersion());
        }
        return ipAndVersions;
    }

    @Override
    @Transactional
    public void fixConsumerVersion(ConfigMeta configMeta, String ip, long version) throws InterruptedException, ExecutionException, TimeoutException {
        String rtxId = userContextService.getRtxId();

        Optional<ClientData> clientData = listeningClientsService.getListeningClientsData(configMeta, ip);
        if (clientData.isPresent()) {
            if (clientData.get().getVersion() > version) {
                throw new RuntimeException("当前机器版本大于锁定版本,version=" + clientData.get().getVersion());
            }
        } else {
            throw new RuntimeException("当前客户端已经下线");
        }

        addFixedVersionAndNotify(configMeta, ip, version, rtxId);
        ConfigMeta parentMeta = inheritConfigDao.findReference(configMeta, RefType.INHERIT.value());

        if (parentMeta != null) {//同时将父版本也锁定
            ConfigInfoWithoutPublicStatus configInfoWithoutPublicStatus = configDao.findCurrentConfigInfo(parentMeta);
            addFixedVersionAndNotify(parentMeta, ip, configInfoWithoutPublicStatus.getVersion(), rtxId);
        }

        ConfigMeta childMetaInUsed = inheritConfigDao.findChildMetaInUsed(configMeta, ip);
        if (childMetaInUsed != null) {//如果是父版本锁定，同时锁定子版本
            ConfigInfoWithoutPublicStatus configInfoWithoutPublicStatus = configDao.findCurrentConfigInfo(childMetaInUsed);
            addFixedVersionAndNotify(childMetaInUsed, ip, configInfoWithoutPublicStatus.getVersion(), rtxId);
        }
    }

    @Override
    public void deleteConsumerVersion(ConfigMeta configMeta, String ip) {
        deleteFixedVersionAndNotify(configMeta, ip);
        ConfigMeta parentMeta = inheritConfigDao.findReference(configMeta, RefType.INHERIT.value());

        if (parentMeta != null) {//同时将父版本也解锁
            configDao.findCurrentConfigInfo(parentMeta);
            deleteFixedVersionAndNotify(parentMeta, ip);
        }

        ConfigMeta childMetaInUsed = inheritConfigDao.findChildMetaInUsed(configMeta, ip);
        if (childMetaInUsed != null) {//如果是父版本解锁，同时解锁子版本
            configDao.findCurrentConfigInfo(childMetaInUsed);
            deleteFixedVersionAndNotify(childMetaInUsed, ip);
        }
    }

    @Override
    public List<ConfigUsedLog> addFixedVersion(ConfigMeta configMeta, List<ConfigUsedLog> configUsedLogs) {
        if (CollectionUtils.isEmpty(configUsedLogs)) {
            return configUsedLogs;
        }
        Map<String, Long> ipAndVersions = findIpAndVersions(configMeta);
        List<ConfigUsedLog> result = Lists.newArrayListWithCapacity(configUsedLogs.size());

        for (ConfigUsedLog configUsedLog : configUsedLogs) {
            String consumerIp = configUsedLog.getIp();
            Long version = ipAndVersions.get(consumerIp);
            if (version != null) {
                ConfigUsedLog newConfigUsedLog = new ConfigUsedLog(configUsedLog.getGroup(), configUsedLog.getDataId(), configUsedLog.getProfile(),
                        configUsedLog.getSourceGroupId(), configUsedLog.getSourceDataId(), configUsedLog.getSourceProfile(), configUsedLog.getIp(),
                        configUsedLog.getPort(), Ints.checkedCast(version), configUsedLog.getType(), configUsedLog.getConsumerProfile(), configUsedLog.getRemarks(),
                        configUsedLog.getUpdateTime());
                newConfigUsedLog.setFixedVersion(true);
                result.add(newConfigUsedLog);
            } else {
                result.add(configUsedLog);
            }
        }
        return result;
    }

    private void addFixedVersionAndNotify(ConfigMeta meta, String ip, long version, String operator) {
        fixedConsumerVersionDao.add(meta, ip, version, operator);
        notifyService.notifyFixedVersion(meta, ip, version);
    }

    private void deleteFixedVersionAndNotify(ConfigMeta meta, String ip) {
        fixedConsumerVersionDao.delete(meta, ip);
        notifyService.notifyFixedVersion(meta, ip, VERSION_NOT_FIXED);
    }
}
