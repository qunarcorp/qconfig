package qunar.tc.qconfig.client.spring;

interface Action {

    String getType();

    Class<?> getClazz();

    String getName();

    Util.File getFile();

    void act(Object bean, Object value, QConfigLogLevel logLevel);
}
