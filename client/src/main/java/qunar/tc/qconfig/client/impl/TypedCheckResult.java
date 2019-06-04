package qunar.tc.qconfig.client.impl;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * @author zhenyu.nie created on 2017 2017/4/1 16:58
 */
public class TypedCheckResult {

    enum Type {
        UPDATE, PULL
    }

    public static final TypedCheckResult EMPTY = new TypedCheckResult(ImmutableMap.<Meta, VersionProfile>of(), Type.UPDATE);

    private final Map<Meta, VersionProfile> result;

    private final Type type;

    public TypedCheckResult(Map<Meta, VersionProfile> result, Type type) {
        this.result = result;
        this.type = type;
    }

    public Map<Meta, VersionProfile> getResult() {
        return result;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "TypedCheckResult{" +
                "result=" + result +
                ", type=" + type +
                '}';
    }
}
