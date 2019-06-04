package qunar.tc.qconfig.client.impl;

/**
 * @author zhenyu.nie created on 2018 2018/8/14 19:51
 */
public class TrivialTranslator extends QConfigTranslator<Void> {

    @Override
    public Void translate(String value) {
        return null;
    }
}
