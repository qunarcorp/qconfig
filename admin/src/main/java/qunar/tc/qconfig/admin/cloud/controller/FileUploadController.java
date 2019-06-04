package qunar.tc.qconfig.admin.cloud.controller;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.admin.web.controller.AbstractControllerHelper;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.common.util.FileChecker;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping(value = "/qconfig/file")
public class FileUploadController extends AbstractControllerHelper {

    @Resource
    private ActionController actionController;

    private final long FILE_MAX_SIZE = 512 * 1024; //512K
    private final String FILE_MAX_SIZE_STRING = "512K";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public Object fileUpload(@RequestParam("group") String group,
                             @RequestParam("profile") String profile,
                             MultipartHttpServletRequest request) {
        checkLegalGroup(group);
        checkLegalProfile(profile);

        Monitor.MULTI_APPLY_STATICS.inc();
        Collection<MultipartFile> files = request.getFileMap().values();
        List<FileApplyResult> results = Lists.newArrayListWithCapacity(files.size());
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            try {
                if (file.getSize() > FILE_MAX_SIZE) {
                    results.add(FileApplyResult.fail(filename, ResultStatus.FILE_TOO_BIG.code(), "文件大小不能超过" + FILE_MAX_SIZE_STRING));
                    continue;
                }
                try {
                    checkLegalDataId(filename);
                    FileChecker.checkName(filename);
                } catch (Exception e) {
                    results.add(FileApplyResult.fail(filename, ResultStatus.NORMAL_ERROR.code(), e.getMessage()));
                    continue;
                }
                if (FileChecker.isTemplateFile(filename)) {
                    results.add(FileApplyResult.fail(filename, ResultStatus.ILLEGAL_FILE.code(), "暂不支持上传模版文件"));
                    continue;
                }

                logger.info("upload and commit file, group={}, profile={}, data id={}", group, profile, filename);
                String data = getFileData(file);
                CandidateDTO dto = new CandidateDTO(group, file.getOriginalFilename(), profile, 0, 0, StatusType.PENDING, data);

                Object commitResult = actionController.apply(dto.copy(), false);
                if (commitResult instanceof JsonV2) {
                    JsonV2 errJson = (JsonV2) commitResult;
                    if (errJson.status == ResultStatus.CONFIG_ALREADY_DELETED.code()) {
                        Candidate deleted = (Candidate) errJson.data;
                        dto.setBasedVersion(deleted.getBasedVersion());
                        dto.setEditVersion(deleted.getEditVersion());
                        //tole copy一份
                        actionController.forceApply(dto.copy());
//                        results.add(failOf(filename, CONFIG_ALREADY_DELETED.code(), "该文件已被删除！"));
                        results.add(FileApplyResult.success(filename));
                    } else {
                        results.add(FileApplyResult.fail(filename, ResultStatus.NORMAL_ERROR.code(), errJson.message));
                    }
                } else {
                    results.add(FileApplyResult.success(file.getOriginalFilename()));
                }
            } catch (Exception e) {
                logger.warn("upload and commit file failOf, group={}, profile={}, data id={}", group, profile, filename, e);
                results.add(FileApplyResult.fail(filename, ResultStatus.NORMAL_ERROR.code(), e.getMessage()));
            }
        }
        return JsonV2.successOf(results);
    }

    public static class FileApplyResult {
        private String filename;
        private String errMsg;
        private int statusCode;

        public static FileApplyResult success(String filename) {
            final FileApplyResult applyResult = new FileApplyResult();
            applyResult.filename = filename;
            applyResult.statusCode = ResultStatus.SUCCESS.code();
            return applyResult;
        }

        public static FileApplyResult fail(String filename, int statusCode, String errMsg) {
            final FileApplyResult applyResult = new FileApplyResult();
            applyResult.filename = filename;
            applyResult.statusCode = statusCode;
            applyResult.errMsg = errMsg;
            return applyResult;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getErrMsg() {
            return errMsg;
        }

        public void setErrMsg(String errMsg) {
            this.errMsg = errMsg;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }
    }

    public enum ResultStatus {
        SUCCESS(0),
        NORMAL_ERROR(2),
        ILLEGAL_FILE(5),
        CONFIG_ALREADY_DELETED(20),
        VALIDATE_ERROR(100),
        FILE_TOO_BIG(101);

        private final int code;

        ResultStatus(final int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }
}
