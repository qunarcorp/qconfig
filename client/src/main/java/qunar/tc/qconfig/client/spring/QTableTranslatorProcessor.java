package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.QTable;
import qunar.tc.qconfig.client.TableConfig;
import qunar.tc.qconfig.client.impl.QConfigTableTranslator;
import qunar.tc.qconfig.client.impl.Translators;

class QTableTranslatorProcessor implements Processor {

    private final TableConfig config;
    private final QConfigTableTranslator translator;
    private final Action action;
    private final QConfigLogLevel logLevel;

    QTableTranslatorProcessor(Class<? extends QConfigTableTranslator> translatorClass, String appCode, String file, Feature feature,
                              final Action action, final QConfigLogLevel logLevel, final AnnotationManager manager) {
        translator = Translators.doGetUserTableTranslator(translatorClass);
        this.action = action;
        this.logLevel = logLevel;
        config = TableConfig.get(appCode, file, feature);
        AnnotationListenerManager.getInstance().addAnnotationListener(config, new Configuration.ConfigListener<QTable>() {
            @Override
            public void onLoad(final QTable conf) {
                manager.process(action, translator.translate(conf), logLevel);
            }
        });
    }

    @Override
    public void process(Object bean) {
        action.act(bean, translator.translate(config.asTable()), logLevel);
    }
}
