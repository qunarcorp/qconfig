package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qunar.tc.qconfig.admin.dao.ApplicationDao;
import qunar.tc.qconfig.admin.dao.ApplicationUserDao;
import qunar.tc.qconfig.admin.model.AccessRoleType;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.ApplicationInfoService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.common.support.Application;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2014 2014/5/27 11:13
 */
@Service
public class ApplicationInfoServiceImpl implements ApplicationInfoService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    ApplicationDao applicationDao;

    @Resource
    ApplicationUserDao applicationUserDao;

    @Resource
    UserContextService userContextService;

    private CacheLoader<String, Application> cacheLoader = CacheLoader.from(new Function<String, Application>() {
        @Override
        public Application apply(String input) {
            Application noUserApplication = applicationDao.getApplicationByAppCode(input);
            List<String> dev = applicationUserDao.getUserByAppCodeAndRole(input, AccessRoleType.DEV);
            List<String> owner = applicationUserDao.getUserByAppCodeAndRole(input, AccessRoleType.OWNER);
            noUserApplication.setDeveloper(dev);
            noUserApplication.setOwner(owner);
            return noUserApplication;
        }
    });

    private LoadingCache<String, Application> cache = CacheBuilder.newBuilder().maximumSize(50).build(cacheLoader);

    @Override
    public boolean checkExist(String appCode) {
        return applicationDao.checkExist(appCode);
    }

    @Override
    @Transactional
    public int createApplication(Application application) {
        if (application.getDeveloper() != null && application.getDeveloper().size() > 0) {
            applicationUserDao.batchAdd(application.getDeveloper(), application.getCode(), AccessRoleType.DEV);
        }
        if (application.getOwner() != null && application.getOwner().size() > 0) {
            applicationUserDao.batchAdd(application.getOwner(), application.getCode(), AccessRoleType.OWNER);
        }
        application.setCreator(userContextService.getRtxId());
        return applicationDao.createApplication(application);
    }

    @Override
    @Transactional
    public int updateApplication(Application application) {
        applicationUserDao.batchAdd(application.getDeveloper(), application.getCode(), AccessRoleType.DEV);
        applicationUserDao.batchAdd(application.getOwner(), application.getCode(), AccessRoleType.OWNER);
        return applicationDao.updateApplicationMail(application);
    }

    @Override
    public Application getGroupInfo(String group) {
        if (Strings.isNullOrEmpty(group)) {
            return null;
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return cache.get(group);
        } catch (RuntimeException e) {
            logger.error("can not get info from app center", e);
            Monitor.getAppInfoFailCounter.inc();
            throw e;
        } catch (ExecutionException e) {
            logger.error("can not get info from app center", e);
            Monitor.getAppInfoFailCounter.inc();
            throw new RuntimeException(e);
        } finally {
            Monitor.getAppInfoTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public List<Application> getGroupInfos(String rtxId) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            List<String> appCodes = applicationUserDao.getAppCodeByRTX(rtxId);

            return dealApplication(appCodes);
        } catch (RuntimeException e) {
            logger.error("can not get info from app center", e);
            Monitor.getAppInfoFailCounter.inc();
            throw e;
        } catch (ExecutionException e) {
            logger.error("can not get info from app center", e);
            Monitor.getAppInfoFailCounter.inc();
            throw new RuntimeException(e);
        } finally {
            Monitor.getAppInfoTimer.update(stopwatch.elapsed().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private List<Application> dealApplication(List<String> appCodes) throws ExecutionException {
        List<Application> applications = Lists.newArrayListWithCapacity(appCodes.size());
        for (String code : appCodes) {
            applications.add(cache.get(code));
        }
        return applications;
    }
}
