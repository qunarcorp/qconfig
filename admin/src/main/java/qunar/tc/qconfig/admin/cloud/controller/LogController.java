package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import qunar.tc.qconfig.admin.cloud.vo.FileMetaRequest;
import qunar.tc.qconfig.admin.cloud.vo.ProfileRequest;
import qunar.tc.qconfig.admin.dao.ClientLogDao;
import qunar.tc.qconfig.admin.dao.ConfigOpLogDao;
import qunar.tc.qconfig.admin.dao.ConfigUsedLogDao;
import qunar.tc.qconfig.admin.dao.ReferenceLogDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.dto.ProfileOpLogDto;
import qunar.tc.qconfig.admin.model.ClientLog;
import qunar.tc.qconfig.admin.model.ConfigOpLog;
import qunar.tc.qconfig.admin.model.ConfigUsedLog;
import qunar.tc.qconfig.admin.model.ReferenceLog;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.FixedConsumerVersionService;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/qconfig/log")
public class LogController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(LogController.class);

    @Resource
    private ConfigService configService;

    @Resource
    private ConfigUsedLogDao configUsedLogDao;

    @Resource
    private ClientLogDao clientLogDao;

    @Resource
    private ConfigOpLogDao configOpLogDao;

    @Resource
    private ReferenceLogDao referenceLogDao;

    @Resource
    private FixedConsumerVersionService fixedConsumerVersionService;

    @Value("${configLog.showLength}")
    private int configLogLength;

    @Value("${profileConfigLog.showLength}")
    private int profileConfigLogLength;

    @Value("${profileRefLog.showLength}")
    private int profileRefLogLength;


    private static final Comparator<ProfileOpLogDto> TIME_COMPARATOR = Comparator.comparing(ProfileOpLogDto::getTime);

    //TODO 貌似已经不用了
    @RequestMapping(value = "/consumerPullLog", method = RequestMethod.POST)
    @ResponseBody
    public Object viewClientPullLog(@RequestBody ConfigMeta meta) {
        checkLegalMeta(meta);
        try {
            List<ConfigUsedLog> configUsedLogs = configUsedLogDao
                    .select(meta.getGroup(), meta.getDataId(), meta.getProfile());
            ConfigMeta configMeta = new ConfigMeta(meta.getGroup(), meta.getDataId(), meta.getProfile());
            return JsonV2.successOf(fixedConsumerVersionService.addFixedVersion(configMeta, configUsedLogs));
        } catch (RuntimeException e) {
            logger.error("view client pull log error, {}", meta, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    @RequestMapping(value = "/recentClientLogs", method = RequestMethod.POST)
    @ResponseBody
    public Object recentClientLogs(@RequestBody CandidateDTO candidateDTO) {
        checkLegalMeta(candidateDTO);
        return JsonV2.successOf(CLIENT_LOG_ORDERING.immutableSortedCopy(clientLogDao.selectRecent(candidateDTO.getGroup(),
                candidateDTO.getProfile(), candidateDTO.getDataId(), candidateDTO.getBasedVersion())));
    }

    @RequestMapping(value = "/fileOperationLog", method = RequestMethod.POST)
    @ResponseBody
    public Object viewFileOpLog(@RequestBody FileMetaRequest fileMeta) {
        ConfigMeta meta = transform(fileMeta);
        CandidateSnapshot candidateSnapshot = configService.currentEditSnapshot(meta);
        if (candidateSnapshot == null) {
            return JsonV2.successOf(ImmutableList.of());
        }
        long basedVersion = candidateSnapshot.getBasedVersion();
        return JsonV2.successOf(configOpLogDao.selectRecent(meta, basedVersion, configLogLength));
    }

    @RequestMapping(value = "/profileFileOpLog", method = RequestMethod.POST)
    @ResponseBody
    public Object profileConfigOpLog(@RequestBody ProfileRequest profileRequest) {
        String group = profileRequest.getGroup();
        String profile = profileRequest.getProfile();

        checkLegalGroup(group);
        checkLegalProfile(profile);

        try {
            List<ProfileOpLogDto> opLogDtos = Lists.newArrayList();
            List<ConfigOpLog> configOpLogs = configOpLogDao.selectRecent(group, profile, profileConfigLogLength);
            for (ConfigOpLog configOpLog : configOpLogs) {
                opLogDtos.add(new ProfileOpLogDto(configOpLog));
            }
            List<ReferenceLog> referenceLogs = referenceLogDao.selectRecent(group, profile, profileRefLogLength);
            for (ReferenceLog referenceLog : referenceLogs) {
                opLogDtos.add(new ProfileOpLogDto(referenceLog));
            }
            return JsonV2.successOf(Ordering.from(TIME_COMPARATOR).reverse().immutableSortedCopy(opLogDtos));
        } catch (RuntimeException e) {
            logger.error("get group config op log error, group=[{}], profile=[{}]", group, profile, e);
            throw e;
        }
    }

    private static final Ordering<ClientLog> CLIENT_LOG_ORDERING = new Ordering<ClientLog>() {
        @Override
        public int compare(ClientLog left, ClientLog right) {
            if (left == null || right == null) {
                throw new IllegalArgumentException("参数不能为空");
            }
            return ComparisonChain.start()
                    .compare(left.getTime(), right.getTime(), Ordering.natural().reverse())
                    .compare(left.getType(), right.getType(), Ordering.natural().reverse())
                    .result();
        }
    };


}
