package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.EncryptKeyDao;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.admin.model.EncryptKey;
import qunar.tc.qconfig.admin.model.EncryptKeyStatus;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.EncryptKeyBlacklistService;
import qunar.tc.qconfig.admin.service.EncryptKeyService;
import qunar.tc.qconfig.client.MapConfig;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhenyu.nie created on 2014 2014/12/30 18:14
 */
@Service
public class EncryptKeyServiceImpl implements EncryptKeyService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptKeyServiceImpl.class);

    @Resource
    private ConfigService configService;

    @Resource
    private EncryptKeyBlacklistService encryptKeyBlacklistService;

    @Resource
    private EncryptKeyDao encryptKeyDao;

    /**
     * 判断一个key是不是需要加密，判断加密的原则如下：
     * 1. 如果数据库中有key相关的加密配置数据，则以数据库为准
     * 2. 如果数据库中不存在key对应的配置数据，则以是否在黑名单中为准
     *
     * @param encryptKeys 数据库中保存的key和加密状态的设置列表
     * @param key         需要判断的key
     * @return 是否需要加密
     */
    @Override
    public boolean isEncryptedKey(final List<EncryptKey> encryptKeys, final String key) {
        for (EncryptKey encryptKey : encryptKeys) {
            // 如果数据库中存在设置，则以数据库中的设置数据为准
            if (Objects.equal(encryptKey.getKey(), key)) {
                return encryptKey.getStatus() == EncryptKeyStatus.ENCRYPTED;
            }
        }

        // 数据库中无设置数据，则以配置的黑名单为准
        return encryptKeyBlacklistService.inBlacklist(key);
    }

    @Override
    public Map<String, Boolean> getEditableEncryptKeys(String group, String dataId, String profile, int editVersion) {
        CandidateSnapshot details = configService.getCandidateDetails(group, dataId, profile, editVersion);
        String data;
        if (details == null) {
            data = "";
        } else {
            data = Strings.nullToEmpty(details.getData());
        }

        Set<String> keys = getKeys(data);
        List<EncryptKey> encryptKeys = encryptKeyDao.select(group, dataId);
        Map<String, Boolean> result = Maps.newHashMap();
        for (String key : keys) {
            result.put(key, isEncryptedKey(encryptKeys, key));
        }
        return result;
    }

    @Override
    public void insertOrUpdate(String group, String dataId, String operator, List<EncryptKey> keys) {
        final List<EncryptKey> encryptKeys = encryptKeyDao.select(group, dataId);
        encryptKeyDao.insertOrUpdate(group, dataId, operator, statusChangedEncryptKeys(encryptKeys, keys));
    }

    private Set<String> getKeys(String data) {
        try {
            return MapConfig.parseProperties(data, false).keySet();
        } catch (IOException e) {
            logger.error("parse property file error, data: {}", data);
            return Sets.newHashSet();
        }
    }

    /**
     * 将加密状态未改变的EncryptKey剔除
     *
     * @param originEncryptKeys  数据库中保存的EncryptKey
     * @param currentEncryptKeys 用户提交的EncryptKey
     * @return 剔除加密状态未改变的EncryptKey之后的currentEncryptKeys列表
     */
    private List<EncryptKey> statusChangedEncryptKeys(final List<EncryptKey> originEncryptKeys,
                                                      final List<EncryptKey> currentEncryptKeys) {
        return Lists.newArrayList(
                currentEncryptKeys.stream().filter(input -> {
                    final boolean isCurrentKeyEncrypted = input.getStatus() == EncryptKeyStatus.ENCRYPTED;
                    return isCurrentKeyEncrypted != isEncryptedKey(originEncryptKeys, input.getKey());
                }).collect(Collectors.toList()));
    }
}
