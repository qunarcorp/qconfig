package qunar.tc.qconfig.client.impl;

import qunar.tc.qconfig.client.QTable;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author zhenyu.nie created on 2018 2018/8/17 13:50
 */
public abstract class QConfigTableTranslator<T> extends TypeCapture<T> {

    private final Type type;

    public QConfigTableTranslator() {
        this.type = captureType();
        checkState(!(type instanceof TypeVariable),
                "cannot construct translator with a type variable");
    }

    public abstract T translate(QTable value);

    public final Type getType() {
        return type;
    }
}
