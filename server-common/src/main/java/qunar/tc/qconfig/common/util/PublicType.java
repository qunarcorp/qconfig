package qunar.tc.qconfig.common.util;

/**
 * 公共文件类型
 *
 * 在public表里面标记引用，继承或是rest类型，默认都是public类型，都可以被其他应用读取
 *
 * NewInherit目的是为了兼容线上已经存在的Inherit类型版本
 *
 * Created by chenjk on 2017/4/19.
 */
public class PublicType {

    public static final int PUBLIC_SHIFT_BITS = 0;

    public static final int REFERENCE_SHIFT_BITS = 0;

    public static final int INHERIT_SHIFT_BITS = 2;

    public static final int REST_MASK_SHIFT_BITS = 3;

    public static final int PUBLIC_MASK = 1;

    public static final int REFERENCE_MASK = 1;

    public static final int INHERIT_MASK = 1 << INHERIT_SHIFT_BITS;

    public static final int REST_MASK = 1 << REST_MASK_SHIFT_BITS;

    private int code;

    public PublicType(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public boolean isReference() {
        return ((code & REFERENCE_MASK) == REFERENCE_MASK);
    }

    public boolean isPublic() {
        return ((code & PUBLIC_MASK) == PUBLIC_MASK);
    }

    public boolean isInherit() {
        return ((code & INHERIT_MASK) == INHERIT_MASK);
    }

    public boolean isRest() {
        return ((code & REST_MASK) == REST_MASK);
    }
}
