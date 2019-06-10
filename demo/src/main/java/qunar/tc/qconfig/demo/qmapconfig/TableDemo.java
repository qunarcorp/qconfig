package qunar.tc.qconfig.demo.qmapconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.spring.QMapConfig;

import java.util.Map;

@Component("mapTableDemo")
public class TableDemo {

    private static Logger logger = LoggerFactory.getLogger(TableDemo.class);

    @QMapConfig(value = "table_config_map.t", key = "1/id")
    private void mapConfigTableTest(String word) {
        logger.info("map Config table file, get content by key {}", word);
    }

    @QMapConfig(value = "table_config_map.t")
    private void mapConfigTableTest(Map<String, String> word) {
        logger.info("map Config table file, get all content {}", word);
    }

    //剩余参照propertiesDemo中。
}
