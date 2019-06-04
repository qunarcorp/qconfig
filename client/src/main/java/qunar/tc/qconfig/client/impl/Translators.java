package qunar.tc.qconfig.client.impl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import qunar.tc.qconfig.client.spring.DisableQConfig;
import qunar.tc.qconfig.client.spring.QConfigField;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhenyu.nie created on 2018 2018/8/1 19:13
 */
public class Translators {

    private static final Logger logger = LoggerFactory.getLogger(Translators.class);

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    @SuppressWarnings("all")
    private static final Splitter.MapSplitter MAP_SPLITTER = Splitter.on(';').trimResults().withKeyValueSeparator(':');

    private static final Map<Class<?>, QConfigTranslator> primitiveTranslators = initPrimitiveTranslators();

    private static final Map<Class<?>, QConfigTranslator> userTranslators = Maps.newHashMap();

    private static final Map<Class<?>, QConfigMapTranslator> userMapTranslators = Maps.newHashMap();

    private static final Map<Class<?>, QConfigTableTranslator> userTableTranslators = Maps.newHashMap();

    private static abstract class BoxPrimitiveTranslator<T> extends QConfigTranslator<T> {

        private final QConfigTranslator<T> translator;

        protected BoxPrimitiveTranslator(QConfigTranslator<T> translator) {
            this.translator = translator;
        }

        @Override
        public T translate(String value) {
            if (Strings.isNullOrEmpty(value)) {
                return null;
            }
            return translator.translate(value);
        }
    }

    private static abstract class UnBoxPrimitiveTranslator<T> extends QConfigTranslator<T> {

        private final QConfigTranslator<T> translator;

        protected UnBoxPrimitiveTranslator(QConfigTranslator<T> translator) {
            this.translator = translator;
        }

        protected abstract T initValue();

        @Override
        public T translate(String value) {
            if (Strings.isNullOrEmpty(value)) {
                return initValue();
            }
            return translator.translate(value);
        }
    }

    private static Map<Class<?>, QConfigTranslator> initPrimitiveTranslators() {
        Map<Class<?>, QConfigTranslator> primitiveTranslators = Maps.newHashMap();
        primitiveTranslators.put(String.class, new QConfigTranslator<String>() {
            @Override
            public String translate(String value) {
                return value;
            }
        });

        QConfigTranslator<Boolean> booleanTranslator = new QConfigTranslator<Boolean>() {
            @Override
            public Boolean translate(String value) {
                return Boolean.valueOf(value);
            }
        };
        primitiveTranslators.put(boolean.class, new UnBoxPrimitiveTranslator<Boolean>(booleanTranslator) {
            @Override
            protected Boolean initValue() {
                return Boolean.FALSE;
            }
        });
        primitiveTranslators.put(Boolean.class, new BoxPrimitiveTranslator<Boolean>(booleanTranslator) {});

        QConfigTranslator<Byte> byteTranslator = new QConfigTranslator<Byte>() {
            @Override
            public Byte translate(String value) {
                return Byte.valueOf(value);
            }
        };
        primitiveTranslators.put(byte.class, new UnBoxPrimitiveTranslator<Byte>(byteTranslator) {
            @Override
            protected Byte initValue() {
                return (byte) 0;
            }
        });
        primitiveTranslators.put(Byte.class, new BoxPrimitiveTranslator<Byte>(byteTranslator) {});

        QConfigTranslator<Character> charTranslator = new QConfigTranslator<Character>() {
            @Override
            public Character translate(String value) {
                if (value.length() == 1) {
                    return value.charAt(0);
                } else {
                    throw new IllegalArgumentException("value [" + value + "] to char, but length is " + value.length());
                }
            }
        };
        primitiveTranslators.put(char.class, new UnBoxPrimitiveTranslator<Character>(charTranslator) {
            @Override
            protected Character initValue() {
                return (char) 0;
            }
        });
        primitiveTranslators.put(Character.class, new BoxPrimitiveTranslator<Character>(charTranslator) {});

        QConfigTranslator<Short> shortTranslator = new QConfigTranslator<Short>() {
            @Override
            public Short translate(String value) {
                return Short.valueOf(value);
            }
        };
        primitiveTranslators.put(short.class, new UnBoxPrimitiveTranslator<Short>(shortTranslator) {
            @Override
            protected Short initValue() {
                return 0;
            }
        });
        primitiveTranslators.put(Short.class, new BoxPrimitiveTranslator<Short>(shortTranslator) {});

        QConfigTranslator<Integer> intTranslator = new QConfigTranslator<Integer>() {
            @Override
            public Integer translate(String value) {
                return Integer.valueOf(value);
            }
        };
        primitiveTranslators.put(int.class, new UnBoxPrimitiveTranslator<Integer>(intTranslator) {
            @Override
            protected Integer initValue() {
                return 0;
            }
        });
        primitiveTranslators.put(Integer.class, new BoxPrimitiveTranslator<Integer>(intTranslator) {});

        QConfigTranslator<Long> longTranslator = new QConfigTranslator<Long>() {
            @Override
            public Long translate(String value) {
                return Long.valueOf(value);
            }
        };
        primitiveTranslators.put(long.class, new UnBoxPrimitiveTranslator<Long>(longTranslator) {
            @Override
            protected Long initValue() {
                return 0L;
            }
        });
        primitiveTranslators.put(Long.class, new BoxPrimitiveTranslator<Long>(longTranslator) {});

        QConfigTranslator<Float> floatTranslator = new QConfigTranslator<Float>() {
            @Override
            public Float translate(String value) {
                return Float.parseFloat(value);
            }
        };
        primitiveTranslators.put(float.class, new UnBoxPrimitiveTranslator<Float>(floatTranslator) {
            @Override
            protected Float initValue() {
                return 0F;
            }
        });
        primitiveTranslators.put(Float.class, new BoxPrimitiveTranslator<Float>(floatTranslator) {});

        QConfigTranslator<Double> doubleTranslator = new QConfigTranslator<Double>() {
            @Override
            public Double translate(String value) {
                return Double.parseDouble(value);
            }
        };
        primitiveTranslators.put(double.class, new UnBoxPrimitiveTranslator<Double>(doubleTranslator) {
            @Override
            protected Double initValue() {
                return 0D;
            }
        });
        primitiveTranslators.put(Double.class, new BoxPrimitiveTranslator<Double>(doubleTranslator) {});
        return primitiveTranslators;
    }

