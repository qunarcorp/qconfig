package qunar.tc.qconfig.admin.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author keli.wang
 * @since 2017/4/10
 */
public interface TemplateExcelParseService {

    List<Map<String, String>> parse(final MultipartFile file);
}
