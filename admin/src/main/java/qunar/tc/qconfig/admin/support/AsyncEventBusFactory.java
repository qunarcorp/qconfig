package qunar.tc.qconfig.admin.support;


import com.google.common.eventbus.AsyncEventBus;
import qunar.tc.qconfig.common.support.concurrent.NamedThreadFactory;

import java.util.concurrent.Executors;

/**
 * @author keli.wang
 */
public class AsyncEventBusFactory {
    public static AsyncEventBus newAsyncEventBus() {
        return new AsyncEventBus("async-event", Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("qconfig-async-event")));
    }
}
