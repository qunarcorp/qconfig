package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.QConfigFileType;

import java.util.List;

/**
 * @author yunfeng.yang
 * @since 2017/4/25
 */
public interface QConfigFileTypeService {
    List<QConfigFileType> findAllQConfigFileTypes();
}
