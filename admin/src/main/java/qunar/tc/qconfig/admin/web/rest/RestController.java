package qunar.tc.qconfig.admin.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import qunar.tc.qconfig.admin.dao.CandidateDao;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dao.FilePublicStatusDao;
import qunar.tc.qconfig.admin.dao.ReferenceDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.exception.ConfigExistException;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.exception.StatusMismatchException;
import qunar.tc.qconfig.admin.greyrelease.ModificationDuringPublishingException;
import qunar.tc.qconfig.admin.model.ApiPermission;
import qunar.tc.qconfig.admin.model.Conflict;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.model.rest.upload.UploadFileEntity;
import qunar.tc.qconfig.admin.model.rest.upload.UploadRequest;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.EventPostApplyService;
import qunar.tc.qconfig.admin.service.ProfileService;
import qunar.tc.qconfig.admin.service.UserContextService;
import qunar.tc.qconfig.admin.service.impl.ApiPermissionServiceImpl;
import qunar.tc.qconfig.admin.support.CheckUtil;
import qunar.tc.qconfig.admin.web.security.Account;
import qunar.tc.qconfig.client.spring.QConfig;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.ApiResponseCode;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static qunar.tc.qconfig.common.bean.StatusType.DELETE;

/**
 * 上传配置相关的rest api
 * <p>
 * Created by chenjk on 2018/1/17.
 */
@Controller
public class RestController {

