package qunar.tc.qconfig.server.config;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 14:06
 */
public interface ConfigTypeService {

    boolean isPublicConfig(String group, String dataId);

    boolean isInheritConfig(String group, String dataId);

    boolean isRest(String group, String dataId);
}
