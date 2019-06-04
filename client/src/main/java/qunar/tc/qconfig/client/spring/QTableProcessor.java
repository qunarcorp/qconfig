package qunar.tc.qconfig.client.spring;

import com.google.common.base.Suppliers;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.QTable;
import qunar.tc.qconfig.client.TableConfig;

class QTableProcessor implements Processor {

    private final TableConfig config;
    private final Action action;
    private final QConfigLogLevel logLevel;

    QTableProcessor(String appCode, String file, Feature feature,
                    final Action action, final QConfigLogLevel logLevel, final AnnotationManager manager) {
        this.action = action;
        this.logLevel = logLevel;
        config = TableConfig.get(appCode, file, feature);
        AnnotationListenerManager.getInstance().addAnnotationListener(config, new Configuration.ConfigListener<QTable>() {
            @Override
            public void onLoad(QTable conf) {
                manager.process(action, conf, logLevel);
            }
        });
    }

    public void process(Object bean) {
        action.act(bean, config.asTable(), logLevel);
    }
}
