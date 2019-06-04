package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.cloud.vo.BatchPushVo;
import qunar.tc.qconfig.admin.cloud.vo.EditPushHostVO;
import qunar.tc.qconfig.admin.cloud.vo.EditingPushVo;
import qunar.tc.qconfig.admin.cloud.vo.PushSimpleVo;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.dto.PushDto;
import qunar.tc.qconfig.admin.dto.PushItemDto;
import qunar.tc.qconfig.admin.model.ConfigUsedLog;
import qunar.tc.qconfig.admin.model.PushItemWithHostName;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.BastionAddressService;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.ConsumerService;
import qunar.tc.qconfig.admin.service.EventPostApplyService;
import qunar.tc.qconfig.admin.service.FixedConsumerVersionService;
import qunar.tc.qconfig.admin.service.ListeningClientsService;
import qunar.tc.qconfig.admin.service.PushHistoryService;
import qunar.tc.qconfig.admin.support.AddressUtil;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/qconfig/file/push")
public class FilePushController extends AbstractControllerHelper {

    private final static int LIST_CLIENTS_TIMEOUT = 5;
    @Resource
    private ConsumerService consumerService;

    @Resource
    private BastionAddressService bastionAddressService;

    @Resource
    private ListeningClientsService listeningClientsService;

    @Resource
    private ActionController actionController;

    @Resource
    private EventPostApplyService applyService;

    @Resource
    private ConfigService configService;

    @Resource
    private PushHistoryService pushHistoryService;

    @Resource
    private FixedConsumerVersionService fixedConsumerVersionService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Splitter COLON_SPLITTER = Splitter.on(':').trimResults().omitEmptyStrings();

    private static final int IP_PORT_PAIR_LIMIT = 2;

