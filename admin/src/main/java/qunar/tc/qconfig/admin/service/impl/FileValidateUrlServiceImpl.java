package qunar.tc.qconfig.admin.service.impl;

import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.FileValidateUrlDao;
import qunar.tc.qconfig.admin.service.FileValidateUrlService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2017 2017/2/20 16:54
 */
@Service
public class FileValidateUrlServiceImpl implements FileValidateUrlService {

    @Resource
    private FileValidateUrlDao fileValidateUrlDao;

    @Resource
    private UserContextService userContextService;

    @Override
    public void setUrl(ConfigMeta meta, String url) {
        fileValidateUrlDao.update(meta, Strings.nullToEmpty(url), userContextService.getRtxId());
    }

    @Override
    public String getUrl(ConfigMeta meta) {
        return Strings.nullToEmpty(fileValidateUrlDao.select(meta));
    }
}
