package qunar.tc.qconfig.servercommon.bean;

/**
 * @author zhenyu.nie created on 2014 2014/7/4 15:32
 */
public enum RefChangeType {

    ADD(0, "add"), REMOVE(1, "remove"), INHERIT(2, "inherit");

    private int code;

    private String text;

    private RefChangeType(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int code() {
        return code;
    }

    public String text() {
        return text;
    }

    public static RefChangeType codeOf(int code) {
        for (RefChangeType changeType : RefChangeType.values()) {
            if (changeType.code == code) {
                return changeType;
            }
        }
        throw new IllegalArgumentException("invalid ref change type code [" + code + "] to generate " + RefChangeType.class.getName());
    }

    public static RefChangeType fromText(String text) {
        for (RefChangeType refChangeType : RefChangeType.values()) {
            if (refChangeType.text.equals(text)) {
                return refChangeType;
            }
        }
        throw new IllegalArgumentException("invalid ref change type text [" + text + "] to generate " + RefChangeType.class.getName());
    }
}
