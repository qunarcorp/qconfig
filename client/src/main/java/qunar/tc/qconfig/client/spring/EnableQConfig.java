package qunar.tc.qconfig.client.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by zhaohui.yu
 * 9/22/17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(QConfigAutoConfiguration.Register.class)
public @interface EnableQConfig {
}
