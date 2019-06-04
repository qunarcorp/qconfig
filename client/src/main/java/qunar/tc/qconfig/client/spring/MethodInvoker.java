package qunar.tc.qconfig.client.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

class MethodInvoker implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(MethodInvoker.class);

    private volatile Object value;

    private final Util.File file;

    private final Method method;

    MethodInvoker(Util.File file, Method method) {
        this.file = file;
        this.method = method;
        method.setAccessible(true);
    }

    @Override
    public String getType() {
        return "method";
    }

    @Override
    public Class<?> getClazz() {
        return method.getDeclaringClass();
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public Util.File getFile() {
        return file;
    }

    @Override
    public void act(Object bean, Object newValue, QConfigLogLevel logLevel) {
        try {
            method.invoke(bean, newValue);

            LogUtil.log(this, logLevel, this.value, newValue);
            this.value = newValue;
        } catch (Exception e) {
            LOG.error("receive qconfig change error, class {}, method {}, file [{}], group [{}]", getClazz().getName(), method.getName(), file.file, file.group, e);
            throw new RuntimeException(e);
        }
    }
}