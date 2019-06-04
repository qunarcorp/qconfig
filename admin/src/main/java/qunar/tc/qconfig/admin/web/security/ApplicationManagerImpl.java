package qunar.tc.qconfig.admin.web.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.ApplicationInfoService;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.support.Application;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by pingyang.yang on 2019-05-08
 */
@Service
public class ApplicationManagerImpl implements ApplicationManager {

    @Resource
    ApplicationInfoService applicationInfoService;

    private static final Logger logger = LoggerFactory.getLogger(ApplicationManager.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

    private static final int TIMEOUT_MS = 3000;

    @Override
    public Map<String, Map<String, Set<String>>> getAllEnvs(Set<String> appCodes) {

        return Maps.newHashMap();
    }


    public Application getAppByCode(String code) {
        return applicationInfoService.getGroupInfo(code);
    }

    public List<Application> listByLoginId(String loginId){
        return applicationInfoService.getGroupInfos(loginId);
    }

}
