package qunar.tc.qconfig.client.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

class BeanInject implements Action {
    private static final Logger logger = LoggerFactory.getLogger(BeanInject.class);

    private final Util.File file;

    private final Class<?> clazz;
    private final AnnotationManager manager;

    BeanInject(Util.File file, Class<?> clazz, AnnotationManager manager) {
        this.file = file;
        this.clazz = clazz;
        this.manager = manager;
    }

    @Override
    public String getType() {
        return "bean";
    }

    @Override
    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public String getName() {
        return "inject";
    }

    @Override
    public Util.File getFile() {
        return file;
    }

    @Override
    public void act(Object bean, Object newValue, QConfigLogLevel logLevel) {
        try {
            LogUtil.log(this, logLevel, bean, newValue);
            for (Field field : clazz.getDeclaredFields()) {
                if (manager.getAnnotations(field).isEmpty()) {
                    field.setAccessible(true);
                    Object qconfigValue = field.get(newValue);
                    field.set(bean, qconfigValue);
                }
            }
        } catch (Exception e) {
            logger.error("set qconfig bean failOf", e);
            throw new RuntimeException("set qconfig bean failOf", e);
        }
    }
}
