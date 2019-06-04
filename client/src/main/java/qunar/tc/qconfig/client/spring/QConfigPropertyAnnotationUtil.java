package qunar.tc.qconfig.client.spring;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.util.StringValueResolver;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.MapConfig;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author yiqun.fan create on 17-3-28.
 */
class QConfigPropertyAnnotationUtil {
    private static final Logger logger = LoggerFactory.getLogger(QConfigPropertyAnnotationUtil.class);

    // 拉取qconfig文件超时时间
    private static final long DEFAULT_TIMEOUT_MILLISECOND = 60000;

    static Map<String, Properties> loadQConfigFilesInAnnotation(ConfigurableListableBeanFactory beanFactory, ClassLoader beanClassLoader) {
        Map<String, Properties> props = new HashMap<String, Properties>();
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            Class<?> clz = getClassByBeanDefinitionName(beanFactory, beanDefinitionName, beanClassLoader);
            if (clz == null) continue;

            QConfigPropertySource annotation = AnnotationUtils.getAnnotation(clz, QConfigPropertySource.class);
            if (annotation == null) continue;

            String[] files = annotation.files();
            List<String> filtedFiles = new ArrayList<String>();
            for (String file : files) {
                if (!Strings.isNullOrEmpty(file)) {
                    if (!props.containsKey(file)) {
                        filtedFiles.add(file);
                    }
                }
            }
            if (filtedFiles.isEmpty()) continue;

            try {
                props.putAll(load(filtedFiles, annotation.trimValue(), annotation.timeout(), annotation.ignoreFileNotFound()));
            } catch (Exception e) {
                throw new RuntimeException("从qconfig读取配置文件失败", e);
            }

        }
        return props;
    }

    private static Class<?> getClassByBeanDefinitionName(ConfigurableListableBeanFactory beanFactory, String beanDefinitionName, ClassLoader beanClassLoader) {
        if (Strings.isNullOrEmpty(beanDefinitionName)) return null;

        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
        if (beanDefinition == null) return null;

        if (beanDefinition instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) beanDefinition).hasBeanClass()) {
            return ((AbstractBeanDefinition) beanDefinition).getBeanClass();
        }

        String className = beanDefinition.getBeanClassName();
        if (!Strings.isNullOrEmpty(className) && beanClassLoader != null) {
            try {
                return beanClassLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                logger.debug("找不到类: {}", className, e);
            }
        }
        return null;
    }

    private static Map<String, Properties> load(List<String> files, boolean trimValue, long timeout, boolean ignoreFileNotFound) throws Exception {
        if (files == null || files.isEmpty()) {
            return Collections.emptyMap();
        }
        if (timeout <= 0) {
            timeout = DEFAULT_TIMEOUT_MILLISECOND;
        }

        MapConfig[] configs = new MapConfig[files.size()];
        ListenableFuture<?>[] futures = new ListenableFuture[files.size()];
        for (int i = 0; i < files.size(); ++i) {
            String file = files.get(i);
            Map.Entry<Util.File, Feature> entry = Util.parse(file, trimValue);
            configs[i] = MapConfig.get(entry.getKey().group, entry.getKey().file, entry.getValue());
            futures[i] = configs[i].initFuture();
        }

        Map<String, Properties> filenameToPropsMap = new HashMap<String, Properties>();
        ListenableFuture<?> future = ignoreFileNotFound ? Futures.successfulAsList(futures) : Futures.allAsList(futures);
        try {
            List result = (List) future.get(timeout, TimeUnit.MILLISECONDS);
            for (int i = 0; i < configs.length; ++i) {
                if (result.get(i) == null) continue;
                filenameToPropsMap.put(files.get(i), configs[i].asProperties());
            }
            return filenameToPropsMap;
        } catch (ExecutionException e) {
            logger.error("从qconfig读取配置文件失败, files: {}", files, e.getCause());
            throw e;
        } catch (TimeoutException e) {
            logger.error("从qconfig读取配置文件超时, files: {}", files, e);
            throw e;
        }
    }

    static StringValueResolver buildStringValueResolver(Map<String, Properties> filenameToPropsMap) {
        MutablePropertySources propertySources = new MutablePropertySources();
        for (Map.Entry<String, Properties> entry : filenameToPropsMap.entrySet()) {
            propertySources.addFirst(new PropertiesPropertySource(entry.getKey(), entry.getValue()));
        }
        return buildStringValueResolver(new PropertySourcesPropertyResolver(propertySources));
    }

    private static StringValueResolver buildStringValueResolver(final ConfigurablePropertyResolver propertyResolver) throws BeansException {
        final String placeholderPrefix = PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_PREFIX;
        final String placeholderSuffix = PlaceholderConfigurerSupport.DEFAULT_PLACEHOLDER_SUFFIX;
        final String valueSeparator = PlaceholderConfigurerSupport.DEFAULT_VALUE_SEPARATOR;
        propertyResolver.setPlaceholderPrefix(placeholderPrefix);
        propertyResolver.setPlaceholderSuffix(placeholderSuffix);
        propertyResolver.setValueSeparator(valueSeparator);

        return new StringValueResolver() {
            public String resolveStringValue(String strVal) {
                String key = parseKey(strVal);
                if (!propertyResolver.containsProperty(key)) {
                    return strVal;
                }
                String resolved = propertyResolver.resolveRequiredPlaceholders(strVal);
                return (resolved.equals("") ? null : resolved);
            }

            private String parseKey(String strVal) {
                if (Strings.isNullOrEmpty(strVal) || !strVal.startsWith(placeholderPrefix) || !strVal.endsWith(placeholderSuffix)) {
                    return strVal;
                }
                int index = strVal.indexOf(valueSeparator, placeholderPrefix.length());
                if (index < placeholderPrefix.length()) {
                    return strVal.substring(placeholderPrefix.length(), strVal.length() - placeholderSuffix.length());
                } else {
                    return strVal.substring(placeholderPrefix.length(), index);
                }
            }
        };
    }
}
