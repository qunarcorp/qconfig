package qunar.tc.qconfig.client.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author yiqun.fan create on 17-2-20.
 */
@Configuration
@Import(QConfigAutoConfiguration.Register.class)
public class QConfigAutoConfiguration {
    private static final String QCONFIG_ANNOTATION = "QCONFIG_ANNOTATION";
    private static final String QCONFIG_PROPERTY_SOURCE_ANNOTATION = "QCONFIG_PROPERTY_SOURCE_ANNOTATION";

    public static class Register implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
            if (!registry.containsBeanDefinition(QCONFIG_ANNOTATION)) {
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(QConfigAnnotationProcessor.class);
                beanDefinition.setRole(2);
                beanDefinition.setSynthetic(true);
                registry.registerBeanDefinition(QCONFIG_ANNOTATION, beanDefinition);
            }
            if (!registry.containsBeanDefinition(QCONFIG_PROPERTY_SOURCE_ANNOTATION)) {
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(QConfigPropertySourceAnnotationProcessor.class);
                beanDefinition.setRole(2);
                beanDefinition.setSynthetic(true);
                registry.registerBeanDefinition(QCONFIG_PROPERTY_SOURCE_ANNOTATION, beanDefinition);
            }
        }
    }
}
