package qunar.tc.qconfig.server.config.longpolling.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 18:27
 */
public class TimeoutServletListener implements AsyncListener {

    private static final Logger logger = LoggerFactory.getLogger(TimeoutServletListener.class);

    private final AsyncContextHolder contextHolder;

    public TimeoutServletListener(AsyncContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {

    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        contextHolder.completeRequest(timeoutReturn);
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        logger.debug("long-polling check change error, {}", contextHolder, event.getThrowable());
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {

    }

    private static final ReturnAction timeoutReturn = new ReturnAction() {
        @Override
        public String type() {
            return "timeout";
        }

        @Override
        public void act(AsyncContext context) throws Exception {
            ((HttpServletResponse) context.getResponse()).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
    };
}
