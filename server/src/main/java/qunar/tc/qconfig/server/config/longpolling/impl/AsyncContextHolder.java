package qunar.tc.qconfig.server.config.longpolling.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.servercommon.bean.IpAndPort;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhenyu.nie created on 2017 2017/3/27 20:16
 */
public class AsyncContextHolder {

    private static final Logger logger = LoggerFactory.getLogger(AsyncContextHolder.class);

    private volatile boolean complete = false;

    private final AsyncContext context;

    private final String ip;

    private final int port;

    public AsyncContextHolder(AsyncContext context, IpAndPort ipAndPort) {
        this.context = context;
        this.ip = ipAndPort.getIp();
        this.port = ipAndPort.getPort();
    }

    public boolean isComplete() {
        return complete;
    }

    public AsyncContext getContext() {
        return context;
    }

    // 这里这么实现是为了节省空间，长轮询listener太多，gc压力很大
    public IpAndPort getIpAndPort() {
        return new IpAndPort(ip, port);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void completeRequest(ReturnAction returnAction) {
        if (complete) {
            return;
        }

        synchronized (this) {
            if (complete) {
                return;
            }

            complete = true;
            try {
                logger.info("do return {} to [{}:{}]", returnAction.type(), ip, port);
                returnAction.act(context);
            } catch (Exception e) {
                logger.info("do return {} to [{}:{}] error", returnAction.type(), ip, port, e);
                ((HttpServletResponse) context.getResponse()).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
                context.complete();
            }
        }
    }

    @Override
    public String toString() {
        return "AsyncContextHolder{" +
                "complete=" + complete +
                ", context=" + context +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
