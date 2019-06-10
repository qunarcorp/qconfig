package qunar.tc.qconfig.demo.qconfig;

import qunar.tc.qconfig.demo.mock.MockJsonBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.spring.QConfig;
import qunar.tc.qconfig.client.spring.QConfigLogLevel;

@Component
public class JsonDemo {

    private static Logger logger = LoggerFactory.getLogger(JsonDemo.class);

    @QConfig("string.json")
    public void test(String word) {
        logger.info("Json String Test {}", word);
    }

    @QConfig("string.json")
    private String word;

    @QConfig("bean.json")
    public void testA(MockJsonBean word) {
        logger.info("Json Bean test {}" + word.toString());
    }

    @QConfig("bean.json")
    private MockJsonBean mockBean;

    @QConfig(value = "string.json", logLevel = QConfigLogLevel.high)
    public void testLogHigh(String word) {
        logger.info("High log level Test {}", word);
    }

    @QConfig(value = "string.json", logLevel = QConfigLogLevel.mid)
    public void testLogMid(String word) {
        logger.info("Mid log level Test {}", word);
    }

    @QConfig(value = "string.json", logLevel = QConfigLogLevel.low)
    public void testLogLow(String word) {
        logger.info("Low log level Test {}", word);
    }

    @QConfig(value = "string.json", logLevel = QConfigLogLevel.off)
    public void testLogOff(String word) {
        logger.info("Off log level Test {}", word);
    }

}