    @RequestMapping(value = "/getAddresses", method = RequestMethod.GET)
    @ResponseBody
    public Object getAddresses(@RequestParam("group") String group, @RequestParam("profile") String profile,
                               @RequestParam("dataId") String dataId) {

        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        try {
            ListenableFuture<Set<ClientData>> clientsDataFuture = listeningClientsService.getListeningClientsData(meta, true);
            Set<ClientData> clientDataSet = clientsDataFuture.get(Constants.FUTURE_DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            return  JsonV2.successOf(fixedConsumerVersionService.addFixedVersion(meta, transformToUseLog(clientDataSet, meta)));
        } catch (Exception e) {
            logger.error("get addresses error with group={}, dataId={}, profile={}", group, dataId, profile, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！");
        }
    }

    // TODO: 2018/12/11 这个链接要改
    @RequestMapping(value = "/getConsumerAddress", method = RequestMethod.GET)
    @ResponseBody
    public Object getOnlineAddresses(@RequestParam("group") String group, @RequestParam("profile") String profile,
                               @RequestParam("dataId") String dataId) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        try {
            List<ConfigUsedLog> consumerLogs = consumerService.getConsumerLogs(meta, ConsumerService.ALLOW_PUSH_TYPE);//TODO 这里可以先不动，等前端干完，再看是否有用，建议前端都从新接口走
            return JsonV2.successOf(fixedConsumerVersionService.addFixedVersion(meta, consumerLogs));
        } catch (Exception e) {
            logger.error("get addresses error with group={}, dataId={}, profile={}", group, dataId, profile, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！");
        }
    }

    private List<ConfigUsedLog> transformToUseLog(Set<ClientData> clientDataSet, ConfigMeta meta) {
        List<ConfigUsedLog> result = Lists.newLinkedList();
        for (ClientData clientData : clientDataSet) {
            result.add(new ConfigUsedLog(meta, clientData));
        }
        return result;
    }

    @RequestMapping(value = "/pushToMachine", method = RequestMethod.POST)
    @ResponseBody
    public Object push(@RequestBody PushDto pushDto) {

        logger.info("push with {}", pushDto);

        CandidateDTO dto = new CandidateDTO(pushDto.getGroup(), pushDto.getDataId(), pushDto.getProfile(),
                pushDto.getBasedVersion(), pushDto.getEditVersion(), null, "");

        List<PushItemWithHostName> destinations;

        Monitor.PUSH_STATICS.inc();
        destinations = getPushItemWithHostNames(pushDto.getGroup(), pushDto.getDataId(), pushDto.getProfile(), pushDto.getPushItems());

        try {
            if (!destinations.isEmpty()) {
                applyService.push(dto, "", destinations);
            }
            return JsonV2.successOf(true);
        } catch (Exception e) {
            return handleException(dto, e);
        }
    }

    private List<PushItemWithHostName> getPushItemWithHostNames(String group, String dataId, String profile, List<PushItemDto> pushItems) {
        List<PushItemWithHostName> destinations;
        try {
            destinations = getPushItemWithHostNames(
                    new ConfigMeta(group, dataId, profile),
                    pushItems);
        } catch (RuntimeException re) {
            throw new IllegalArgumentException(re.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("不正确的推送地址");
        }
        return destinations;
    }

    /**
     * 接口的结果处理在verify-171029.js中loadPushResult函数中
     */
    @RequestMapping(value = "/pushToMachineResult", method = RequestMethod.POST)
    @ResponseBody
    public Object pushResult(@RequestBody PushDto pushDto) {
        ConfigMeta meta = new ConfigMeta(pushDto.getGroup(), pushDto.getDataId(), pushDto.getProfile());
        checkLegalMeta(meta);
        try {
            return JsonV2.successOf(consumerService.getConsumerLogs(meta, ConsumerService.ALLOW_PUSH_TYPE).stream().filter(new IpPortFilter(pushDto.getPushItems())::apply).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("get push result error with pushDto {}", pushDto, e);
            throw new RuntimeException("获取推送结果失败");
        }
    }

    /**
     * 批量推送
     *
     * @param pushVo 需要推送的内容
     * @return 推送失败的条目，以及失败原因
     */
    @RequestMapping(value = "/batchPushToMachine", method = RequestMethod.POST)
    @ResponseBody
    public Object batchPush(@RequestBody BatchPushVo pushVo) {

        logger.info("batch push with {}", pushVo);

        if (pushVo.getPushSimpleVos() == null || pushVo.getPushSimpleVos().size() == 0) {
            return JsonV2.failOf( "推送内容为空");
        }

        if (pushVo.getPushItems() == null || pushVo.getPushItems().size() == 0) {
            return new JsonV2<>(-1, "推送机器为空", null);
        }

        Monitor.BATCH_PUSH_STATICS.inc();
        List<Map<String, String>> resultList = Lists.newLinkedList();

        for (PushSimpleVo pushSimpleVo : pushVo.getPushSimpleVos()) {

            logger.info("push with {}", pushSimpleVo);
            CandidateDTO dto = new CandidateDTO(pushSimpleVo.getGroup(), pushSimpleVo.getDataId(),
                    pushSimpleVo.getProfile(), pushSimpleVo.getBasedVersion(), pushSimpleVo.getEditVersion(), null, "");

            Monitor.PUSH_STATICS.inc();
            List<PushItemWithHostName> destinations;
            try {
                destinations = getPushItemWithHostNames(
                        new ConfigMeta(pushSimpleVo.getGroup(), pushSimpleVo.getDataId(), pushSimpleVo.getProfile()),
                        pushVo.getPushItems());
            } catch (RuntimeException re) {
                resultList.add(processExceptionWithMessage(dto, re.getMessage()));
                continue;
            } catch (Exception e) {
                resultList.add(processExceptionWithMessage(dto, "不正确的推送地址"));
                continue;
            }

            try {
                if (!destinations.isEmpty()) {
                    applyService.push(dto, "", destinations);
                }
            } catch (Exception e) {
                resultList.add(processException(dto, e));
            }
        }
        return JsonV2.successOf(resultList);
    }

    /**
     * 推送编辑中的文件
     *
     * @param editingPushVo 需要推送的内容
     * @return 推送结果
     */
    @RequestMapping(value = "/editPush", method = RequestMethod.POST)
    @ResponseBody
    public Object editingPush(@RequestBody EditingPushVo editingPushVo) throws IllegalAccessException {
        CandidateDTO dto = editingPushVo.getCandidateDTO();

        logger.info("push with {}", editingPushVo);

        ConfigMeta meta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
        CandidateSnapshot snapshot = configService.findLastCandidateSnapshot(meta);

        if (dto.getEditVersion() < snapshot.getEditVersion()) {
            return new JsonV2<>(-1, "非最新版本，请刷新后重试!", null);
        }

        dto.setData(Strings.nullToEmpty(dto.getData()).trim());

        if (!Objects.equal(dto.getData(), snapshot.getData().trim())) {
            if ((actionController.apply(dto, false) instanceof JsonV2)) {
                return new JsonV2<>(-1, "保存失败，无法推送!", null);
            } else {
                dto.setEditVersion(dto.getEditVersion() + 1);
            }
        }

        Monitor.EDIT_PUSH_STATICS.inc();
        List<PushItemWithHostName> destinations;

        //  这里是以前的逻辑，应该需要修改
        try {
            destinations = getPushItemWithHostNames(new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile()),
                            editingPushVo.getPushItems());
        } catch (RuntimeException re) {
            throw new IllegalAccessException(re.getMessage());
        } catch (Exception e) {
            throw new IllegalAccessException("不正确的推送地址");
        }

        return JsonV2.successOf(pushEditFile(dto, destinations));
    }

    /**
     * 推送编辑后未审核的文件
     *
     * @param pushDto 需要推送的内容的信息
     * @return 推送结果
     */
    @RequestMapping(value = "/editPushWithMeta", method = RequestMethod.POST)
    @ResponseBody
    public Object editPush(@RequestBody PushDto pushDto) {
        CandidateDTO dto = new CandidateDTO(pushDto.getGroup(), pushDto.getDataId(), pushDto.getProfile(), "");
        dto.setEditVersion(pushDto.getEditVersion());

        Monitor.EDIT_PUSH_STATICS.inc();
        List<PushItemWithHostName> destinations;

        try {
            destinations = getPushItemWithHostNames(new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile()),
                            pushDto.getPushItems());
        } catch (RuntimeException re) {
            throw new IllegalArgumentException(re.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("不正确的推送地址");
        }

        return JsonV2.successOf(pushEditFile(dto, destinations));

    }

    private Object pushEditFile(CandidateDTO dto, List<PushItemWithHostName> destinations) {
        try {
            if (!destinations.isEmpty()) {
                applyService.pushEditing(dto, "editPush", destinations);
            }
            return true;
        } catch (Exception e) {
            return handleException(dto, e);
        }
    }

    /**
     * 批量推送结果
     *
     * @param pushVo 推送的内容
     * @return 查询结果
     */
    @RequestMapping(value = "/batchPushToMachineResult", method = RequestMethod.POST)
    @ResponseBody
    public Object batchPushResult(@RequestBody BatchPushVo pushVo) {

        Map<String, Map<String, Object>> resultMap = Maps.newHashMap();

        for (PushSimpleVo pushSimpleVo : pushVo.getPushSimpleVos()) {

            Map<String, Object> currentResult = Maps.newHashMap();

            ConfigMeta meta = new ConfigMeta(pushSimpleVo.getGroup(), pushSimpleVo.getDataId(),
                    pushSimpleVo.getProfile());
            checkLegalMeta(meta);
            try {
                currentResult.put("group", pushSimpleVo.getGroup());
                currentResult.put("profile", pushSimpleVo.getProfile());
                currentResult.put("dataId", pushSimpleVo.getDataId());
                currentResult.put("currentStatus",
                        consumerService.getConsumerLogs(meta, ConsumerService.ALLOW_PUSH_TYPE).stream().filter(new IpPortFilter(pushVo.getPushItems())::apply).collect(Collectors.toList()));

                resultMap.put(pushSimpleVo.getDataId(), currentResult);
            } catch (Exception e) {
                logger.error("get push result error with pushDto {}", pushSimpleVo, e);
                currentResult.putAll(processExceptionWithMessage(
                        new CandidateDTO(pushSimpleVo.getGroup(), pushSimpleVo.getDataId(), pushSimpleVo.getProfile(),
                                ""), "不正确的推送地址"));
                resultMap.put(pushSimpleVo.getDataId(), currentResult);
            }
        }
        return JsonV2.successOf(resultMap);
    }

    @RequestMapping(value = "/editPush/checkProfile", method = RequestMethod.GET)
    @ResponseBody
    public Object checkEditPushWithProfile(String group, String profile) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(group), "group 不能为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(profile), "profile 不能为空");

        return pushHistoryService.getUnRollbackPushList(group, profile);
    }

    @RequestMapping(value = "/editPush/checkOneFile", method = RequestMethod.GET)
    @ResponseBody
    public Object checkEditPushWithData(String group, String profile, String dataId) {
        ConfigMeta configMeta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(configMeta);
        List<EditPushHostVO> ips = pushHistoryService.getUnRollBackIPandPort(configMeta);
        Map<String, Object> result = Maps.newHashMap();
        if (ips.size() != 0) {
            result.put("unRollBack", true);
            result.put("machine", ips);
        } else {
            result.put("unRollBack", false);
        }

        return JsonV2.successOf(result);
    }

    @RequestMapping(value = "/editPush/rollBack", method = RequestMethod.POST)
    @ResponseBody
    public Object checkEditPushWithData(@RequestBody CandidateDTO dto) {
        Preconditions.checkNotNull(dto, "dto 不能为空");
        CandidateSnapshot snapshot = configService
                .getCandidateDetails(dto.getGroup(), dto.getDataId(), dto.getProfile(), dto.getEditVersion());

        if (snapshot.getStatus().equals(StatusType.PASSED)) {
            return new JsonV2<>(-1, "已通过的文件无法回滚，请回退审核或者直接发布!", null);
        }

        applyService.rollBackEditPush(dto);

        return JsonV2.success();
    }

    @RequestMapping(value = "/getBastionAddress")
    @ResponseBody
    public Object getBastionAddress(String group, String dataId, String profile) {
        ConfigMeta meta = new ConfigMeta(group, dataId, profile);
        checkLegalMeta(meta);
        try {
                ListenableFuture<Set<ClientData>> data = listeningClientsService.getListeningClientsData(meta, false);
                Set<ClientData> consumerLogs = data.get(LIST_CLIENTS_TIMEOUT, TimeUnit.SECONDS);
                // 线上环境只返回堡垒机地址，测试环境返回所有可推送地址
                if (ProfileUtil.affectProd(profile)) {
                    final Set<String> bastionAddresses = Sets.newHashSet(bastionAddressService.getBastionAddress(meta.getGroup()));
                    return consumerLogs.stream().filter(log -> bastionAddresses.contains(log.getIp())).collect(Collectors.toList());
                }
                return JsonV2.successOf(consumerLogs);
        } catch (Exception e) {
            logger.error("get addresses error with group={}, dataId={}, profile={}", group, dataId, profile, e);
            return JsonV2.failOf("系统发生异常，请与管理员联系！");
        }
    }

    private List<PushItemWithHostName> getPushItemWithHostNames(ConfigMeta meta, List<PushItemDto> pushItems) {
        if (pushItems.size() <= 0) {
            return Collections.emptyList();
        }
        Map<String, Long> ipAndVersions = fixedConsumerVersionService.findIpAndVersions(meta);
        List<PushItemWithHostName> pushItemWithHostNames = Lists.newArrayListWithCapacity(pushItems.size());
        for (PushItemDto pushItem : pushItems) {
            String ipAndPort = pushItem.getIpAndPort();
            if (!Strings.isNullOrEmpty(ipAndPort)) {
                List<String> iterable = Lists.newArrayList(COLON_SPLITTER.split(ipAndPort));
                Preconditions.checkArgument(iterable.size() == IP_PORT_PAIR_LIMIT);
                String ip = iterable.get(0);
                Long version = ipAndVersions.get(ip);
                if (version != null && version > 0) {
                    logger.info("consumer fix version, do not push, meta={}, ip={}, version={}", meta, ip, version);
                    throw new RuntimeException("推送失败，机器版本已锁定ip=" + ip);
                }
                int port = Integer.parseInt(iterable.get(1));
                Preconditions.checkArgument(isValidIp(ip));
                Preconditions.checkArgument(AddressUtil.canPushPort(port));
                pushItemWithHostNames.add(new PushItemWithHostName(ip, port, pushItem.getSourceGroup(), pushItem
                        .getSourceDataId(), pushItem.getSourceProfile()));
            }
        }
        return pushItemWithHostNames;
    }

    public static class IpPortFilter implements Predicate<ConfigUsedLog> {

        private final Set<String> ipAndPorts;

        IpPortFilter(List<PushItemDto> pushItems) {
            ipAndPorts = Sets.newHashSetWithExpectedSize(pushItems.size());
            for (PushItemDto pushItem : pushItems) {
                ipAndPorts.add(pushItem.getIpAndPort());
            }
        }

        @Override
        public boolean apply(ConfigUsedLog input) {
            return ipAndPorts.contains(input.getIp() + ":" + input.getPort());
        }
    }

    private static final String IP_PATTERN_STR = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}"
            + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    private static final Pattern IP_PATTERN = Pattern.compile(IP_PATTERN_STR);

    private static boolean isValidIp(String ip) {
        return IP_PATTERN.matcher(ip).find();
    }
}
