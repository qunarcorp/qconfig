package qunar.tc.qconfig.client.spring;

import java.lang.annotation.*;

/**
 * @author yiqun.fan create on 17-3-1.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface QConfigPropertySource {

    String[] files();

    boolean trimValue() default true;

    long timeout() default 60000;

    boolean ignoreFileNotFound() default false;
}
