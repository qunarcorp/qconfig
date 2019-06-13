package qunar.tc.qconfig.admin.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.cloud.service.EnvironmentService;
import qunar.tc.qconfig.admin.dao.CandidateDao;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.exception.ConfigExistException;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.exception.OnePersonPublishException;
import qunar.tc.qconfig.admin.exception.StatusMismatchException;
import qunar.tc.qconfig.admin.greyrelease.ModificationDuringPublishingException;
import qunar.tc.qconfig.admin.model.Conflict;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.*;
import qunar.tc.qconfig.admin.web.bean.ConfigDetail;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.admin.web.security.Account;
import qunar.tc.qconfig.admin.web.security.TokenService;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.*;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.service.EnvironmentMappingService;
import qunar.tc.qconfig.servercommon.util.PriorityUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static qunar.tc.qconfig.common.bean.StatusType.DELETE;
import static qunar.tc.qconfig.common.bean.StatusType.codeOf;

/**
 * @author zhenyu.nie created on 2015 2015/4/20 23:16
 */
@Controller
public class ClientUploadController extends AbstractControllerHelper {

    private static final Logger logger = LoggerFactory.getLogger(ClientUploadController.class);

    @Resource
    private TokenService tokenService;

    @Resource
    private EventPostApplyService applyService;

    @Resource
    private UserContextService userContext;

    @Resource
    private CandidateDao candidateDao;

    @Resource
    private CandidateSnapshotDao candidateSnapshotDao;

    @Resource
    private ProfileService profileService;

    @Resource
    private EventPostApplyService eventPostApplyService;

    @Resource
    private EnvironmentService environmentService;

    @Resource
    private EnvironmentMappingService environmentMappingService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Set<String> validEnvTypes;

    private static final String emptyVm = "empty";

    @PostConstruct
    private void initValidEnvTypes() {
        List<String> defaultEnvList = environmentService.getSystemDefaultEnvs();
        validEnvTypes = Sets.newHashSet(defaultEnvList);
        validEnvTypes.remove(Environment.RESOURCES.text());
    }

