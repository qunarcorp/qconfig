package qunar.tc.qconfig.client.spring;

import java.lang.annotation.*;

/**
 * User: zhaohuiyu
 * Date: 6/5/14
 * Time: 4:10 PM
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface QConfig {
    String value();

    QConfigLogLevel logLevel() default QConfigLogLevel.off;
}
