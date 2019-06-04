package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

/**
 * @author zhenyu.nie created on 2017 2017/2/20 16:54
 */
public interface FileValidateUrlService {

    void setUrl(ConfigMeta meta, String url);

    String getUrl(ConfigMeta meta);
}
