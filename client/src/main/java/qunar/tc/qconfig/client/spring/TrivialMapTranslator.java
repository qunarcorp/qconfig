package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.impl.QConfigMapTranslator;

import java.util.Map;

/**
 * @author zhenyu.nie created on 2018 2018/8/15 19:59
 */
class TrivialMapTranslator extends QConfigMapTranslator<Void> {

    @Override
    public Void translate(Map<String, String> value) {
        return null;
    }
}
