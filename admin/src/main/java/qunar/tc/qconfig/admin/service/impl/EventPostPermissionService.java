package qunar.tc.qconfig.admin.service.impl;

import com.google.common.eventbus.EventBus;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.event.PermissionNotifyBean;
import qunar.tc.qconfig.admin.model.FilePermissionInfo;
import qunar.tc.qconfig.admin.model.PermissionInfo;
import qunar.tc.qconfig.admin.service.PermissionService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.web.security.PermissionType;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/27 14:38
 */
@Service("eventPostPermissionService")
public class EventPostPermissionService implements PermissionService {

    @Resource(name = "acturalPermissionService")
    private PermissionService permissionService;

    @Resource
    private EventBus eventBus;

    @Resource
    private UserContextService userContext;

    @Override
    public List<PermissionInfo> getPermissionListByGroup(String group) {
        return permissionService.getPermissionListByGroup(group);
    }

    @Override
    public List<FilePermissionInfo> getFilePermissionListByGroupAndDataId(String group, String dataId) {
        return permissionService.getFilePermissionListByGroupAndDataId(group, dataId);
    }

    @Override
    public List<String> setPermissionList(String group, List<PermissionInfo> permissionInfo) {
        List<String> remarks = permissionService.setPermissionList(group, permissionInfo);

        if (!remarks.isEmpty()) {
            eventBus.post(new PermissionNotifyBean(group, userContext.getRtxId(), remarks,
                    new Timestamp(System.currentTimeMillis())));
        }

        return remarks;
    }

    @Override
    public List<PermissionInfo> getPermissionListByRtxId(String rtxId) {
        return permissionService.getPermissionListByRtxId(rtxId);
    }

    @Override
    public List<FilePermissionInfo> getFilePermissionListByRtxId(String rtxId) {
        return permissionService.getFilePermissionListByRtxId(rtxId);
    }

    @Override
    public List<FilePermissionInfo> getFilePermissionListByGroupAndRtxId(String group, String rtxId) {
        return permissionService.getFilePermissionListByGroupAndRtxId(group, rtxId);
    }

    @Override
    public boolean hasFilePermission(String group, String profile, String dataId, PermissionType permissionType) {
        return permissionService.hasFilePermission(group, profile, dataId, permissionType);
    }

    @Override
    public boolean hasPermission(String group, String profile, PermissionType permissionType) {
        return permissionService.hasPermission(group, profile, permissionType);
    }

    @Override
    public List<String> setPermissionList(String group, String dataId, List<FilePermissionInfo> permissionInfos) {
        List<String> remarks = permissionService.setPermissionList(group, dataId, permissionInfos);

        if (!remarks.isEmpty()) {
            eventBus.post(new PermissionNotifyBean(group, userContext.getRtxId(), remarks,
                    new Timestamp(System.currentTimeMillis())));
        }

        return remarks;
    }

    @Override
    public boolean deleteFilePermission(String group, String dataId, String rtxId) {
        boolean result = permissionService.deleteFilePermission(group, dataId, rtxId);
        if (result) {
            eventBus.post(new PermissionNotifyBean(group, userContext.getRtxId(),
                    Collections.singletonList("删除" + rtxId + "对文件" + dataId + "的权限"),
                    new Timestamp(System.currentTimeMillis())));
        }
        return result;
    }
}
