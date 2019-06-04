package qunar.tc.qconfig.admin.model;

/**
 * @author zhenyu.nie created on 2017 2017/3/22 20:29
 */
public enum InterceptStrategy {

    NO(0, "不拦截"),
    BETA_HAS_PROD_NOT_HAS(1, "beta有prod没有"),
    BETA_NOT_HAS_PROD_HAS(2, "beta没有prod有"),
    ALL(3, "不相同则拦截");

    private int code;
    private String text;

    InterceptStrategy(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int code() {
        return code;
    }

    public String text() {
        return text;
    }

    public static InterceptStrategy codeOf(int code) {
        for (InterceptStrategy strategy : InterceptStrategy.values()) {
            if (strategy.code == code) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("invalid code [" + code + "] to generate " + InterceptStrategy.class.getName());
    }

    public static InterceptStrategy fromText(String text) {
        for (InterceptStrategy statusType : InterceptStrategy.values()) {
            if (statusType.text().equals(text)) {
                return statusType;
            }
        }
        throw new IllegalArgumentException("invalid text [" + text + "] to generate " + InterceptStrategy.class.getName());
    }
}
