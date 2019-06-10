package qunar.tc.qconfig.demo.mock;

import com.google.common.base.MoreObjects;
import qunar.tc.qconfig.client.impl.QConfigTranslator;
import qunar.tc.qconfig.client.spring.DisableQConfig;
import qunar.tc.qconfig.client.spring.QConfigField;

import java.util.List;
import java.util.Map;

public class MockProperties {

    @QConfigField(key = "a")
    private String a;

    @QConfigField(key = "b")
    private String b;

    @QConfigField(key = "c")
    private String c;

    @QConfigField(key = "d")
    private List<String> d;

    @QConfigField(key = "e")
    private Map<String, Integer> e;

    @QConfigField(key = "g", value = MockInnerTranslator.class)
    private MockInner inner;

    @DisableQConfig
    private String f;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("a", a)
                .add("b", b)
                .add("c", c)
                .add("d", d)
                .add("e", e)
                .add("inner", inner)
                .add("f", f)
                .toString();
    }

    public MockInner getInner() {
        return inner;
    }

    public void setInner(MockInner inner) {
        this.inner = inner;
    }

    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public List<String> getD() {
        return d;
    }

    public void setD(List<String> d) {
        this.d = d;
    }

    public Map<String, Integer> getE() {
        return e;
    }

    public void setE(Map<String, Integer> e) {
        this.e = e;
    }

    public static class MockInner {

    }
}