    private static final Logger logger = LoggerFactory.getLogger(RestController.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final long INIT_VERSION = 0;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private CandidateDao candidateDao;

    @Autowired
    private CandidateSnapshotDao candidateSnapshotDao;

    @Autowired
    private FilePublicStatusDao filePublicStatusDao;

    @Autowired
    private EventPostApplyService applyService;

    @Autowired
    private ApiPermissionServiceImpl apiPermissionService;

    @Resource(name = "referenceDao")
    private ReferenceDao referenceDao;

    @Resource
    private ProfileService profileService;

    @QConfig("config.properties")
    private Map<String, String> configs;

    @RequestMapping(value = "restapi/configs", method = RequestMethod.GET)
    @ResponseBody
    public Object getPublishedConfig(HttpServletRequest request,
                                     @RequestParam(value = "token", required = false) String token,
                                     @RequestParam("groupid") String groupid,
                                     @RequestParam("dataid") String dataId,
                                     @RequestParam("env") String env,
                                     @RequestParam(value = "subenv", required = false, defaultValue = "") String subenv,
                                     @RequestParam("targetgroupid") String targetGroupid) {
        try {
            Environment environment = Environment.generate(env, subenv);
            CheckUtil.checkLegalGroup(groupid);
            CheckUtil.checkLegalGroup(targetGroupid);
            CheckUtil.checkLegalDataId(dataId);
            CheckUtil.checkLegalProfile(environment.profile());
            //清理token相关逻辑
            if (!hasGroupIdPermission(token, groupid, targetGroupid, request.getRequestURI(), request.getMethod())) {
                return new JsonV2<>(ApiResponseCode.NO_READ_PERMISSION, "没有读取配置权限", "");
            }
            CandidateSnapshot candidateSnapshot = candidateSnapshotDao.findLatestCandidateSnapshot(new ConfigMeta(targetGroupid, dataId, environment.profile()));
            if (candidateSnapshot != null
                    && candidateSnapshot.getStatus() == StatusType.PUBLISH) {
                return new JsonV2<>(ApiResponseCode.OK_CODE, "正常", candidateSnapshot);
            } else {
                return new JsonV2<>(ApiResponseCode.FILE_NOT_EXIST_ON_QCONFIG_CODE, "配置不存在", "");
            }
        } catch (Throwable e) {
            logger.error("读取配置失败！", e);
            return new JsonV2<>(ApiResponseCode.BAD_SERVICE_CODE, "读取配置失败！" + e.getMessage(), "");
        }
    }

    @RequestMapping(value = "restapi/configs", method = RequestMethod.POST)
    @ResponseBody
    public Object uploadConfig(HttpServletRequest request,
                               @RequestParam(value = "token", required = false) String token,
                               @RequestBody UploadRequest uploadRequest) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            checkPostConfig(uploadRequest, request);
            if (!hasGroupIdPermission(token, uploadRequest.getConfig().getGroupid(),
                    uploadRequest.getConfig().getTargetgroupid(),
                    request.getRequestURI(),
                    request.getMethod())) {
                return new JsonV2<>(ApiResponseCode.NO_MODIFY_FILE_PERMISSION_CODE, "没有修改配置权限", "");
            }

            if (!Strings.isNullOrEmpty(uploadRequest.getOperator())) {
                userContextService.setAccount(new Account(uploadRequest.getOperator()));
            }
            logger.info("rest api post config, " + mapper.writeValueAsString(uploadRequest) + " ,token:" + token);
            UploadFileEntity uploadFileEntity = uploadRequest.getConfig();
            Environment environment = Environment.generate(uploadFileEntity.getEnv(), uploadFileEntity.getSubenv());
            ConfigMeta configMeta = new ConfigMeta(uploadFileEntity.getTargetgroupid(), uploadFileEntity.getDataid(), environment.profile());
            CandidateDTO candidateDTO = new CandidateDTO();
            candidateDTO.setGroup(uploadFileEntity.getTargetgroupid());
            candidateDTO.setDataId(uploadFileEntity.getDataid());
            candidateDTO.setProfile(configMeta.getProfile());
            candidateDTO.setEditVersion(uploadFileEntity.getVersion());
            candidateDTO.setData(uploadFileEntity.getContent());
            candidateDTO.setDescription(uploadFileEntity.getDescription() == null ? "" : uploadFileEntity.getDescription());
            applyService.oneButtonPublish(candidateDTO, candidateDTO.getDescription(), isforceload(candidateDTO));
            if (uploadFileEntity.isPublic()
                    && !filePublicStatusDao.exist(new ConfigMetaWithoutProfile(candidateDTO.getGroup(), candidateDTO.getDataId()))) {
                applyService.makePublic(configMeta, "通过rest api 将配置设为public！");
            }
        } catch (StatusMismatchException e) {
            logger.error("文件处于不能上传的状态", e);
            return new JsonV2<>(ApiResponseCode.NOT_IN_MODIFY_STATUS, "文件处于不能上传的状态", "");
        } catch (ModificationDuringPublishingException e) {
            logger.error("文件正在发布中，不能上传");
            return new JsonV2<>(ApiResponseCode.NOT_IN_MODIFY_STATUS, "文件正在发布中，不能上传", "");
        } catch (ConfigExistException e) {
            if (e.getConflict().getType() == Conflict.Type.REF) {
                logger.error("文件处于取消引用状态", e);
                return new JsonV2<>(ApiResponseCode.CANCEL_REF_STATUS, "文件处于取消引用状态", "");
            } else {
                logger.error("文件已被修改", e);
                return new JsonV2<>(ApiResponseCode.HAS_BEEN_MODIFIED_CODE, "文件已被修改", "");
            }
        } catch (ModifiedException e) {
            logger.error("文件已被修改", e);
            return new JsonV2<>(ApiResponseCode.HAS_BEEN_MODIFIED_CODE, "文件已被修改", "");
        } catch (Throwable e) {
            logger.error("上传配置失败！", e);
            return new JsonV2<>(ApiResponseCode.BAD_SERVICE_CODE, "上传配置失败！" + e.getMessage(), "");
        } finally {
            Monitor.REST_CONFIGS_POST_TIMER.update(stopwatch.elapsed(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
            Monitor.REST_CONFIGS_POST_COUNT.inc();
        }
        return new JsonV2<>(ApiResponseCode.OK_CODE, "上传成功！", "");
    }

    @RequestMapping(value = "restapi/{targetgroupid}/{env}/{subenv}", method = RequestMethod.POST)
    @ResponseBody
    public Object createSubenv(HttpServletRequest request,
                               @RequestParam(value = "token", required = false) String token,
                               @RequestParam("groupid") String groupid,
                               @PathVariable("targetgroupid") String targetgroupid,
                               @PathVariable("env") String env,
                               @PathVariable("subenv") String subenv) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(targetgroupid), "appid不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(env), "env不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(subenv), "subenv不能为空！");
        if (!hasGroupIdPermission(token,
                groupid,
                targetgroupid,
                request.getRequestURI(),
                request.getMethod())) {
            return new JsonV2<>(ApiResponseCode.BUILD_GROUP_CANNOT_CREATE_PERMISSION_CODE, "没有创建子环境权限", "");
        }
        Environment environment = Environment.generate(env, subenv);
        if (!profileService.exist(targetgroupid, environment.profile())) {
            profileService.create(targetgroupid, environment.profile());
            return new JsonV2<>(ApiResponseCode.OK_CODE, "创建子环境成功！", "");
        } else {
            return new JsonV2<>(ApiResponseCode.BUILD_GROUP_NOT_EXISTED_CODE, "子环境已经存在！", "");
        }
    }

    @RequestMapping(value = "restapi/{targetgroupid}/{env}/{subenv}", method = RequestMethod.GET)
    @ResponseBody
    public Object subenvExist(HttpServletRequest request,
                              @RequestParam(value = "token", required = false) String token,
                              @RequestParam("groupid") String groupid,
                              @PathVariable("targetgroupid") String targetgroupid,
                              @PathVariable("env") String env,
                              @PathVariable("subenv") String subenv) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(targetgroupid), "appid不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(env), "env不能为空！");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(subenv), "subenv不能为空！");
        if (!hasGroupIdPermission(token,
                groupid,
                targetgroupid,
                request.getRequestURI(),
                request.getMethod())) {
            return new JsonV2<>(ApiResponseCode.BUILD_GROUP_CANNOT_CREATE_PERMISSION_CODE, "没有查询子环境是否存在的权限", "");
        }
        Environment environment = Environment.generate(env, subenv);
        if (profileService.exist(targetgroupid, environment.profile())) {
            return new JsonV2<>(ApiResponseCode.OK_CODE, "子环境已经存在！", "");
        } else {
            return new JsonV2<>(ApiResponseCode.BUILD_GROUP_NOT_EXIST_CODE, "子环境不存在！", "");
        }
    }

    @RequestMapping(value = "restapi/configs/exists", method = RequestMethod.GET)
    @ResponseBody
    public Object configExist(HttpServletRequest request,
                              @RequestParam(value = "token", required = false) String token,
                              @RequestParam("groupid") String groupid,
                              @RequestParam("dataids") List<String> dataIds,
                              @RequestParam("env") String env,
                              @RequestParam(value = "subenv", required = false, defaultValue = "") String subenv,
                              @RequestParam("targetgroupid") String targetGroupid) {
        try {
            List<Map<String, Boolean>> result = Lists.newLinkedList();
            Environment environment = Environment.generate(env, subenv);
            CheckUtil.checkLegalGroup(groupid);
            CheckUtil.checkLegalGroup(targetGroupid);
            CheckUtil.checkLegalProfile(environment.profile());
            //清理token相关逻辑
            if (!hasGroupIdPermission(token, groupid, targetGroupid, request.getRequestURI(), request.getMethod())) {
                return new JsonV2<>(ApiResponseCode.NO_READ_PERMISSION, "没有查询配置是否存在的权限", "");
            }

            for (String dataId : dataIds) {
                CheckUtil.checkLegalDataId(dataId);
                CandidateSnapshot candidateSnapshot = candidateSnapshotDao.findLatestCandidateSnapshot(new ConfigMeta(targetGroupid, dataId, environment.profile()));
                Reference reference = referenceDao.findEverReference(new ConfigMeta(targetGroupid, dataId, environment.profile()));
                Map<String, Boolean> singleResult = Maps.newHashMap();
                if (candidateSnapshot != null
                        && candidateSnapshot.getStatus() == StatusType.PUBLISH) {
                    singleResult.put(dataId, true);
                } else if (reference != null) {
                    singleResult.put(dataId, true);
                } else {
                    singleResult.put(dataId, false);
                }
                result.add(singleResult);
            }
            return new JsonV2<>(ApiResponseCode.OK_CODE, "正常", result);
        } catch (Throwable e) {
            logger.error("读取配置失败！", e);
            return new JsonV2<>(ApiResponseCode.BAD_SERVICE_CODE, "读取配置失败！" + e.getMessage(), "");
        }
    }

    private boolean isforceload(CandidateDTO candidateDTO) {
        if (candidateDTO.getEditVersion() != INIT_VERSION) {
            return false;
        } else {
            Candidate candidateNow = candidateDao.find(candidateDTO.getGroup(), candidateDTO.getDataId(), candidateDTO.getProfile());
            if (candidateNow != null && candidateNow.getStatus() == DELETE) {
                candidateDTO.setEditVersion(candidateNow.getEditVersion());
                return true;
            }
        }
        return false;
    }

    private void checkPostConfig(UploadRequest uploadRequest, HttpServletRequest request) throws IllegalAccessException {
        Preconditions.checkArgument(uploadRequest != null, "保存对象不能为空!");
        UploadFileEntity uploadFileEntity = uploadRequest.getConfig();
        if (uploadFileEntity != null) {
            CheckUtil.checkLegalGroup(uploadFileEntity.getGroupid());
            CheckUtil.checkLegalDataId(uploadFileEntity.getDataid());
            //check file profile and profile from server.properties
            Environment environment = Environment.generate(uploadFileEntity.getEnv(), uploadFileEntity.getSubenv());
            CheckUtil.checkLegalProfile(environment.profile());
            Environment serverEnvironment = Environment.generate(uploadFileEntity.getServerenv(), uploadFileEntity.getServersubenv());
            CheckUtil.checkLegalProfile(serverEnvironment.profile());
            checkEnvironment(environment, serverEnvironment);
        }
    }

    private void checkToken(String groupId, String targetGroupId, String token) {
        if (RestUtil.checkToken(groupId, token)) {
            return;
        } else {
            Preconditions.checkArgument(RestUtil.checkToken(groupId, targetGroupId, token), "token校验失败");
        }
    }

    private void checkEnvironment(Environment environment, Environment serverEnvironment) throws IllegalAccessException {
        if ((environment.isResources() || environment.isProd()) && !serverEnvironment.isProd()) {
            throw new IllegalAccessException(serverEnvironment.profile() + "环境不能操作" + environment.profile() + "下的配置");
        }
    }

    private boolean hasGroupIdPermission(String token, String groupId, String targetGroupId, String url, String method) {
        if (Strings.isNullOrEmpty(groupId)) {
            return false;
        }

        String adminAppids = configs.get("restapi.admin.appids");
        if (!Strings.isNullOrEmpty(adminAppids) && adminAppids.contains(groupId)) {//拥有超级权限，可以修改其他任何appid配置,不需要token
            return true;
        }

        checkToken(groupId, targetGroupId, token);//checktoken

        if (groupId.equalsIgnoreCase(targetGroupId)) {
            return true;
        }

        List<ApiPermission> apiPermissionList = apiPermissionService.queryByGroupIdAndTargetGroupId(groupId, targetGroupId);
        if (apiPermissionList == null || apiPermissionList.size() == 0) {
            return false;
        }

        AntPathMatcher antPathMatcher = new AntPathMatcher();

        for (ApiPermission apiPermission : apiPermissionList) {
            if (apiPermission.getMethod() != null
                    && apiPermission.getMethod().equalsIgnoreCase(method)
                    && antPathMatcher.match(apiPermission.getUrl(), url)) {
                return true;
            }
        }
        return false;
    }
}
