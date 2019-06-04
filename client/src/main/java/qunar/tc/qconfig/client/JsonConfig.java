package qunar.tc.qconfig.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import qunar.tc.qconfig.client.impl.AbstractConfiguration;
import qunar.tc.qconfig.client.impl.ConfigEngine;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/5/23 17:56
 */
public class JsonConfig<T> extends AbstractConfiguration<T> {

    private static final DataLoader loader = ConfigEngine.getInstance();

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        mapper.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    private static final TypeFactory typeFactory = mapper.getTypeFactory();

    private JavaType type;

    private JsonConfig(ParameterizedClass clazz, Feature feature, String fileName) {
        super(feature, fileName);
        type = typeOf(clazz);
    }

    public static <U> JsonConfig<U> get(String groupName, String fileName, Feature feature, Class<U> clazz) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fileName), "fileName必须提供");
        return (JsonConfig<U>) loader.load(groupName, fileName, feature, new Generator<U>(ParameterizedClass.of(clazz)));
    }

    public static <U> JsonConfig<U> get(String groupName, String fileName, Feature feature, ParameterizedClass clazz) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fileName), "fileName必须提供");
        return (JsonConfig<U>) loader.load(groupName, fileName, feature, new Generator<U>(clazz));
    }

    public static <U> JsonConfig<U> get(String fileName, Feature feature, Class<U> clazz) {
        return get(null, fileName, feature, clazz);
    }

    public static <U> JsonConfig<U> get(String fileName, Feature feature, ParameterizedClass clazz) {
        return get(null, fileName, feature, clazz);
    }

    public static <U> JsonConfig<U> get(String fileName, Class<U> clazz) {
        return get(null, fileName, null, clazz);
    }

    public static <U> JsonConfig<U> get(String fileName, ParameterizedClass clazz) {
        return get(null, fileName, null, clazz);
    }

    public T current() {
        waitFistLoad();
        return current.get();
    }

    @Override
    public T emptyData() {
        return null;
    }

    @Override
    public T parse(String data) throws IOException {
        return mapper.readValue(data, type);
    }

    private JavaType typeOf(ParameterizedClass clazz) {
        if (clazz.parameters.isEmpty()) {
            return typeFactory.uncheckedSimpleType(clazz.clazz);
        }

        JavaType[] parameters = new JavaType[clazz.parameters.size()];
        for (int i = 0; i < clazz.parameters.size(); ++i) {
            parameters[i] = typeOf(clazz.parameters.get(i));
        }
        return typeFactory.constructParametricType(clazz.clazz, parameters);
    }

    public static class ParameterizedClass {

        private Class clazz;

        private List<ParameterizedClass> parameters;

        public ParameterizedClass(Class clazz) {
            this(clazz, ImmutableList.<ParameterizedClass>of());
        }

        public ParameterizedClass(Class clazz, Class... parameters) {
            this.clazz = clazz;
            this.parameters = Lists.newArrayListWithCapacity(parameters.length);
            for (Class parameter : parameters) {
                this.parameters.add(ParameterizedClass.of(parameter));
            }
            Preconditions.checkNotNull(this.clazz);
            Preconditions.checkNotNull(this.parameters);
        }

        public ParameterizedClass(Class clazz, ParameterizedClass... parameters) {
            this(clazz, ImmutableList.copyOf(parameters));
        }

        public ParameterizedClass(Class clazz, Collection<ParameterizedClass> parameters) {
            this.clazz = clazz;
            if (parameters == null) {
                parameters = ImmutableList.of();
            }
            this.parameters = Lists.newArrayList(parameters);
            Preconditions.checkNotNull(this.clazz);
            Preconditions.checkNotNull(this.parameters);
        }

        public static ParameterizedClass of(Class clazz) {
            return new ParameterizedClass(clazz);
        }

        public static ParameterizedClass of(Class clazz, Class... parameters) {
            return new ParameterizedClass(clazz, parameters);
        }

        public static ParameterizedClass of(Class clazz, ParameterizedClass... parameters) {
            return new ParameterizedClass(clazz, parameters);
        }

        public static ParameterizedClass of(Class clazz, Collection<ParameterizedClass> parameters) {
            return new ParameterizedClass(clazz, parameters);
        }

        public static ParameterizedClass of(Type type) {
            if (type instanceof Class) {
                return of((Class) type);
            }
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                ParameterizedClass parameterizedClass = of(parameterizedType.getRawType());
                Type[] arguments = parameterizedType.getActualTypeArguments();
                for (Type argument : arguments) {
                    parameterizedClass.addParameter(of(argument));
                }
                return parameterizedClass;
            }
            throw new RuntimeException("不支持的类型");
        }

        public ParameterizedClass addParameter(Class parameter) {
            Preconditions.checkNotNull(parameter);
            parameters.add(new ParameterizedClass(parameter));
            return this;
        }

        public ParameterizedClass addParameter(ParameterizedClass parameter) {
            Preconditions.checkNotNull(parameter);
            parameters.add(parameter);
            return this;
        }
    }

    private static final class Generator<U> implements DataLoader.Generator<U> {

        private final ParameterizedClass clazz;

        private Generator(ParameterizedClass clazz) {
            this.clazz = clazz;
        }

        @Override
        public AbstractConfiguration<U> create(Feature feature, String fileName) {
            return new JsonConfig<U>(clazz, feature, fileName);
        }
    }
}
