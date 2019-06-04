package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.QTable;
import qunar.tc.qconfig.client.impl.QConfigTableTranslator;

/**
 * @author zhenyu.nie created on 2018 2018/8/17 13:52
 */
class TrivialTableTranslator extends QConfigTableTranslator<Void> {
    @Override
    public Void translate(QTable value) {
        return null;
    }
}
