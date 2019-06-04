package qunar.tc.qconfig.admin.event;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.annotation.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-26
 * Time: 下午2:23
 */
public class EventBusRegisterBeanPostProcessor implements BeanPostProcessor {

    @Resource
    private EventBus eventBus;

    @Resource
    private AsyncEventBus asyncEventBus;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof CandidateDTOChangeListener) {
            eventBus.register(bean);
        } else if (bean instanceof PermissionChangeListener) {
            eventBus.register(bean);
        } else if (bean instanceof ProfileCreatedListener) {
            eventBus.register(bean);
        } else if (bean instanceof PublicStatusChangeListener) {
            eventBus.register(bean);
        } else if (bean instanceof ReferenceChangeListener) {
            eventBus.register(bean);
        } else if (bean instanceof CurrentConfigChangedListener) {
            if (bean instanceof CandidateDTOPushListener) {
                eventBus.register(bean);
            }
            asyncEventBus.register(bean);
        }

        return bean;
    }
}
