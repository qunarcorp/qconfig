package qunar.tc.qconfig.demo.qtableconfig;

import qunar.tc.qconfig.demo.mock.MockBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.QTable;
import qunar.tc.qconfig.client.impl.QConfigTableTranslator;
import qunar.tc.qconfig.client.spring.QTableConfig;

import java.util.Map;

@Component
public class TableConfigDemo {

    private static Logger logger = LoggerFactory.getLogger(TableConfigDemo.class);

    @QTableConfig("table_config_table.t")
    private void tableConfigAll(QTable qTable) {
        logger.info("table Config get All content {}", qTable);
    }

    //rowkey做key，rowkey对应的map转换为Bean
    @QTableConfig("table_config_Bean.t")
    private void tableConfigMap (Map<String, MockBean> beanMap) {
        logger.info("table Config get All content to map {}", beanMap);
    }


    //丢弃rowKey，转换为Bean List
    @QTableConfig(value = "table_config_mock.t", translator = MockTableTranslator.class)
    private void tableConfigMap (MockBean object) {
        logger.info("table Config get All content {}", object);
    }

    private static class MockTableTranslator extends QConfigTableTranslator<MockBean> {

        @Override
        public MockBean translate(QTable value) {
            logger.info("input content {}", value);
            //do what you want
            return new MockBean();
        }
    }
}
