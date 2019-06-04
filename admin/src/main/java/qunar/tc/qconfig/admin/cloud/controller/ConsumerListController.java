package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import qunar.tc.qconfig.admin.cloud.vo.ConsumerListOfCurrentMachineVo;
import qunar.tc.qconfig.admin.dao.ConfigUsedLogDao;
import qunar.tc.qconfig.admin.model.ConfigUsedLog;
import qunar.tc.qconfig.admin.service.ConsumerService;
import qunar.tc.qconfig.admin.service.FixedConsumerVersionService;
import qunar.tc.qconfig.admin.service.ListeningClientsService;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.ProfileUtil;
import qunar.tc.qconfig.servercommon.bean.ClientData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/qconfig/consumer")
public class ConsumerListController extends AbstractControllerHelper {

    private final static int LIST_CLIENTS_TIMEOUT = 10;
    @Resource
    private ConsumerService consumerService;

    @Resource
    private ConfigUsedLogDao configUsedLogDao;

    @Resource
    private FixedConsumerVersionService fixedConsumerVersionService;

    @Resource
    private ListeningClientsService listeningClientsService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/machine/list", method = RequestMethod.GET)
    @ResponseBody
    public JsonV2<?> list(@RequestParam("group") String group,
                          @RequestParam("profile") String profile) {
        if (Strings.isNullOrEmpty(group)) {
            return JsonV2.failOf("group 不能为空");
        }

        if (!ProfileUtil.legalProfile(profile)) {
            return JsonV2.failOf("无效的 profile", profile);
        }

        try {
            Map<String, List<ConfigUsedLog>> appConsumerLogs = consumerService.getAppConsumerLogs(
                    group, Environment.fromProfile(profile).defaultProfile().toLowerCase());

            ConsumerListOfCurrentMachineVo consumerListVo = new ConsumerListOfCurrentMachineVo();
            consumerListVo.setGroup(group);
            consumerListVo.setEnv(Environment.fromProfile(profile).text());
            consumerListVo.setHost2ConfigUsedLogs(appConsumerLogs);
            return JsonV2.successOf("", consumerListVo);
        } catch (Exception e) {
            logger.error("get application files list error, group:{}, profile:{}", group, profile, e);
            return JsonV2.failOf("获取consumer列表异常");
        }
    }

    @RequestMapping(value = "/file/list", method = RequestMethod.GET)
    @ResponseBody
    public Object viewClientPullLog(@RequestParam("group") String group,
                                    @RequestParam("profile") String profile,
                                    @RequestParam("dataId") String dataId) {
        checkLegalMeta(group, profile, dataId);
        try {
            List<ConfigUsedLog> configUsedLogs = configUsedLogDao
                    .select(group, dataId, profile);
            ConfigMeta configMeta = new ConfigMeta(group, dataId, profile);
            return fixedConsumerVersionService.addFixedVersion(configMeta, configUsedLogs);
        } catch (Exception e) {
            logger.error("view client pull log error, group:{}, profile:{}, dataId:{}",
                    group, profile, dataId, e);
            return JsonV2.failOf("查看使用该文件的客户端列表异常");
        }
    }

    @RequestMapping(value = "/clients/list", method = RequestMethod.GET)
    @ResponseBody
    public Object viewClientsData(@RequestParam("group") String group,
                                  @RequestParam("profile") String profile,
                                  @RequestParam("dataId") String dataId) {
        checkLegalMeta(group, profile, dataId);
        try {
            ConfigMeta configMeta = new ConfigMeta(group, dataId, profile);
            ListenableFuture<Set<ClientData>> data = listeningClientsService.getListeningClientsData(configMeta, false);
            return JsonV2.successOf(data.get(LIST_CLIENTS_TIMEOUT, TimeUnit.SECONDS));
        } catch (Exception e) {
            logger.error("view client list log error, group:{}, profile:{}, dataId:{}", group, profile, dataId, e);
            return JsonV2.failOf("查看使用该文件的客户端列表异常");
        }
    }

    @RequestMapping(value = "/clients/pushresult", method = RequestMethod.GET)
    @ResponseBody
    public Object pushResult(@RequestParam("group") String group,
                             @RequestParam("profile") String profile,
                             @RequestParam("dataId") String dataId,
                             @RequestParam("editVersion") Long editVersion,
                             @RequestParam("ipports") List<String> ipports) {
        checkLegalMeta(group, profile, dataId);
        try {
            ConfigMeta configMeta = new ConfigMeta(group, dataId, profile);
            return listeningClientsService.getListeningPushStatus(configMeta, editVersion, ipports);
        } catch (Exception e) {
            logger.error("check push result error, group:{}, profile:{}, dataId:{}", group, profile, dataId, e);
            return JsonV2.failOf("校验推送结果失败！");
        }
    }
}
