package qunar.tc.qconfig.client.impl;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author zhenyu.nie created on 2018 2018/8/15 19:57
 */
public abstract class QConfigMapTranslator<T> extends TypeCapture<T> {

    private final Type type;

    public QConfigMapTranslator() {
        this.type = captureType();
        checkState(!(type instanceof TypeVariable),
                "cannot construct translator with a type variable");
    }

    public abstract T translate(Map<String, String> value);

    public final Type getType() {
        return type;
    }
}
