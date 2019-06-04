package qunar.tc.qconfig.servercommon.util;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/10/28 21:34
 */
public class PriorityUtil {

    private static Logger logger = LoggerFactory.getLogger(PriorityUtil.class);

    public static List<ConfigMeta> createPriorityList(ConfigMeta meta) {
        List<ConfigMeta> priorityList = Lists.newArrayList();

        // add self
        priorityList.add(meta);

        Environment env = Environment.fromProfile(meta.getProfile());
        // add like dev:
        if (!env.defaultProfile().equals(meta.getProfile())) {
            priorityList.add(new ConfigMeta(meta.getGroup(), meta.getDataId(), env.defaultProfile()));
        }

        // add resources:
        if (!env.isResources()) {
            priorityList.add(new ConfigMeta(meta.getGroup(), meta.getDataId(), Environment.RESOURCES.defaultProfile()));
        }
        return priorityList;
    }

    public static List<ConfigMeta> createPriorityListWithRoom(ConfigMeta meta, String room) {
        List<ConfigMeta> priorityList = Lists.newArrayList();
        Environment env;
        try {
            env = Environment.fromProfile(meta.getProfile());
            // add self
            priorityList.add(meta);
        } catch (IllegalArgumentException e) {
            logger.info("create subenv candidate from profile exception: {}", e);
            env = Environment.extractDefaultProfile(meta.getProfile());
        }

        // add like dev:
        if (!env.defaultProfile().equals(meta.getProfile())) {
            priorityList.add(new ConfigMeta(meta.getGroup(), meta.getDataId(), env.defaultProfile()));
        }

        // add resources:
        if (!env.isResources()) {
            priorityList.add(new ConfigMeta(meta.getGroup(), meta.getDataId(), Environment.RESOURCES.defaultProfile()));
        }
        return priorityList;
    }

}
