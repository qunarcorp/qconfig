package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.impl.QConfigTableTranslator;

import java.lang.annotation.*;

/**
 * @author zhenyu.nie created on 2018 2018/8/17 13:50
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface QTableConfig {

    String value();

    QConfigLogLevel logLevel() default QConfigLogLevel.low;

    Class<? extends QConfigTableTranslator> translator() default TrivialTableTranslator.class;
}
