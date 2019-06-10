package qunar.tc.qconfig.demo.qconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.QTable;
import qunar.tc.qconfig.client.spring.QConfig;
import qunar.tc.qconfig.client.spring.QMapConfig;

@Component
public class TableDemo {

    private static Logger logger = LoggerFactory.getLogger(TableDemo.class);

    @QConfig("table_config.t")
    private void tableTest(QTable table) {
        logger.info("qconfig test table {}", table);
    }

    @QConfig("table_config_word.t")
    private void tableTest(String table) {
        logger.info("qconfig test table {}", table);
    }
}

