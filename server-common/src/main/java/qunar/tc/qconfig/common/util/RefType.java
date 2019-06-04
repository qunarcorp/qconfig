package qunar.tc.qconfig.common.util;

public enum RefType {
    NULL(-1),//没有实际用处，表示不是引用或者继承类型
    REFERENCE(0),//引用类型
    INHERIT(1);//继承类型

    int value;

    RefType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public boolean isReference() {
        return this == REFERENCE;
    }

    public boolean isInherit() {
        return this == INHERIT;
    }

    public static RefType codeOf(int code) {
        for (RefType refType : RefType.values()) {
            if (refType.value == code) {
                return refType;
            }
        }
        throw new IllegalArgumentException("invalid ref type code [" + code + "] to generate " + RefType.class.getName());
    }
}
