package qunar.tc.qconfig.demo.mock;

import qunar.tc.qconfig.client.impl.QConfigMapTranslator;

import java.util.Map;

public class MockTranslator extends QConfigMapTranslator<MockBean> {
    @Override
    public MockBean translate(Map<String, String> value) {
        //do what you want
        return new MockBean();
    }
}
