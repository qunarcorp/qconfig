package qunar.tc.qconfig.admin.cloud.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.cloud.vo.ConfigMetaVersion;
import qunar.tc.qconfig.admin.cloud.vo.DiffInfosVo;
import qunar.tc.qconfig.admin.dao.ConfigOpLogDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.exception.IllegalTemplateException;
import qunar.tc.qconfig.admin.model.DiffCount;
import qunar.tc.qconfig.admin.model.DiffResult;
import qunar.tc.qconfig.admin.model.InterceptStrategy;
import qunar.tc.qconfig.admin.model.KeyValuePair;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.service.DiffService;
import qunar.tc.qconfig.admin.service.FileTemplateService;
import qunar.tc.qconfig.admin.service.PublishKeyInterceptStrategyService;
import qunar.tc.qconfig.admin.service.template.TemplateUtils;
import qunar.tc.qconfig.admin.support.AdminConstants;
import qunar.tc.qconfig.admin.support.DiffUtil;
import qunar.tc.qconfig.admin.support.Differ;
import qunar.tc.qconfig.admin.support.JsonUtil;
import qunar.tc.qconfig.admin.web.bean.DiffResultView;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.FileChecker;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/qconfig/diff")
public class  ContentDiffController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(ContentDiffController.class);

    @Resource
    private ConfigService configService;

    @Resource
    private ConfigOpLogDao configOpLogDao;

    @Resource
    private FileTemplateService fileTemplateService;

    @Resource
    private PublishKeyInterceptStrategyService interceptStrategyService;

    @Resource
    private Differ differ;

    @Resource
    private DiffService diffService;

    @Value("${profileConfigLog.showLength}")
    private int profileConfigLogLength;

    @Value("${profileRefLog.showLength}")
    private int profileRefLogLength;

    @Value("${configLog.showLength}")
    private int configLogLength;

    @RequestMapping(value = "/applyInfo", method = RequestMethod.POST)
    @ResponseBody
    public Object applyInfo(@RequestBody CandidateDTO dto) {
        ConfigMeta meta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
        checkLegalMeta(meta);
        String data = dto.getData();
        if (FileChecker.isTemplateFile(dto.getDataId())) {
            Optional<String> newData;
            try {
                newData = fileTemplateService.processTemplateValue(dto);
            } catch (IllegalTemplateException e) {
                return JsonV2.failOf("模版" + e.getTemplate() + "不存在");
            }
            if (newData.isPresent()) {
                data = newData.get();
            }
        }
        long basedVersion = dto.getEditVersion();

        try {
            return JsonV2.successOf(wrapDiffAlert(getCompareInfo(meta, data, basedVersion)));
        } catch (Exception e) {
            logger.error("get apply compare info error, {}", dto, e);
            return JsonV2.failOf("系统发生异常，请与管理员联系！");
        }
    }

    // 点击发布按钮
    @RequestMapping(value = "/publishInfo", method = RequestMethod.POST)
    @ResponseBody
    public Object publishInfo(@RequestBody final CandidateDTO dto) {
        ConfigMeta meta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
        checkLegalMeta(meta);

        try {
            return JsonV2.successOf(getDiffInfosVo(dto, meta));
        } catch (Exception e) {
            logger.error("get normal compare info error, {}", dto, e);
            throw new RuntimeException("系统发生异常，请与管理员联系！", e);
        }
    }

    private DiffInfosVo getDiffInfosVo(@RequestBody CandidateDTO dto, ConfigMeta meta) {
        CandidateSnapshot candidateDetails = configService.getCandidateDetails(dto.getGroup(), dto.getDataId(),
                dto.getProfile(), dto.getEditVersion());
        String data = candidateDetails.getData();
        long basedVersion = candidateDetails.getBasedVersion();

        DiffInfosVo compareInfo = wrapDiffAlert(getCompareInfo(meta, data, basedVersion));

        if (candidateDetails.getStatus() == StatusType.PASSED
                && Environment.fromProfile(meta.getProfile()).isProd()
                && FileChecker.isPropertiesFile(meta.getDataId())) {
            InterceptStrategy strategy = interceptStrategyService.getStrategy(meta.getGroup());
            if (needIntercept(strategy, compareInfo.getDiffs())) {
                compareInfo.setIntercept(true);
            }
        }
        return compareInfo;
    }

    // 点击发布按钮
    @RequestMapping(value = "/batchPublishInfo", method = RequestMethod.POST)
    @ResponseBody
    public Object batchPublishInfo(@RequestBody final List<CandidateDTO> dtos) {
        Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(dtos.size());

        for (CandidateDTO dto : dtos) {
            ConfigMeta meta = new ConfigMeta(dto.getGroup(), dto.getDataId(), dto.getProfile());
            checkLegalMeta(meta);

            try {
                DiffInfosVo compareInfo = getDiffInfosVo(dto, meta);
                resultMap.put(dto.getDataId(), compareInfo);
            } catch (Exception e) {
                logger.error("get normal compare info error, {}", dto, e);
                resultMap.put(dto.getDataId(), "diff出错！请联系管理员");
            }
        }
        return JsonV2.successOf(resultMap);
    }

    //tole 历史版本diff接口
    @RequestMapping("/diffHistoryVersion")
    @ResponseBody
    public Object diff(@RequestBody CandidateDTO candidate) {
        Monitor.HISTORY_DIFF_STATICS.inc();
        CandidateSnapshot snapshot = configService.getCandidateDetails(
                candidate.getGroup(),
                candidate.getDataId(),
                candidate.getProfile(),
                candidate.getEditVersion());

        boolean isTemplateFile = FileChecker.isTemplateFile(candidate.getDataId());

        if (!isTemplateFile && FileChecker.isJsonFile(candidate.getDataId())) {
            return JsonV2.successOf(jsonDiff(candidate.getData(), snapshot.getData()));
        }

        Map<String, Object> map = Maps.newHashMapWithExpectedSize(3);
        String snapshotData = configService.templateDataLongToStr(candidate.getGroup(), candidate.getDataId(), snapshot.getData());
        DiffResult<String> diffResult = differ.diffToHtml(snapshotData, candidate.getData(), candidate.getDataId());
        map.put("origData", snapshot.getData());
        map.put("diffData", diffResult.getResult());
        map.put("diffResultText", DiffUtil.diffText(candidate.getDataId(), diffResult.getDiffCount(), isTemplateFile));
        //tole 整理下返回值只留这个diff，其他去掉
        Differ.MixedDiffResult<String, String> mixedDiff = diffService.getHtmlMixedDiff(snapshotData, candidate.getData(), candidate.getDataId());
        map.put("diff", DiffUtil.wrapDiffVo(candidate.getDataId(), mixedDiff));
        return JsonV2.successOf(map);
    }


    private Object jsonDiff(String lhs, String rhs) {
        Map<String, Object> map = Maps.newHashMap();
        Optional<JsonNode> snapshotJson = JsonUtil.read(rhs);
        if (!snapshotJson.isPresent()) {
            return new JsonV2<>(AdminConstants.PARSE_JSON_ERROR_CODE, "源json解析出错", null);
        }
        Optional<JsonNode> candidateJson = JsonUtil.read(lhs);
        if (!candidateJson.isPresent()) {
            return new JsonV2<>(AdminConstants.PARSE_JSON_ERROR_CODE, "目标json解析出错", null);
        }
        map.put("origData", snapshotJson.get());
        map.put("newData", candidateJson.get());
        return map;
    }


    // html格式的diff
    private DiffInfosVo getCompareInfo(ConfigMeta meta, String data, long basedVersion) {
        boolean isTemplateFile = FileChecker.isTemplateFile(meta.getDataId());
        if (FileChecker.isJsonFile(meta.getDataId())) {
            Optional<JsonNode> dataJson = JsonUtil.read(data);
            if (!dataJson.isPresent()) {
                return new DiffInfosVo(meta, data, null, null, Lists.<Map.Entry<VersionData<ConfigMeta>, JsonNode>>newArrayList(), configOpLogDao.selectRecent(meta,
                        basedVersion, configLogLength));
            }
            Map.Entry<VersionData<ConfigMeta>, JsonNode> lastPublishDiff = configService.getJsonDiffToLastPublish(meta, data);
            List<Map.Entry<VersionData<ConfigMeta>, JsonNode>> relativeDiffs = configService.getJsonProdBetaOrBetaProdDiffs(meta, data);
            List<Map.Entry<VersionData<ConfigMeta>, JsonNode>> diffs = Lists.newArrayListWithCapacity(relativeDiffs.size() + 1);
            diffs.add(lastPublishDiff);
            diffs.addAll(relativeDiffs);
            return new DiffInfosVo(meta, data, dataJson.get(), null, diffs,
                    configOpLogDao.selectRecent(meta, basedVersion, configLogLength));
        } else {
            String templateDetail = "";
            if (isTemplateFile) {
                Optional<String> detail = fileTemplateService.getTemplateDetailByFile(meta.getGroup(), meta.getDataId());
                if (detail.isPresent()) {
                    templateDetail = detail.get();
                    Optional<String> optional = TemplateUtils.processTimeLongToStr(meta.getDataId(), data, templateDetail);
                    if (optional.isPresent()) {
                        data = optional.get();
                    }
                }
            }

            Map.Entry<VersionData<ConfigMeta>, Differ.MixedDiffResult<String, String>> lastPublishDiff = diffService.getHtmlMixedDiffToLastPublish(meta, data);
            List<Map.Entry<VersionData<ConfigMeta>, Differ.MixedDiffResult<String, String>>> relativeDiffs = diffService.getHtmlMixedDiffToRelativeProfile(meta, data);
            List<Map.Entry<VersionData<ConfigMeta>, DiffResultView>> diffs = Lists.newArrayListWithCapacity(relativeDiffs.size() + 1);
            diffs.add(diffVo(lastPublishDiff));
            for (Map.Entry<VersionData<ConfigMeta>, Differ.MixedDiffResult<String, String>> diff : relativeDiffs) {
                diffs.add(diffVo(diff));
            }
            return new DiffInfosVo(meta, data, null, templateDetail, diffs,
                    configOpLogDao.selectRecent(meta, basedVersion, configLogLength));
        }
    }

    private DiffInfosVo wrapDiffAlert(DiffInfosVo diffInfosVo) {
        List<KeyValuePair<ConfigMetaVersion, Object>> diffs = diffInfosVo.getDiffs();
        if (diffs.size() <= 1) {
            return diffInfosVo;
        }
        Environment env = Environment.fromProfile(diffInfosVo.getProfile());
        if (!env.isProd() && !env.isResources()) {
            // show alert in prod/resources only
            return diffInfosVo;
        }
        KeyValuePair<ConfigMetaVersion, Object> relativeDiff = diffs.get(1);
        Environment relativeEnv = Environment.fromProfile(relativeDiff.getKey().getProfile());
        if (FileChecker.isJsonFile(diffInfosVo.getDataId())) {
            JsonNode jsonNode = (JsonNode)relativeDiff.getValue();
            if (jsonNode == null) {
                diffInfosVo.setShowDiffAlert(true);
                diffInfosVo.setDiffAlertText(String.format("该文件在%s环境中尚未发布过, 请仔细确认!", relativeEnv.env()));
            }

        } else {
            DiffResultView diffResultView = (DiffResultView)relativeDiff.getValue();
            if (diffResultView == null) {
                diffInfosVo.setShowDiffAlert(true);
                diffInfosVo.setDiffAlertText(String.format("该文件在%s环境中尚未发布过, 请仔细确认!", relativeEnv.env()));
                return diffInfosVo;
            }
            // properties检查key set是否一致
            if (FileChecker.isPropertiesFile(diffInfosVo.getDataId())) {
                DiffCount diffCount = diffResultView.getDiffCount();
                if (diffCount.getAdd() != 0 || diffCount.getDelete() != 0) {
                    diffInfosVo.setShowDiffAlert(true);
                    StringBuilder stringBuilder = new StringBuilder("该文件对比").append(relativeEnv.env()).append("环境");
                    if (diffCount.getAdd() != 0) {
                        stringBuilder.append("新增了").append(diffCount.getAdd()).append("个key, ");
                    }
                    if (diffCount.getDelete() != 0) {
                        stringBuilder.append("删除了").append(diffCount.getDelete()).append("个key, ");
                    }
                    stringBuilder.append("请仔细确认!");
                    diffInfosVo.setDiffAlertText(stringBuilder.toString());
                }
            }
        }
        return diffInfosVo;
    }

    //origin
    private Map.Entry<VersionData<ConfigMeta>, DiffResultView> diffVo(Map.Entry<VersionData<ConfigMeta>, Differ.MixedDiffResult<String, String>> diff) {
        Differ.MixedDiffResult<String, String> value = diff.getValue();
        VersionData<ConfigMeta> versionMeta = diff.getKey();
        String name = versionMeta.getData().getDataId();
        return Maps.immutableEntry(versionMeta, DiffUtil.wrapDiffVo(name, value));
    }

    private boolean needIntercept(InterceptStrategy strategy, List<KeyValuePair<ConfigMetaVersion, Object>> diffs) {
        if (strategy == InterceptStrategy.NO) {
            return false;
        } else {
            boolean betaHasProdNotHas = false;
            boolean betaNotHasProdHas = false;
            // 只有和上次发布的diff，即没有beta文件
            if (diffs.size() <= 1 || diffs.get(1) == null || diffs.get(1).getValue() == null) {
                betaNotHasProdHas = true;
            } else {
                DiffCount diffCount = ((DiffResultView) diffs.get(1).getValue()).getDiffCount();
                if (diffCount.getAdd() > 0) {
                    betaNotHasProdHas = true;
                }
                if (diffCount.getDelete() > 0) {
                    betaHasProdNotHas = true;
                }
            }
            if (strategy == InterceptStrategy.ALL) {
                return betaHasProdNotHas || betaNotHasProdHas;
            } else if (strategy == InterceptStrategy.BETA_HAS_PROD_NOT_HAS) {
                return betaHasProdNotHas;
            } else if (strategy == InterceptStrategy.BETA_NOT_HAS_PROD_HAS) {
                return betaNotHasProdHas;
            } else {
                throw new RuntimeException("illegal intercept strategy");
            }
        }
    }

}
