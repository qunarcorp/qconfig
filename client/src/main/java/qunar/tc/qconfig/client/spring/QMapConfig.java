package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.impl.QConfigMapTranslator;

import java.lang.annotation.*;

/**
 * @author zhenyu.nie created on 2018 2018/8/15 19:53
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface QMapConfig {

    String value();

    String key() default "";

    String defaultValue() default "";

    QConfigLogLevel logLevel() default QConfigLogLevel.low;

    Class<? extends QConfigMapTranslator> translator() default TrivialMapTranslator.class;
}
