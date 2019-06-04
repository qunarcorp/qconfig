package qunar.tc.qconfig.client.impl;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author zhenyu.nie created on 2018 2018/8/1 19:19
 */
public abstract class QConfigTranslator<T> extends TypeCapture<T> {

    private final Type type;

    protected QConfigTranslator() {
        this.type = captureType();
        checkState(!(type instanceof TypeVariable),
                "cannot construct translator with a type variable");
    }

    public abstract T translate(String value);

    public final Type getType() {
        return type;
    }
}
