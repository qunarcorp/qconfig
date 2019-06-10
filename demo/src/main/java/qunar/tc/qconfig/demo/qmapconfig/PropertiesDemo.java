package qunar.tc.qconfig.demo.qmapconfig;

import qunar.tc.qconfig.demo.mock.MockBean;
import qunar.tc.qconfig.demo.mock.MockInnerTranslator;
import qunar.tc.qconfig.demo.mock.MockProperties;
import qunar.tc.qconfig.demo.mock.MockTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.spring.QMapConfig;

import java.util.List;
import java.util.Map;

@Component("mapPropertiesDemo")
public class PropertiesDemo {

    private static Logger logger = LoggerFactory.getLogger(PropertiesDemo.class);

    @QMapConfig(value = "map_config.properties", key = "a")
    private void mapKeyWordTest(String word) {
        logger.info("QMapConfig word test key = a value = {}", word);
    }

    @QMapConfig(value = "map_config.properties")
    private void mapMapTest(Map<String, String> word) {
        logger.info("QMapConfig Map Test {}", word);
    }

    @QMapConfig(value = "map_config.properties", key = "d")
    private void mapKeyArrayTest(List<String> word) {
        logger.info("QMapConfig list test key = d value = {}", word);
    }

    @QMapConfig(value = "map_config.properties", key = "e")
    private void mapKeyMapTest(Map<String, Integer> word) {
        logger.info("QMapConfig Map test key = e value = {}", word);
    }

    @QMapConfig(value = "map_config.properties", translator = MockTranslator.class)
    private void mapKeyMapTest(MockBean mockBean) {
        logger.info("QMapConfig translator bean test value = {}", mockBean);
    }

    @QMapConfig(value = "map_bean_config.properties")
    private void mapBeanTest(MockProperties properties) {
        logger.info("QMapConfig Bean test value = {}", properties);
    }


}
