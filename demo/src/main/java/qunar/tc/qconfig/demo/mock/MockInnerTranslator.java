package qunar.tc.qconfig.demo.mock;

import qunar.tc.qconfig.client.impl.QConfigTranslator;
public class MockInnerTranslator extends QConfigTranslator<MockProperties.MockInner> {

    @Override
    public MockProperties.MockInner translate(String value) {
        //do what you want
        return new MockProperties.MockInner();
    }
}