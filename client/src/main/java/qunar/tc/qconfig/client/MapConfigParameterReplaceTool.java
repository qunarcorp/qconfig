package qunar.tc.qconfig.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.util.AppStoreUtil;
import qunar.tc.qconfig.common.util.Constants;

import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * @author suhongju
 * @date 2017-06-28
 * for flight beta
 */
class MapConfigParameterReplaceTool {
    private static final Logger logger = LoggerFactory.getLogger(MapConfigParameterReplaceTool.class);

    private static final File MERGED_PATH = new File(AppStoreUtil.getAppStore(), "qconfig");
    private static final String MERGED_FILE_SUFFIX = ".merged";

    private static final File PARAMETER_FILE_PATH = new File(System.getProperty("catalina.base"), "template");

    private static final String PARAMETER_FILE_SUFFIX = ".parameters";

    private static final Map<String, Replace> replaceParameters = Maps.newConcurrentMap();

    private static class handler {
        private static final MapConfigParameterReplaceTool instance = new MapConfigParameterReplaceTool();
    }

    public static MapConfigParameterReplaceTool getInstance() {
        return handler.instance;
    }

    private MapConfigParameterReplaceTool() {
    }

    public Map<String, String> replace(String fileName, Map<String, String> content, boolean trimValue) {
        if (content == null) {
            return ImmutableMap.of();
        }
        Replace replace = getReplaceInstance(fileName);
        if (!replace.needReplace) {
            return content;
        }
        Map<String, String> tmp = Maps.newHashMap();
        tmp.putAll(content);
        for (Map.Entry<String, String> parameter : replace.parameters.entrySet()) {
            String replaceVal = getValue(parameter.getValue(), trimValue);
            String oldVal = tmp.put(parameter.getKey(), replaceVal);
            logger.info("配置替换,fileName:{},key:{},value:{}->{}", fileName, parameter.getKey(), oldVal, replaceVal);
        }
        save(fileName, tmp);
        return ImmutableMap.copyOf(tmp);
    }

    /**
     * 内存中的替换实例
     *
     * @param fileName
     * @return
     */
    private Replace getReplaceInstance(String fileName) {
        Replace replace = replaceParameters.get(fileName);
        if (replace == null) {
            replace = initReplaceInstance(fileName);
            replaceParameters.put(fileName, replace);
        }
        return replace;
    }

    /**
     * 初始fileName对应的replaceParameters
     *
     * @param fileName
     * @return
     */
    private Replace initReplaceInstance(String fileName) {
        Map<String, String> param = loadParameters(fileName);
        return new Replace(param);
    }

    /**
     * 替换key-value
     *
     * @param fileName
     * @return
     */
    private Map<String, String> loadParameters(String fileName) {
        InputStream inputStream = null;
        File parameterFile = null;
        try {
            parameterFile = new File(PARAMETER_FILE_PATH, fileName + PARAMETER_FILE_SUFFIX);
            if (!parameterFile.exists()) {
                return null;
            }
            inputStream = new FileInputStream(parameterFile);
            Properties properties = new Properties();
            properties.load(inputStream);
            Map<String, String> result = Maps.newHashMap();
            for (String key : properties.stringPropertyNames()) {
                result.put(key, properties.getProperty(key));
            }
            return result;
        } catch (Exception e) {
            logger.error("替换文件读取失败，{}", parameterFile != null ? parameterFile.getAbsolutePath() : fileName);
            return null;
        } finally {
            safeClose(inputStream);
        }
    }

    /**
     * 存储替换变量后的文件
     *
     * @param fileName
     * @param result
     */
    private void save(String fileName, Map<String, String> result) {
        OutputStreamWriter outputStreamWriter = null;
        File to = null;
        try {
            to = new File(MERGED_PATH, fileName + MERGED_FILE_SUFFIX);
            if (result == null) {
                return;
            }
            if (!to.exists()) {
                to.createNewFile();
            }
            OutputStream outputStream = new FileOutputStream(to);
            Properties properties = new Properties();
            properties.putAll(result);
            outputStreamWriter = new OutputStreamWriter(outputStream, Constants.UTF_8);
            properties.store(outputStreamWriter, "");
        } catch (Exception e) {
            logger.error("保存merge后的文件失败: {}", to != null ? to.getAbsolutePath() : fileName);
        } finally {
            safeClose(outputStreamWriter);
        }
    }

    /**
     * trim
     *
     * @param value
     * @param trimValue
     * @return
     */
    private String getValue(String value, boolean trimValue) {
        if (trimValue && value != null) {
            return value.trim();
        }
        return value;
    }

    private void safeClose(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            logger.warn("outputStream close error");
        }
    }

    class Replace {
        final boolean needReplace;
        final Map<String, String> parameters;

        public Replace(Map<String, String> parameters) {
            if (parameters == null) {
                this.needReplace = false;
                this.parameters = null;
            } else {
                this.needReplace = true;
                this.parameters = ImmutableMap.copyOf(parameters);
            }

        }
    }
}
