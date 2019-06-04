package qunar.tc.qconfig.client.impl;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.util.AppStoreUtil;
import qunar.tc.qconfig.common.util.TemplateMergeParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by zhaohui.yu
 * 4/23/15
 */
class TemplateTool {
    private static final Logger logger = LoggerFactory.getLogger(TemplateTool.class);

    private static final File MERGED_PATH = new File(AppStoreUtil.getAppStore(), "qconfig");
    private static final String MERGED_FILE_SUFFIX = ".merged";

    private static final String PARAMETERS_FILE = "qconfig.template.parameters";

    private final TemplateMergeParser parser;
    private final Map<String, String> parameters;

    TemplateTool() {
        parameters = readParameters();
        this.parser = new TemplateMergeParser(parameters);
    }

    public String merge(String fileName, String content) {
        try {
            TemplateMergeParser.Result result = parser.mergeParse(content);
            if (parameters != null) {
                summary(fileName, result);
                save(fileName, content, result.content);
            }
            return result.content;
        } catch (Throwable e) {
            logger.error("merge parameters failed");
            return content;
        }
    }

    private void summary(String fileName, TemplateMergeParser.Result result) {
        if (result.parameters == null) return;
        logger.info("------------------配置文件 {} 替换情况---------------------\n", fileName);
        Map<TemplateMergeParser.Param, String> map = result.parameters;
        for (Map.Entry<TemplateMergeParser.Param, String> entry : map.entrySet()) {
            TemplateMergeParser.Param param = entry.getKey();
            if (Objects.equal(param.defaultValue, entry.getValue())) {
                logger.info("参数: {}，在参数文件中未发现对应参数，使用默认值: {}", param.name, param.defaultValue);
            } else {
                logger.info("参数: {}，使用参数文件中的参数: {}", param.name, entry.getValue());
            }
        }
        logger.info("--------------------------------------------------------\n");
    }

    private void save(String fileName, String content, String result) {
        File to = new File(MERGED_PATH, fileName + MERGED_FILE_SUFFIX);
        try {
            if (!to.exists()) {
                to.createNewFile();
            }
            Files.asCharSink(to, Charsets.UTF_8).write(result);
        } catch (Throwable e) {
            logger.error("保存merge后的文件失败: {}", to.getAbsolutePath());
        }
    }

    private Map<String, String> readParameters() {
        FileInputStream inputStream = null;
        try {

            URL parametersSource = Thread.currentThread().getContextClassLoader().getResource(PARAMETERS_FILE);
            if (parametersSource == null) {
                return null;
            }
            File parametersFile = new File(parametersSource.getPath());
            if (!parametersFile.exists()) {
                return null;
            }

            inputStream = new FileInputStream(parametersFile);
            Properties properties = new Properties();
            properties.load(inputStream);
            Map<String, String> result = new HashMap<String, String>();
            for (String key : properties.stringPropertyNames()) {
                result.put(key, properties.getProperty(key));
            }
            return result;
        } catch (Throwable e) {
            logger.warn("读取参数文件失败");
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException ignore) {
                logger.error("关闭流失败", ignore);
            }
        }
    }
}
