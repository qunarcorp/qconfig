package qunar.tc.qconfig.common.util;

/**
 * @author zhenyu.nie created on 2014 2014/5/27 15:58
 */
public class FileChecker {

    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 50;

    public static void checkName(String name) {
        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("文件名称长度必须在" + MIN_LENGTH + "到" + MAX_LENGTH + "之间");
        }

        for (char c : name.toCharArray()) {
            if (c >= 'a' && c <= 'z')
                continue;
            if (c >= 'A' && c <= 'Z')
                continue;
            if (c >= '0' && c <= '9')
                continue;
            switch (c) {
                case '-':
                case '_':
                case '.':
                case '%':
                    continue;
                default:
                    throw new IllegalArgumentException(name + " 包含特殊符号，请使用小写字母，数字,'-','_','.','%'");
            }
        }
    }

    public static boolean isTemplateFile(String name) {
        return name != null && name.endsWith(".t");
    }

    public static boolean isPropertiesFile(String name) {
        return name != null && name.endsWith(Constants.PROPERTIES_FILE_SUFFIX);
    }

    public static boolean isJsonFile(String name) {
        return name != null && name.endsWith(".json");
    }
}