package qunar.tc.qconfig.client.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

class FieldSetter implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(FieldSetter.class);

    private volatile Object value;

    private final Util.File file;

    private final Field field;

    FieldSetter(Util.File file, Field field) {
        this.file = file;
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    public String getType() {
        return "field";
    }

    @Override
    public Class<?> getClazz() {
        return field.getDeclaringClass();
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Util.File getFile() {
        return file;
    }

    @Override
    public void act(Object bean, Object newValue, QConfigLogLevel logLevel) {
        try {
            field.set(bean, newValue);
            LogUtil.log(this, logLevel, this.value, newValue);
            this.value = newValue;
        } catch (Exception e) {
            LOG.error("receive qconfig change error, class {}, field {}, file [{}], group [{}]", getClazz().getName(), field.getName(), file.file, file.group, e);
            throw new RuntimeException(e);
        }
    }
}