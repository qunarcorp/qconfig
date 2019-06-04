package qunar.tc.qconfig.client.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static qunar.tc.qconfig.client.spring.QConfigPropertyAnnotationUtil.buildStringValueResolver;
import static qunar.tc.qconfig.client.spring.QConfigPropertyAnnotationUtil.loadQConfigFilesInAnnotation;

/**
 * @author yiqun.fan create on 17-3-1.
 */
public class QConfigPropertySourceAnnotationProcessor extends PropertyPlaceholderConfigurer implements BeanClassLoaderAware {

    private ClassLoader beanClassLoader;
    private Map<String, Properties> filenameToPropsMap = new HashMap<String, Properties>();

    public QConfigPropertySourceAnnotationProcessor() {
        setOrder(0);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        beanClassLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        super.setBeanFactory(beanFactory);
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            filenameToPropsMap = loadQConfigFilesInAnnotation((ConfigurableListableBeanFactory) beanFactory, beanClassLoader);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (filenameToPropsMap != null && !filenameToPropsMap.isEmpty()) {
            doProcessProperties(beanFactory, buildStringValueResolver(filenameToPropsMap));
        }
    }
}
