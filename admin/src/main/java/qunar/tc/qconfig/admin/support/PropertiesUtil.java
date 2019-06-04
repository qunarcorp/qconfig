package qunar.tc.qconfig.admin.support;

import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.FileChecker;

/**
 * @author zhenyu.nie created on 2017 2017/6/20 17:53
 */
public class PropertiesUtil {

    public static String getTemplateName(String fileName) {
        if (!FileChecker.isPropertiesFile(fileName)) {
            throw new IllegalArgumentException("not properties file [" + fileName + "]");
        }

        return fileName.substring(0, fileName.length() - Constants.PROPERTIES_FILE_SUFFIX.length()) + AdminConstants.PROPERTIES_TEMPLATE_SUFFIX;
    }
}
