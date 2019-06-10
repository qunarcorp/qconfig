package qunar.tc.qconfig.demo.api;

import qunar.tc.qconfig.demo.mock.MockBean;
import qunar.tc.qconfig.demo.mock.MockJsonBean;
import org.springframework.stereotype.Component;
import qunar.tc.qconfig.client.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

@Component
public class ApiDemo {

    @PostConstruct
    private void init() {
        MapConfig config = MapConfig.get("map_config.properties");
        config.asMap();
        config.addListener(new Configuration.ConfigListener<Map<String, String>>() {
            @Override
            public void onLoad(Map<String, String> conf) {
                //do what you want
            }
        });
        config.addPropertiesListener(new MapConfig.PropertiesChangeListener() {
            @Override
            public void onChange(PropertiesChange change) {
                //do what you want
            }
        });

        JsonConfig<MockJsonBean> jsonConfig = JsonConfig.get("bean.json", MockJsonBean.class);
        MockJsonBean jsonBean = jsonConfig.current();
        jsonConfig.addListener(new Configuration.ConfigListener<MockJsonBean>() {
            @Override
            public void onLoad(MockJsonBean conf) {
                //do what you want
            }
        });

        /*
         * 关于Json转泛型 可以使用如下方式定义class
         *
         * List<Integer> 如下
         *
         * JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(List.class, Integer.class);
         * JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(List.class).addParameter(Integer.class);
         * JsonConfig<List<Integer>> config = JsonConfig.get("list.json", parameter);
         *
         * Map<String, Integer>
         * JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(Map.class, String.class, Integer.class);
         * JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(Map.class).addParameter(String.class).addParameter(String.class);
         * JsonConfig<Map<String, Integer>> config = JsonConfig.get("map.json", parameter);
         *
         * Map<String, Foo<Integer>》
         * JsonConfig.ParameterizedClass stringDesc = JsonConfig.ParameterizedClass.of(String.class);
         * JsonConfig.ParameterizedClass fooDesc = JsonConfig.ParameterizedClass.of(Foo.class, Integer.class);
         * JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(Map.class, stringDesc, fooDesc);
         * JsonConfig.ParameterizedClass parameter = JsonConfig.ParameterizedClass.of(Map.class)
         *      .addParameter(String.class)
         *      .addParameter(JsonConfig.ParameterizedClass.of(Foo.class, Integer.class));
         * JsonConfig<Map<String, Foo<Integer>>> config = JsonConfig.get("complex.json", parameter);
         */

        TableConfig tableConfig = TableConfig.get("table_config_table.t");
        QTable qTable = tableConfig.asTable();
        tableConfig.addListener(new Configuration.ConfigListener<QTable>() {
            @Override
            public void onLoad(QTable conf) {
                //do what you want
            }
        });

        // 对于自定义格式，可以如下
        TypedConfig<MockBean> typedConfig = TypedConfig.get("aaa.c", new TypedConfig.Parser<MockBean>() {
            @Override
            public MockBean parse(String data) throws IOException {
                //do what you need
                return new MockBean();
            }
        });
        MockBean bean = typedConfig.current();
        typedConfig.addListener(new Configuration.ConfigListener<MockBean>() {
            @Override
            public void onLoad(MockBean conf) {

            }
        });
    }
}
