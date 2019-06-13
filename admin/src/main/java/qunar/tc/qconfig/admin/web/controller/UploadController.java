package qunar.tc.qconfig.admin.web.controller;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.common.bean.JsonV2;
import qunar.tc.qconfig.common.util.FileChecker;

/**
 * @author zhenyu.nie created on 2014 2014/5/29 17:14
 */
@Controller
public class UploadController extends AbstractControllerHelper {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/upload/uploadFile", method = RequestMethod.POST)
    @ResponseBody
    public Object upload(@RequestParam("group") String group, @RequestParam("profile") String profile,
                         final @RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        logger.info("upload file, group=[{}], profile=[{}], file name=[{}]", group, profile, filename);

        checkLegalGroup(group);
        checkLegalProfile(profile);
        checkLegalDataId(filename);

        Monitor.UPLOAD_STATICS.inc();
        FileChecker.checkName(filename);
        Preconditions.checkArgument(!FileChecker.isTemplateFile(filename), "暂不支持上传模版文件");
        return JsonV2.successOf(getFileData(file));
    }
}
