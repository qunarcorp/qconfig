package qunar.tc.qconfig.admin.web.rest;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import qunar.tc.qconfig.admin.dao.impl.ApiGroupIdPermissionRelDaoImpl;
import qunar.tc.qconfig.admin.dao.impl.ApiGroupIdRelDaoImpl;
import qunar.tc.qconfig.admin.model.ApiGroupIdRel;
import qunar.tc.qconfig.admin.model.ApiPermission;
import qunar.tc.qconfig.admin.service.impl.ApiPermissionServiceImpl;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by chenjk on 2018/1/25.
 */
@Controller
@RequestMapping("/admin")
public class RestPermissionController {

    @Autowired
    private ApiGroupIdRelDaoImpl apiGroupIdRelDao;

    @Autowired
    private ApiGroupIdPermissionRelDaoImpl apiGroupIdPermissionRelDao;

    @Autowired
    private ApiPermissionServiceImpl apiPermissionServiceImpl;

    @RequestMapping("/apis")
    public String tokens() {
        return "admin/apis";
    }

    @RequestMapping(value = "/apis/permissions", method = RequestMethod.POST)
    @ResponseBody
    public Object savePermission(@ModelAttribute ApiPermission apiPermission) {
        apiPermission.setParentid(-1l);
        apiPermissionServiceImpl.save(apiPermission);
        return "ok";
    }

    @RequestMapping(value = "/apis/permissions", method = RequestMethod.GET)
    @ResponseBody
    public Object getPermissions() {
        return apiPermissionServiceImpl.queryAll();
    }

    @RequestMapping(value = "/apis/permissions/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteById(@PathVariable("id") Long id) {
        apiPermissionServiceImpl.delete(id);
        apiGroupIdPermissionRelDao.deleteByPermissionId(id);
        return "ok";
    }

    @RequestMapping(value = "/apis/{groupidRelId}/permissions/{permissionId}", method = RequestMethod.POST)
    @ResponseBody
    public Object saveApiPermissions(@PathVariable("groupidRelId") Long groupidRelId, @PathVariable("permissionId") Long permissionId) {
        List<Long> permissionList = Lists.newLinkedList();
        permissionList.add(permissionId);
        apiGroupIdPermissionRelDao.save(groupidRelId, permissionList);
        return "ok";
    }

    @RequestMapping(value = "/apis/{groupidRelId}/permissions/{permissionId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteApiPermissions(@PathVariable("groupidRelId") Long groupidRelId, @PathVariable("permissionId") Long permissionId) {
        List<Long> permissionList = Lists.newLinkedList();
        permissionList.add(permissionId);
        apiGroupIdPermissionRelDao.delete(groupidRelId, permissionList);
        return "ok";
    }

    @RequestMapping(value = "/apis/{groupid}/permissions/{targetGroupId}", method = RequestMethod.GET)
    @ResponseBody
    public Object getApisPermissions(@PathVariable("groupid") String groupid,
                                     @PathVariable("targetGroupId") String targetGroupId) {
        return apiPermissionServiceImpl.queryByGroupIdAndTargetGroupId(groupid, targetGroupId);
    }

    @RequestMapping(value = "/apis/groupidRefs", method = RequestMethod.GET)
    @ResponseBody
    public Object getGroupidRefs(@RequestParam(value = "term", required = false) String term) {
        return apiGroupIdRelDao.query(term, 0, apiGroupIdRelDao.count(term));
    }

    @RequestMapping(value = "/apis/{groupId}/{targetAppid}", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public Object addGroupidRefs(@PathVariable("groupId") String groupId, @PathVariable("targetAppid") String targetAppid) {
        apiGroupIdRelDao.save(new ApiGroupIdRel(groupId, targetAppid, RestUtil.getToken(groupId, targetAppid)));
        return "ok";
    }

    @RequestMapping(value = "/apis/{groupId}/{targetAppid}/v2", method = RequestMethod.POST)
    @ResponseBody
    public Object addGroupidRefsForNewToken(@PathVariable("groupId") String groupId, @PathVariable("targetAppid") String targetAppid) {
        apiGroupIdRelDao.save(new ApiGroupIdRel(groupId, targetAppid, RestUtil.getToken(groupId)));
        return "ok";
    }

    @RequestMapping(value = "/apis/groupidRefs/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public Object deleteGroupidRefs(@PathVariable("id") Long id) {
        apiGroupIdRelDao.delete(id);
        apiGroupIdPermissionRelDao.deleteAllGroupidRefPermissions(id);
        return "ok";
    }
}
