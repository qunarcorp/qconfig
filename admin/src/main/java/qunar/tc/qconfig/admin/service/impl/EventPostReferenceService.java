package qunar.tc.qconfig.admin.service.impl;

import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.event.ReferenceNotifyBean;
import qunar.tc.qconfig.admin.exception.ConfigExistException;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.service.ReferenceService;
import qunar.tc.qconfig.admin.service.ReferenceServiceDecorator;
import qunar.tc.qconfig.servercommon.bean.RefChangeType;

import javax.annotation.Resource;

/**
 * @author zhenyu.nie created on 2014 2014/7/7 13:33
 */
@Service("eventPostReferenceService")
public class EventPostReferenceService extends ReferenceServiceDecorator implements ReferenceService {

    @Resource
    private EventBus eventBus;

    @Autowired
    public EventPostReferenceService(@Qualifier("referenceService") ReferenceService delegate) {
        super(delegate);
    }

    @Override
    public int addReference(Reference reference) throws ModifiedException, ConfigExistException {
        int rows = super.addReference(reference);

        if (rows != 0) {
            eventBus.post(new ReferenceNotifyBean(reference, RefChangeType.ADD));
        }

        return rows;
    }

    @Override
    public int removeReference(Reference reference) {
        int rows = super.removeReference(reference);

        if (rows != 0) {
            eventBus.post(new ReferenceNotifyBean(reference, RefChangeType.REMOVE));
        }

        return rows;
    }
}