    public static <T> T translate(Map<String, String> map, Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T bean = constructor.newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (AnnotationUtils.getAnnotation(field, DisableQConfig.class) == null &&
                        !Modifier.isTransient(field.getModifiers())) {
                    Object fieldValue = getFieldValue(field, map);
                    field.setAccessible(true);
                    field.set(bean, fieldValue);
                }
            }
            return bean;
        } catch (NoSuchMethodException e) {
            logger.error("{}类找不到无参构造函数", clazz.getCanonicalName(), e);
            throw new RuntimeException(clazz.getCanonicalName() + "类找不到无参构造函数", e);
        } catch (Exception e) {
            logger.error("translate to class error, {}", clazz.getCanonicalName(), e);
            throw new RuntimeException("translate to class error, " + clazz.getCanonicalName(), e);
        }
    }

    private static Object getFieldValue(Field field, Map<String, String> map) {
        String strValue = getStrValue(field, map);
        QConfigTranslator translator = getTranslator(field);
        return translator.translate(strValue);
    }

    private static class EnumTranslator extends QConfigTranslator<Object> {

        private final Class<?> type;

        public EnumTranslator(Class<?> type) {
            this.type = type;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object translate(String value) {
            if (Strings.isNullOrEmpty(value)) {
                return null;
            } else {
                Class<? extends Enum> enumType = (Class<? extends Enum>) type;
                return Enum.valueOf(enumType, value);
            }
        }
    }

    private static class ListTranslator extends QConfigTranslator<Object> {

        private final QConfigTranslator translator;

        public ListTranslator(QConfigTranslator translator) {
            this.translator = translator;
        }

        @Override
        public Object translate(String value) {
            if (Strings.isNullOrEmpty(value)) {
                return ImmutableList.of();
            }

            ImmutableList.Builder<Object> builder = ImmutableList.builder();
            for (String v : COMMA_SPLITTER.split(value)) {
                builder.add(translator.translate(v));
            }
            return builder.build();
        }
    }

    private static class SetTranslator extends QConfigTranslator<Object> {

        private final QConfigTranslator translator;

        public SetTranslator(QConfigTranslator translator) {
            this.translator = translator;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object translate(String value) {
            if (Strings.isNullOrEmpty(value)) {
                return ImmutableSet.of();
            }

            Set set = Sets.newHashSet();
            for (String v : COMMA_SPLITTER.split(value)) {
                set.add(translator.translate(v));
            }
            return ImmutableSet.copyOf(set);
        }
    }

    private static String getStrValue(Field field, Map<String, String> map) {
        QConfigField fieldAnnotation = field.getAnnotation(QConfigField.class);
        if (fieldAnnotation != null && !Strings.isNullOrEmpty(fieldAnnotation.key())) {
            return map.get(fieldAnnotation.key());
        } else {
            return map.get(field.getName());
        }
    }

    private static QConfigTranslator parseTranslator(Type type, QConfigTranslator userTranslator, AtomicBoolean useUserTranslator) {
        if (userTranslator != null && userTranslator.getType().equals(type)) {
            useUserTranslator.set(Boolean.TRUE);
            return userTranslator;
        } else if (isListSetClass(type)) {
            Type paramType = getListSetParamType(type);
            return generateListSetTranslator(type, parseTranslator(paramType, userTranslator, useUserTranslator));
        } else if (isMapClass(type)) {
            Type keyType = getMapKeyType(type);
            QConfigTranslator keyTranslator = parseTranslator(keyType, userTranslator, useUserTranslator);
            Type valueType = getMapValueType(type);
            QConfigTranslator valueTranslator = parseTranslator(valueType, userTranslator, useUserTranslator);
            return new MapTranslator(keyTranslator, valueTranslator);
        } else if(isArray(type)) {
            Type paramType = getArrayParamType(type);
            return new ArrayTranslator((Class<?>) paramType, parseTranslator(paramType, userTranslator, useUserTranslator));
        } else {
            return doGetPrimitiveTranslator(type);
        }
    }

    private static Type getGenericArgType(Type type, int argIndex) {
        if (type instanceof Class) {
            return String.class;
        } else if (type instanceof ParameterizedType) {
            return correctGeneric(((ParameterizedType) type).getActualTypeArguments()[argIndex]);
        } else {
            throw new IllegalStateException("unknown type to translate, [" + type + "]");
        }
    }

    private static Type getMapKeyType(Type type) {
        return getGenericArgType(type, 0);
    }

    private static Type getMapValueType(Type type) {
        return getGenericArgType(type, 1);
    }

    private static Type getListSetParamType(Type type) {
        return getGenericArgType(type, 0);
    }

    private static Type getArrayParamType(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            return clazz.getComponentType();
        } else {
            throw new IllegalStateException("unknown type to translate, [" + type + "]");
        }
    }

    private static QConfigTranslator getTranslator(Field field) {
        AtomicBoolean useUserTranslator = new AtomicBoolean(false);
        QConfigTranslator userTranslator = getUserTranslator(field);
        QConfigTranslator translator = parseTranslator(field.getGenericType(), userTranslator, useUserTranslator);
        if (userTranslator != null && !useUserTranslator.get()) {
            RuntimeException e = new RuntimeException("user defined translator not used, field " + field.getName() + ", translator " + userTranslator.getClass().getName());
            logger.error("user defined translator not used, field {}, translator {}", field.getName(), userTranslator.getClass().getName(), e);
            throw e;
        }
        return translator;
    }

    private static QConfigTranslator generateListSetTranslator(Type type, QConfigTranslator translator) {
        if (type instanceof Class) {
            return generateListSetTranslator((Class) type, translator);
        } else if (type instanceof ParameterizedType) {
            return generateListSetTranslator((Class) ((ParameterizedType) type).getRawType(), translator);
        } else {
            throw new IllegalStateException("unknown type to translate, [" + type + "]");
        }
    }

    private static QConfigTranslator generateListSetTranslator(Class clazz, QConfigTranslator translator) {
        if (!isListSetClass(clazz)) {
            throw new IllegalStateException("unknown collection class, [" + clazz.getName() + "]");
        }

        if (clazz.equals(List.class)) {
            return new ListTranslator(translator);
        } else if (clazz.equals(Set.class)) {
            return new SetTranslator(translator);
        } else {
            throw new IllegalStateException("unknown collection class, [" + clazz.getName() + "]");
        }
    }

    private static final Set<Class<?>> LIST_SET_CLASSES = ImmutableSet.<Class<?>>of(List.class, Set.class);

    @SuppressWarnings("all")
    private static boolean isListSetClass(Type type) {
        if (type instanceof Class) {
            return LIST_SET_CLASSES.contains(type);
        } else if (type instanceof ParameterizedType) {
            return LIST_SET_CLASSES.contains(((ParameterizedType) type).getRawType());
        } else {
            return false;
        }
    }

    private static boolean isMapClass(Type type) {
        if (type instanceof Class) {
            return Map.class.equals(type);
        } else if (type instanceof ParameterizedType) {
            return Map.class.equals(((ParameterizedType) type).getRawType());
        } else {
            return false;
        }
    }

    private static boolean isArray(Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;
            return clazz.isArray();
        } else {
            return false;
        }
    }

    private static QConfigTranslator getUserTranslator(Field field) {
        QConfigField fieldAnnotation = field.getAnnotation(QConfigField.class);
        if (fieldAnnotation != null) {
            Class<? extends QConfigTranslator> translatorClass = fieldAnnotation.value();
            if (!translatorClass.equals(TrivialTranslator.class)) {
                return doGetUserTranslator(translatorClass);
            }
        }
        return null;
    }

    private static synchronized QConfigTranslator doGetUserTranslator(Class<? extends QConfigTranslator> clazz) {
        try {
            QConfigTranslator translator = userTranslators.get(clazz);
            if (translator != null) {
                return translator;
            }
            Constructor<? extends QConfigTranslator> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            QConfigTranslator newTranslator = constructor.newInstance();
            userTranslators.put(clazz, newTranslator);
            return newTranslator;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static QConfigTranslator doGetPrimitiveTranslator(Class clazz) {
        if (clazz.isEnum()) {
            return new EnumTranslator(clazz);
        }

        QConfigTranslator primitiveTranslator = primitiveTranslators.get(clazz);
        if (primitiveTranslator != null) {
            return primitiveTranslator;
        } else {
            throw new IllegalStateException("unknown type to translate, [" + clazz + "]");
        }
    }

    private static QConfigTranslator doGetPrimitiveTranslator(Type type) {
        Class clazz;
        if (type instanceof Class) {
            clazz = (Class) type;
        } else {
            throw new IllegalStateException("unknown type to translate, [" + type + "]");
        }

        return doGetPrimitiveTranslator(clazz);
    }

    public static synchronized QConfigMapTranslator doGetUserMapTranslator(Class<? extends QConfigMapTranslator> clazz) {
        try {
            QConfigMapTranslator translator = userMapTranslators.get(clazz);
            if (translator != null) {
                return translator;
            }
            Constructor<? extends QConfigMapTranslator> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            QConfigMapTranslator newTranslator = constructor.newInstance();
            userMapTranslators.put(clazz, newTranslator);
            return newTranslator;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized QConfigTableTranslator doGetUserTableTranslator(Class<? extends QConfigTableTranslator> clazz) {
        try {
            QConfigTableTranslator translator = userTableTranslators.get(clazz);
            if (translator != null) {
                return translator;
            }
            Constructor<? extends QConfigTableTranslator> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            QConfigTableTranslator newTranslator = constructor.newInstance();
            userTableTranslators.put(clazz, newTranslator);
            return newTranslator;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static QConfigTranslator getInternalTranslator(Type type) {
        return parseTranslator(type, null, new AtomicBoolean(false));
    }

    private static Type correctGeneric(Type type) {
        if (Object.class.equals(type)) {
            return String.class;
        }
        return type;
    }

    private static class MapTranslator extends QConfigTranslator<Object> {

        private final QConfigTranslator keyTranslator;

        private final QConfigTranslator valueTranslator;

        private MapTranslator(QConfigTranslator keyTranslator, QConfigTranslator valueTranslator) {
            this.keyTranslator = keyTranslator;
            this.valueTranslator = valueTranslator;
        }

        @Override
        public Object translate(String value) {
            if (Strings.isNullOrEmpty(value)) {
                return ImmutableMap.of();
            }

            Map<String, String> map = MAP_SPLITTER.split(value);
            Map<Object, Object> temp = Maps.newHashMap();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                temp.put(keyTranslator.translate(entry.getKey()), valueTranslator.translate(entry.getValue()));
            }
            return ImmutableMap.copyOf(temp);
        }
    }

    private static class ArrayTranslator extends QConfigTranslator<Object> {

        private final QConfigTranslator valueTranslator;

        private final Class<?> clazz;

        public ArrayTranslator(Class clazz, QConfigTranslator valueTranslator) {
            this.valueTranslator = valueTranslator;
            this.clazz = clazz;
        }

        @Override
        public Object translate(String value) {
            if (Strings.isNullOrEmpty(value)) {
                return Array.newInstance(clazz, 0);
            }
            List<String> values = COMMA_SPLITTER.splitToList(value);
            Object result = Array.newInstance(clazz, values.size());

            for (int i = 0; i < values.size(); i++) {
                Array.set(result, i, valueTranslator.translate(values.get(i)));
            }

            return result;
        }
    }
}