    @RequestMapping(value = "/api/config/nocheck/upload", method = RequestMethod.POST)
    @ResponseBody
    public Object upload(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam String configDetails,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "token") String token
    ) throws IOException {

        String app;
        try {
            app = tokenService.decode(token);
            Preconditions.checkNotNull(app);
        } catch (Exception e) {
            write(response, ApiResponseCode.INVALID_TOKEN_CODE, "无效的token");
            return emptyVm;
        }

        if (!Strings.isNullOrEmpty(operator)) {
            userContext.setAccount(new Account(operator));
        }

        int result = 0;
        try {
            if (!Strings.isNullOrEmpty(operator)) {
                List<ConfigDetail> configDetailList = objectMapper.readValue(configDetails, objectMapper.getTypeFactory().constructParametricType(List.class, ConfigDetail.class));
                result = eventPostApplyService.batchSave(configDetailList, true, true);
            }
        } catch (Exception e) {
            logger.error("occur error when client upload file", e);
            return new JsonV2<>(ApiResponseCode.BAD_SERVICE_CODE, "服务器异常", result);
        }
        return new JsonV2<>(ApiResponseCode.OK_CODE, "上传成功", result);
    }

    @RequestMapping(value = "/api/config/latestsnapshot", method = RequestMethod.GET)
    public String latestCandidateSnapshot(HttpServletRequest request,
                                          HttpServletResponse response,
                                          @RequestParam("token") String token,
                                          @RequestParam("group") String group,
                                          @RequestParam("dataId") String dataId,
                                          @RequestParam("buildGroup") String buildGroup) throws IOException {
        try {
            Map<String, String> result = Maps.newHashMap();
            ConfigMeta meta = precheck(response, token, group, dataId, buildGroup, -1, "", "");
            if (meta == null) {
                return emptyVm;
            } else {
                CandidateSnapshot candidateSnapshot = null;
                List<ConfigMeta> priorityMetas = PriorityUtil.createPriorityList(meta);
                if (priorityMetas != null) {
                    for (ConfigMeta configMeta : priorityMetas) {
                        candidateSnapshot = candidateSnapshotDao.findLatestCandidateSnapshot(configMeta);
                        if (candidateSnapshot != null) {
                            result.put("profile", candidateSnapshot.getProfile());
                            result.put("version", String.valueOf(candidateSnapshot.getEditVersion()));
                            result.put("content", candidateSnapshot.getData());
                            result.put("statuscode", String.valueOf(candidateSnapshot.getStatus().code()));
                            write(response, ApiResponseCode.OK_CODE, objectMapper.writeValueAsString(result));
                            break;
                        }
                    }
                }
                if (candidateSnapshot == null) {
                    write(response, ApiResponseCode.FILE_NOT_EXIST_ON_QCONFIG_CODE, "配置不存在！");
                }
            }
        } catch (Exception e) {
            logger.error("服务异常", e);
            write(response, ApiResponseCode.BAD_SERVICE_CODE, "服务异常");
            return emptyVm;
        }
        return emptyVm;
    }

    @RequestMapping(value = "/api/config/status", method = RequestMethod.POST)
    public String changeStatus(HttpServletRequest request,
                               HttpServletResponse response,
                               @RequestParam("token") String token,
                               @RequestParam("group") String group,
                               @RequestParam("dataId") String dataId,
                               @RequestParam("buildGroup") String buildGroup,
                               @RequestParam("editversion") long editversion,
                               @RequestParam("fileProfile") String fileProfile,
                               @RequestParam(value = "isPublic", required = false, defaultValue = "false") boolean isPublic,
                               @RequestParam(value = "operator", required = false) String operator,
                               @RequestParam(value = "statuscode") int statuscode) throws IOException {
        try {
            fileProfile = environmentMappingService.getMappedProfile(fileProfile);
            ConfigMeta meta = precheck(response, token, group, dataId, buildGroup, editversion, fileProfile, operator);
            if (meta == null) {
                return emptyVm;
            } else {
                List<ConfigMeta> priorityMetas = PriorityUtil.createPriorityList(meta);
                CandidateSnapshot candidateSnapshot = findSnapshotConfig(priorityMetas, editversion);
                if (candidateSnapshot != null) {
                    CandidateDTO candidateDTO = new CandidateDTO();
                    candidateDTO.setGroup(candidateSnapshot.getGroup());
                    candidateDTO.setDataId(candidateSnapshot.getDataId());
                    candidateDTO.setProfile(candidateSnapshot.getProfile());
                    StatusType statusType = codeOf(statuscode);
                    candidateDTO.setEditVersion(editversion);
                    String description = userContext.getRtxId() + " " + statusType.text();
                    switch (statusType) {
                        case PASSED:
                            applyService.approve(candidateDTO, description);
                            break;
                        case REJECT:
                            applyService.reject(candidateDTO, description);
                            break;
                        case PUBLISH:
                            applyService.publish(candidateDTO, description);
                            if (isPublic) {
                                applyService.makePublic(new ConfigMeta(candidateSnapshot.getGroup(), candidateSnapshot.getDataId(), candidateSnapshot.getProfile()), "通过接口调用设置为公共文件！");
                            }
                            break;
                        case CANCEL:
                            applyService.cancel(candidateDTO, description);
                            break;
                        default:
                            throw new IllegalArgumentException("状态不正确");

                    }
                } else {
                    write(response, ApiResponseCode.FILE_NOT_EXIST_ON_QCONFIG_CODE, "配置在服务器上不存在");
                }
            }
            write(response, ApiResponseCode.OK_CODE, "正常");
        } catch (ModifiedException e) {
            write(response, ApiResponseCode.HAS_BEEN_MODIFIED_CODE, "文件已经被修改");
            return emptyVm;
        } catch (StatusMismatchException e) {
            write(response, ApiResponseCode.NOT_IN_MODIFY_STATUS, "文件不在可审批状态");
            return emptyVm;
        } catch (OnePersonPublishException e) {
            write(response, ApiResponseCode.NOT_IN_MODIFY_STATUS, "线上环境文件不能只由一人编辑审核并发布，至少要有两个人参与");
            return emptyVm;
        } catch (Exception e) {
            logger.error("服务异常", e);
            write(response, ApiResponseCode.BAD_SERVICE_CODE, "服务异常");
            return emptyVm;
        }
        return emptyVm;
    }

    @RequestMapping(value = "/api/config/uploadError")
    public String uploadError(HttpServletResponse response) throws IOException {
        write(response, ApiResponseCode.OVER_UPLOAD_SIZE_CODE, "上传超过文件大小限制");
        return emptyVm;
    }

    @RequestMapping(value = "/api/config/upload", method = RequestMethod.POST)
    public String upload(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestParam("token") String token,
                         @RequestParam("group") String group,
                         @RequestParam("dataId") String dataId,
                         @RequestParam("buildGroup") String buildGroup,
                         @RequestParam("version") long version,
                         @RequestParam("fileProfile") String fileProfile,
                         @RequestParam("content") String content,
                         @RequestParam(value = "isPublic", required = false, defaultValue = "false") boolean isPublic,
                         @RequestParam(value = "operator", required = false) String operator,
                         @RequestParam(value = "isdirectpublish", required = false, defaultValue = "true") boolean isDirectPublish,
                         @RequestParam(value = "description", required = false, defaultValue = "") String description) throws IOException {
        long stsOne = System.currentTimeMillis();
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            fileProfile = environmentMappingService.getMappedProfile(fileProfile);
            ConfigMeta meta = precheck(response, token, group, dataId, buildGroup, version, fileProfile, operator);
            if (meta == null) {
                return emptyVm;
            }
            CandidateDTO candidateDTO = new CandidateDTO();
            if (version > 0) {
                List<ConfigMeta> priorityMetas = PriorityUtil.createPriorityList(meta);

                CandidateSnapshot candidateSnapshot = findSnapshotConfig(priorityMetas, version);
                if (candidateSnapshot == null) {
                    write(response, ApiResponseCode.FILE_NOT_EXIST_ON_QCONFIG_CODE, "qconfig上没有此文件");
                    return emptyVm;
                }

                if (!candidateSnapshot.getProfile().equals(fileProfile) || candidateSnapshot.getEditVersion() != version) {
                    write(response, ApiResponseCode.HAS_BEEN_MODIFIED_CODE, "文件已被修改");
                    return emptyVm;
                }

                candidateDTO.setGroup(candidateSnapshot.getGroup());
                candidateDTO.setDataId(candidateSnapshot.getDataId());
                candidateDTO.setProfile(candidateSnapshot.getProfile());
                candidateDTO.setEditVersion(candidateSnapshot.getEditVersion());
                candidateDTO.setData(content);
            } else {
                // 新建文件的情况
                if (!profileExist(group, meta.getProfile())) {
                    write(response, ApiResponseCode.BUILD_GROUP_NOT_EXIST_CODE, QConfigAttributesLoader.getInstance().getBuildGroup() + "不存在");
                    return emptyVm;
                }

                candidateDTO.setGroup(group);
                candidateDTO.setDataId(dataId);
                candidateDTO.setProfile(meta.getProfile());
                candidateDTO.setEditVersion(INIT_BASED_VERSION);
                candidateDTO.setData(content);
            }
            boolean forceApply = false;
            if (candidateDTO.getEditVersion() == INIT_BASED_VERSION) {
                Candidate candidateNow = candidateDao.find(candidateDTO.getGroup(), candidateDTO.getDataId(), candidateDTO.getProfile());
                // 这是文件已经被删除而客户端进行新建的情况
                if (candidateNow != null && candidateNow.getStatus() == DELETE) {
                    forceApply = true;
                    candidateDTO.setEditVersion(candidateNow.getEditVersion());
                } else if (candidateNow != null) {
                    write(response, ApiResponseCode.HAS_BEEN_MODIFIED_CODE, "文件已被修改");
                    return emptyVm;
                }
            }

            try {
                candidateDTO.setDescription(description);
                if (isDirectPublish) {
                    applyService.oneButtonPublish(candidateDTO, "client直接发布", forceApply);
                } else {
                    applyService.apply(candidateDTO, "client提交配置，等待审核");
                }
            } catch (StatusMismatchException e) {
                write(response, ApiResponseCode.NOT_IN_MODIFY_STATUS, "文件处于不能上传的状态");
                return emptyVm;
            } catch (ModificationDuringPublishingException e) {
                write(response, ApiResponseCode.NOT_IN_MODIFY_STATUS, "文件正在发布中，不能上传");
                return emptyVm;
            } catch (ConfigExistException e) {
                if (e.getConflict().getType() == Conflict.Type.REF) {
                    write(response, ApiResponseCode.CANCEL_REF_STATUS, "文件处于取消引用状态");
                } else {
                    write(response, ApiResponseCode.HAS_BEEN_MODIFIED_CODE, "文件已被修改");
                }
                return emptyVm;
            } catch (ModifiedException e) {
                write(response, ApiResponseCode.HAS_BEEN_MODIFIED_CODE, "文件已被修改");
                return emptyVm;
            }
            if (isPublic) {
                ConfigMeta configMeta = new ConfigMeta(candidateDTO.getGroup(), candidateDTO.getDataId(), candidateDTO.getProfile());
                applyService.makePublic(configMeta, "通过接口调用设置为公共文件！");
                logger.info("make public through api {}", configMeta);
            }
            write(response, ApiResponseCode.OK_CODE, "ok");

            return emptyVm;
        } catch (Exception e) {
            logger.error("occur error when client upload file", e);
            write(response, ApiResponseCode.BAD_SERVICE_CODE, "服务器异常");
            return emptyVm;
        } finally {
            logger.info("client uploader, cost:{}", System.currentTimeMillis() - stsOne);
            Monitor.clientUploadTimer.update(stopwatch.elapsed(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
    }

    private boolean profileExist(String group, String profile) {
        String buildGroup = ProfileUtil.getBuildGroup(profile);
        return Strings.isNullOrEmpty(buildGroup) || profileService.exist(group, profile);
    }

    private CandidateSnapshot findSnapshotConfig(List<ConfigMeta> priorityMetas, long version) {
        for (ConfigMeta meta : priorityMetas) {
            CandidateSnapshot config = candidateSnapshotDao.find(meta.getGroup(), meta.getDataId(), meta.getProfile(), version);
            if (config != null) {
                return config;
            }
        }
        return null;
    }

    private void checkFileProfileValid(String machineProfile, String fileProfile, long version) {
        Environment machineEnv = Environment.fromProfile(machineProfile);
        Environment fileEnv = Environment.fromProfile(fileProfile);

        // 只有prod环境能修改resources下文件
        if (fileEnv.isResources()) {
            Preconditions.checkArgument(machineEnv.isProd(), "修改resource不是生产，" + machineEnv);
            return;
        }

        Preconditions.checkArgument(machineEnv.equalsEnv(fileEnv), "机器env和配置env不一致，machineEnv.env()" + machineEnv.env() + ", fileEnv.env()" + fileEnv.env());

        // 如果机器没有buildGroup，那么fileProfile必须相等；如果有buildGroup，fileProfile要么没有buildGroup要么相等
        if (machineProfile.equals(machineEnv.defaultProfile())) {
            Preconditions.checkArgument(fileProfile.equals(fileEnv.defaultProfile()), "如果机器没有buildGroup，fileProfile和fileEnv.defaultProfile()必须一致，fileProfile：" + fileProfile + "fileEnv.defaultProfile():" + fileEnv.defaultProfile());
        } else {
            Preconditions.checkArgument(fileProfile.equals(fileEnv.defaultProfile()) || fileProfile.equals(machineProfile), "存在buildgroup的情况，fileProfile:" + fileProfile + "，fileEnv：" + fileEnv + "，machineProfile:" + machineProfile);
        }
    }

    private void write(HttpServletResponse response, int code, String message) throws IOException {
        String codeValue = String.valueOf(code);
        if (code != ApiResponseCode.OK_CODE) {
            Monitor.clientUpdateFileCountInc(code);
            logger.info("client upload file failOf, code=[{}], message=[{}]", code, message);
        }

        response.setHeader(Constants.CODE, codeValue);
        response.getWriter().write(message);
        response.flushBuffer();
    }

    private ConfigMeta precheck(HttpServletResponse response, String token, String group, String dataId, String buildGroup, long version, String fileProfile, String operator) throws IOException {
        logger.info("client upload file, token=[{}], group=[{}], dataId=[{}], buildGroup=[{}], version=[{}], fileProfile=[{}], operator=[{}]",
                token, group, dataId, buildGroup, version, fileProfile, operator);
        if (!Strings.isNullOrEmpty(operator)) {
            userContext.setAccount(new Account(operator));
        }
        String ip = userContext.getIp();
        String app;
        try {
            app = tokenService.decode(token);
            Preconditions.checkNotNull(app);
        } catch (Exception e) {
            write(response, ApiResponseCode.INVALID_TOKEN_CODE, "无效的token");
            return null;
        }

        try {
            checkLegalProfile(fileProfile);
        } catch (Exception e) {
            write(response, ApiResponseCode.INVALID_PARAM_CODE, "无效的参数");
            return null;
        }


        ConfigMeta meta;
        try {
            meta = new ConfigMeta(group, dataId, fileProfile);
            checkLegalMeta(meta);
        } catch (Exception e) {
            write(response, ApiResponseCode.INVALID_PARAM_CODE, "无效的参数");
            return null;
        }

        return meta;
    }
}
