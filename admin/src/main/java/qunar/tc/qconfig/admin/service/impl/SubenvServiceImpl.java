package qunar.tc.qconfig.admin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.service.SubenvService;
import qunar.tc.qconfig.client.spring.QConfig;
import qunar.tc.qconfig.common.application.ServerManagement;
import qunar.tc.qconfig.common.application.ServiceFinder;
import qunar.tc.qconfig.common.util.Environment;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenjk on 2018/6/8.
 */
@Service
public class SubenvServiceImpl implements SubenvService {

    private Map<String, String> getQunarGroupInfoMapByAppid(String appid) {
        return ImmutableMap.of();
    }

    public Map<String, String> getGroupInfoMapByAppIdAndEnv(String appId, String env) {
        if (!Environment.fromEnvName(env).isProd()) {
            return ImmutableMap.of();
        }
        return getQunarGroupInfoMapByAppid(appId);

    }
}
