package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.service.UserBehaviorService;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = {"/qconfig", "/qconfig/user"})
public class UserBehaviorController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(UserBehaviorController.class);

    @Resource
    UserContextService userContext;

    @Resource
    UserBehaviorService userBehaviorService;

    @RequestMapping(value = "/favorites/listFiles", method = RequestMethod.GET)
    @ResponseBody
    public Object listFavoritesFile(@RequestParam(required = false, defaultValue = "1") String page,
                                   @RequestParam(required = false, defaultValue = "10") String pageSize) {
        String user = userContext.getRtxId();
        try {
            return JsonV2.successOf(userBehaviorService.listFavoriteFileInfo(user, Integer.valueOf(page), Integer.valueOf(pageSize)));
        } catch (Exception e) {
            logger.error("get user favorite files error, user:[{}]", user, e);
            return JsonV2.failOf("获取收藏文件列表失败");
        }
    }

    @RequestMapping(value = "/favorites/listGroups", method = RequestMethod.GET)
    @ResponseBody
    public Object listFavoriteGroups(@RequestParam(required = false, defaultValue = "1") String page,
                                    @RequestParam(required = false, defaultValue = "10") String pageSize) {
        String user = userContext.getRtxId();
        try {
            return JsonV2.successOf(userBehaviorService.listFavoriteGroups(user, Integer.valueOf(page), Integer.valueOf(pageSize)));
        } catch (Exception e) {
            logger.error("get user favorite groups error,user:[{}]", user, e);
            return JsonV2.failOf("获取收藏应用列表失败");
        }
    }

    @RequestMapping(value = "/favorites/isFavoriteFile", method = RequestMethod.GET)
    @ResponseBody
    public Object listFavoriteGroups(@RequestParam String group, @RequestParam String dataId, @RequestParam String profile) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        String user = userContext.getRtxId();
        try {
            return JsonV2.successOf(userBehaviorService.isFavoriteFile(meta, user));
        } catch (Exception e) {
            logger.error("query is user favorite group error, meta:[{}], user:[{}]", meta, user, e);
            return JsonV2.failOf("查询文件是否被收藏失败");
        }
    }

    @RequestMapping(value = "/favorites/isFavoriteGroup", method = RequestMethod.GET)
    @ResponseBody
    public Object listFavoriteGroups(@RequestParam String group) {
        String user = userContext.getRtxId();
        try {
            return JsonV2.successOf(userBehaviorService.isFavoriteGroup(group, user));
        } catch (Exception e) {
            logger.error("query is user favorite group error, group:[{}], user:[{}]", group, user, e);
            return JsonV2.failOf("查询应用是否被收藏失败");
        }
    }

    @RequestMapping("/favorites/addFile")
    @ResponseBody
    public Object addFavoriteFile(@RequestBody ConfigMeta meta) {
        checkLegalMeta(meta);
        String user = userContext.getRtxId();
        logger.info("add user favorite file, configMeta:[{}], user:[{}]", meta, user);
        try {
            userBehaviorService.insertFavoriteFile(meta, user);
            return JsonV2.success();
        } catch (Exception e) {
            logger.error("add user favorite file error, configMeta:[{}], user:[{}]", meta, user, e);
            return JsonV2.failOf("添加收藏文件失败");
        }
    }

    @RequestMapping("/favorites/addGroup")
    @ResponseBody
    public Object addFavoriteGroup(@RequestBody ConfigMeta meta) {
        String group = meta.getGroup();
        checkLegalGroup(group);
        String user = userContext.getRtxId();
        logger.info("add user favorite group, group:[{}], user:[{}]", meta, user);
        try {
            userBehaviorService.insertFavoriteGroup(group, user);
            return JsonV2.success();
        } catch (Exception e) {
            logger.error("add user favorite group error, group:[{}], user:[{}]", group, user, e);
            return JsonV2.failOf("添加收藏应用失败");
        }
    }

    @RequestMapping("/favorites/deleteFile")
    @ResponseBody
    public Object deleteFavoriteFile(@RequestBody ConfigMeta meta) {
        String user = userContext.getRtxId();
        logger.info("delete user favorite file, configMeta:[{}], user:[{}]", meta, user);
        try {
            userBehaviorService.deleteFavoriteFile(meta, user);
            return JsonV2.success();
        } catch (Exception e) {
            logger.error("delete user favorite file error, configMeta:[{}], user:[{}]", meta, user, e);
            return JsonV2.failOf("删除收藏文件失败");
        }
    }

    @RequestMapping("/favorites/deleteGroup")
    @ResponseBody
    public Object deleteFavoriteGroup(@RequestBody ConfigMeta meta) {
        String group = meta.getGroup();
        checkLegalGroup(group);
        String user = userContext.getRtxId();
        logger.info("add user favorite group, group:[{}], user:[{}]", group, user);
        try {
            userBehaviorService.deleteFavoriteGroup(group, user);
            return JsonV2.success();
        } catch (Exception e) {
            logger.error("delete user favorite group error, group:[{}], user:[{}]", meta, user, e);
            return JsonV2.failOf("删除收藏应用失败");
        }
    }

    @RequestMapping("/lastModifiedFiles")
    @ResponseBody
    public Object listLastModifiedFiles(@RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        Preconditions.checkArgument(pageSize != null && pageSize > 0, "pageSize应大于0");
        String user = userContext.getRtxId();
        try {
            return JsonV2.successOf(userBehaviorService.listUserLastModifiedFile(user, pageSize));
        } catch (Exception e) {
            logger.error("list user last modified files error, user:{}, pageSize:{}", user, pageSize, e);
            return JsonV2.fail();
        }
    }

}
