package qunar.tc.qconfig.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.spring.QConfig;

import java.util.Map;

/**
 * User: zhaohuiyu
 * Date: 5/28/14
 * Time: 12:26 PM
 */
public class DemoBean implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(DemoBean.class);

    private String name;

    private String city;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

}
