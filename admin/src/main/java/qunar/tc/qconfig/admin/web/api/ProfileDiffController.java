package qunar.tc.qconfig.admin.web.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import qunar.tc.qconfig.admin.cloud.vo.FileDiffVo;
import qunar.tc.qconfig.admin.cloud.vo.PropertyDiffVo;
import qunar.tc.qconfig.admin.model.DiffCount;
import qunar.tc.qconfig.admin.model.DiffResult;
import qunar.tc.qconfig.admin.model.FileDiffInfo;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.service.ConfigService;
import qunar.tc.qconfig.admin.support.CheckUtil;
import qunar.tc.qconfig.admin.support.Differ;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.common.util.FileChecker;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/qconfig/api")
public class ProfileDiffController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(ProfileDiffController.class);

    private final static int CODE_MODIFIED = 0;
    private final static int CODE_ADDED = -2;
    private final static int CODE_DELETED = -1;

    @Resource
    private ConfigService configService;

    @RequestMapping("/diff/app")
    @ResponseBody
    public Object getAppDiff(@RequestBody DiffRequest diffRequest) {
        logger.info("diff files between profiles: {}", diffRequest);
        String group = diffRequest.getAppcode();
        checkLegalGroup(group);
        diffRequest.setFromProfile(getCorrectProfile(diffRequest.getFromProfile(), Environment.BETA.defaultProfile()));
        diffRequest.setToProfile(getCorrectProfile(diffRequest.getToProfile(), Environment.PROD.defaultProfile()));
        CheckUtil.checkLegalProfile(diffRequest.getFromProfile());
        CheckUtil.checkLegalProfile(diffRequest.getToProfile());
        Monitor.API_DIFF_PROFILE_STATICS.inc();
        List<FileDiffInfo> fileDiffInfoList = configService.diffProfileWithUpperLevel(group, diffRequest.getFromProfile(), diffRequest.getToProfile());
        DiffResult<List<FileDiffVo>> diffResult = transform(fileDiffInfoList);
        Map<String, Object> diffInfoVo = Maps.newHashMap();
        diffInfoVo.put("diffRequest", diffRequest);
        diffInfoVo.put("diffResult", diffResult);
        return JsonV2.successOf(diffInfoVo);
    }

    private String getCorrectProfile(String inputProfile, String defaultProfile) {
        if (StringUtils.isBlank(inputProfile)) {
            return defaultProfile;
        }
        if (!inputProfile.contains(":")) {
            return inputProfile + ":";
        }
        return inputProfile;
    }

    private DiffResult<List<FileDiffVo>> transform(List<FileDiffInfo> fileDiffInfoList) {
        List<FileDiffVo> fileDiffVos = Lists.newArrayListWithExpectedSize(fileDiffInfoList.size());
        int added = 0, deleted = 0, modified = 0;
        for (FileDiffInfo diffInfo : fileDiffInfoList) {
            FileDiffVo fileDiffVo = new FileDiffVo();
            fileDiffVo.setDataId(diffInfo.getName());
            fileDiffVo.setError(diffInfo.getError());
            fileDiffVo.setMetaFrom(diffInfo.getMetaFrom());
            fileDiffVo.setMetaTo(diffInfo.getMetaTo());
            // 0:l/r环境(以及上级环境)都有该文件, -1:l有r无, -2:l无r有
            int existCode = 0;
            if (diffInfo.isLExist() && diffInfo.isRExist()) {
                existCode = CODE_MODIFIED;
                modified++;
            } else if (!diffInfo.isLExist()) {
                existCode = CODE_ADDED;
                added++;
            } else if (!diffInfo.isRExist()) {
                existCode = CODE_DELETED;
                deleted++;
            }
            fileDiffVo.setExist(existCode);
            if (FileChecker.isPropertiesFile(diffInfo.getName())) {
                DiffResult<List<Differ.PropertyDiffDto>> propertyDiffResult = (DiffResult<List<Differ.PropertyDiffDto>>)diffInfo.getDiff();
                DiffResult<List<PropertyDiffVo>> keyDiffResult = trans(propertyDiffResult);
                fileDiffVo.setKeyDiffResult(keyDiffResult);
            }
            fileDiffVos.add(fileDiffVo);
        }
        DiffCount count = new DiffCount(added, deleted, modified);
        return new DiffResult<>(count, fileDiffVos);
    }

    private DiffResult<List<PropertyDiffVo>> trans(DiffResult<List<Differ.PropertyDiffDto>> propertyDiffResult) {
        List<PropertyDiffVo> diffVos = Lists.newArrayListWithExpectedSize(propertyDiffResult.getResult().size());
        for (Differ.PropertyDiffDto diffDto : propertyDiffResult.getResult()) {
            diffVos.add(transPropertyDiffVo(diffDto));
        }
        return new DiffResult<>(propertyDiffResult.getDiffCount(), diffVos);
    }

    private PropertyDiffVo transPropertyDiffVo(Differ.PropertyDiffDto diffDto) {
        int existCode = 0;
        switch (diffDto.getType()) {
            case MODIFIED:
                existCode = CODE_MODIFIED;
                break;
            case ADDED:
                existCode = CODE_ADDED;
                break;
            case DELETED:
                existCode = CODE_DELETED;
                break;
        }
        return new PropertyDiffVo(diffDto.getKey(), existCode, diffDto.getlValue(), diffDto.getrValue());
    }


    static class DiffRequest {
        private String appcode;
        private String fromProfile;
        private String toProfile;

        public String getAppcode() {
            return appcode;
        }

        public void setAppcode(String appcode) {
            this.appcode = appcode;
        }

        public String getFromProfile() {
            return fromProfile;
        }

        public void setFromProfile(String fromProfile) {
            this.fromProfile = fromProfile;
        }

        public String getToProfile() {
            return toProfile;
        }

        public void setToProfile(String toProfile) {
            this.toProfile = toProfile;
        }
    }
}
