package qunar.tc.qconfig.client.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author zhenyu.nie created on 2018 2018/8/15 19:56
 */
abstract class TypeCapture<T> {

     final Type captureType() {
        Type superclass = getClass().getGenericSuperclass();
        checkArgument(superclass instanceof ParameterizedType,
                "%s isn't parameterized", superclass);
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }
}
