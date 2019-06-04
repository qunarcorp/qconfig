package qunar.tc.qconfig.client.spring;

import qunar.tc.qconfig.client.impl.QConfigTranslator;
import qunar.tc.qconfig.client.impl.TrivialTranslator;

import java.lang.annotation.*;

/**
 * @author zhenyu.nie created on 2018 2018/8/1 19:18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.FIELD})
public @interface QConfigField {

    Class<? extends QConfigTranslator> value() default TrivialTranslator.class;

    String key() default "";
}
