package qunar.tc.qconfig.client.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static qunar.tc.qconfig.client.spring.QConfigPropertyAnnotationUtil.buildStringValueResolver;
import static qunar.tc.qconfig.client.spring.QConfigPropertyAnnotationUtil.loadQConfigFilesInAnnotation;

/**
 * User: zhaohuiyu
 * Date: 7/5/13
 * Time: 7:57 PM
 */
class QConfigAnnotationProcessor extends PropertyPlaceholderConfigurer implements BeanPostProcessor, ApplicationContextAware, BeanClassLoaderAware {

    private boolean trimValue;

    private final Map<Class, AnnotationManager> managers = new HashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = getRealClass(bean);
        if (AnnotationUtils.isAnnotationDeclaredLocally(DisableQConfig.class, clazz)) return bean;

        AnnotationManager manager = getOrCreateManager(clazz);
        manager.init(clazz);
        manager.processBean(bean);
        return bean;
    }

    private Class<?> getRealClass(Object bean) {
        Class<?> clazz = bean.getClass();
        while (ClassUtils.isCglibProxyClass(clazz)) {
            clazz = clazz.getSuperclass();
        }
        return clazz;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    public void setTrimValue(boolean trimValue) {
        this.trimValue = trimValue;
    }

    private ClassLoader beanClassLoader;

    private Map<String, Properties> filenameToPropsMap;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        beanClassLoader = classLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
            if (beanFactory != null) {
                filenameToPropsMap = loadQConfigFilesInAnnotation(beanFactory, beanClassLoader);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (filenameToPropsMap != null && !filenameToPropsMap.isEmpty()) {
            doProcessProperties(beanFactory, buildStringValueResolver(filenameToPropsMap));
        }
    }

    private AnnotationManager getOrCreateManager(Class<?> clazz) {
        synchronized (managers) {
            AnnotationManager manager = managers.get(clazz);
            if (manager == null) {
                manager = new AnnotationManager(trimValue);
                managers.put(clazz, manager);
            }
            return manager;
        }
    }

}
