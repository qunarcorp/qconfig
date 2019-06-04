package qunar.tc.qconfig.admin.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.admin.monitor.Monitor;
import qunar.tc.qconfig.servercommon.service.IAlarmService;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/7/7 13:59
 */
public abstract class MailSender<T> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private IAlarmService alarmService;

    public final void sendMail(T t) {
        if (!needMail(t)) {
            return;
        }

        String title = generateTitle(t);
        String content = generateContent(t);
        Set<String> names = generateNames(t);
        logger.debug("{} send mail, title: {}, names: {}, content: {}", getClass().getName(), title, names, content);
        try {
            alarmService.sendMailAlarm(title, content, names);
        } catch (Exception e) {
            logger.error("{} send mail error, title: {}, names: {}, content: {}", getClass().getName(), title, names, content, e);
            Monitor.sendMailFailCounter.inc();
        }
    }

    protected abstract Set<String> generateNames(T t);

    protected abstract String generateContent(T t);

    protected abstract String generateTitle(T t);

    protected abstract boolean needMail(T t);
}
