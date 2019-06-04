package qunar.tc.qconfig.client.spring;

import java.lang.annotation.*;

/**
 * @author zhenyu.nie created on 2018 2018/8/15 16:47
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface DisableQConfig {
}
