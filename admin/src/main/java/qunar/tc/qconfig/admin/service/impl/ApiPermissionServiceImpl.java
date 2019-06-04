package qunar.tc.qconfig.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.impl.ApiGroupIdPermissionRelDaoImpl;
import qunar.tc.qconfig.admin.dao.impl.ApiPermissionDaoImpl;
import qunar.tc.qconfig.admin.model.ApiPermission;
import qunar.tc.qconfig.admin.model.Permission;
import qunar.tc.qconfig.admin.model.PermissionType;

import java.util.List;

/**
 * Created by chenjk on 2018/1/14.
 */
@Service
public class ApiPermissionServiceImpl {

    @Autowired
    private ApiPermissionDaoImpl apiPermissionDao;

    @Autowired
    private ApiGroupIdPermissionRelDaoImpl apiGroupIdPermissionRelDao;

    public void initApiPermission() {
        ApiPermission apiPermission = new ApiPermission();
        apiPermission.setUrl("");
        apiPermission.setMethod("");
        apiPermission.setType(PermissionType.FOLDER.getCode());
        apiPermission.setParentid(-1l);
        apiPermission.setDescription("根目录");
        apiPermissionDao.save(apiPermission);
    }

    public void delete(Long id) {
        apiPermissionDao.delete(id);
    }

    public void save(ApiPermission apiPermission) {
        apiPermissionDao.save(apiPermission);
    }

    public List<ApiPermission> queryAll() {
        return apiPermissionDao.queryAll();
    }

    public List<ApiPermission> queryByGroupIdAndTargetGroupId(String groupId, String targetGroupId) {
        return apiGroupIdPermissionRelDao.queryByGroupIdAndTargetGroupId(groupId, targetGroupId);
    }
}
