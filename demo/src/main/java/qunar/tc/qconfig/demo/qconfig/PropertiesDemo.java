package qunar.tc.qconfig.demo.qconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.spring.QConfig;

import java.util.Map;
import java.util.Properties;

@Component
public class PropertiesDemo {

    private static Logger logger = LoggerFactory.getLogger(PropertiesDemo.class);

    @QConfig("string.properties")
    private void propertesTest(String word) {
        logger.info("properties Demo String word {}", word);
    }

    @QConfig("map.properties")
    private void propertesTest(Map<String, String> word) {
        logger.info("properties Demo Map word {}", word);
    }

    @QConfig("map.properties")
    private void propertesTest(Properties word) {
        logger.info("properties Demo properties word {}", word);
    }

    @QConfig("dcdc.properties")
    private void test(String word) {
        logger.error(word);
    }

}
