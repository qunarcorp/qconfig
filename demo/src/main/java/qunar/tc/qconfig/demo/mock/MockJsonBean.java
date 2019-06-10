package qunar.tc.qconfig.demo.mock;

public class MockJsonBean {
    private int outerA;

    private MockInnerBean outerB;

    public int getOuterA() {
        return outerA;
    }

    public void setOuterA(int outerA) {
        this.outerA = outerA;
    }

    public MockInnerBean getOuterB() {
        return outerB;
    }

    public void setOuterB(MockInnerBean outerB) {
        this.outerB = outerB;
    }

    @Override
    public String toString() {
        return "MockBean{" +
                "outerA=" + outerA +
                ", outerB=" + outerB +
                '}';
    }

    static class MockInnerBean {
        private String inner;

        public String getInner() {
            return inner;
        }

        public void setInner(String inner) {
            this.inner = inner;
        }

        @Override
        public String toString() {
            return "MockInnerBean{" +
                    "inner='" + inner + '\'' +
                    '}';
        }
    }
}
