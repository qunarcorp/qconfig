package qunar.tc.qconfig.admin.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import qunar.tc.qconfig.admin.service.TemplateExcelParseService;

import javax.annotation.Resource;

/**
 * @author keli.wang
 * @since 2017/4/10
 */
@Controller
public class ExcelController {

    @Resource
    private TemplateExcelParseService templateExcelParseService;

    @RequestMapping(value = "/excel/parser", method = RequestMethod.POST)
    @ResponseBody
    public Object parseExcel(@RequestParam("file") final MultipartFile file) {
        return templateExcelParseService.parse(file);
    }
}
