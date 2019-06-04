package qunar.tc.qconfig.admin.service.impl;

import com.google.common.eventbus.EventBus;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.ProfileDao;
import qunar.tc.qconfig.admin.event.ProfileCreatedBean;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.service.ProfileService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.security.ApplicationManager;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/5/28 22:57
 */
@Service("profileService")
public class ProfileServiceImpl implements ProfileService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ProfileDao profileDao;

    @Resource
    private UserContextService userContext;

    @Resource
    private EventBus eventBus;

    @Override
    public void create(String group, String profile) throws ModifiedException {
        try {
            profileDao.create(group, profile, userContext.getRtxId());
            logger.info("create profile successOf, group=[{}], profile[{}]", group, profile);
            eventBus.post(new ProfileCreatedBean(group, profile, userContext.getRtxId(),
                    new Timestamp(System.currentTimeMillis())));
        } catch (DuplicateKeyException e) {
            // ignore
        }
    }

    @Override
    public void batchCreate(String group, Set<String> profile) {

    }

    @Override
    public List<String> find(String group) {
        return profileDao.selectProfiles(group);
    }

    @Override
    public List<Map.Entry<String, String>> find(Collection<String> group) {
        return profileDao.selectProfiles(group);
    }

    @Override
    public boolean exist(String group, String profile) {
        return profileDao.exist(group, profile);
    }

    @Override
    public void delete(String group, String profile) {
        profileDao.completeDelete(group, profile);
    }

}
