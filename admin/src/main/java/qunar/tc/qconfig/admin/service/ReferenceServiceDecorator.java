package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.exception.ConfigExistException;
import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.model.ReferenceInfo;
import qunar.tc.qconfig.admin.model.ReferenceLog;
import qunar.tc.qconfig.admin.model.RelativeReference;
import qunar.tc.qconfig.common.util.RefType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/7/7 13:31
 */
public class ReferenceServiceDecorator implements ReferenceService {

    private ReferenceService delegate;

    public ReferenceServiceDecorator(ReferenceService delegate) {
        this.delegate = delegate;
    }

    @Override
    public ReferenceInfo getReferenceInfo(String group, String profile) {
        return delegate.getReferenceInfo(group, profile);
    }

    @Override
    public ReferenceInfo getReferenceInfo(String group, String profile, String groupLike, String dataIdLike, int page, int pageSize, boolean pagination) {
        return delegate.getReferenceInfo(group, profile, groupLike, dataIdLike, page, pageSize, pagination);
    }

    @Override
    public int addReference(Reference reference) throws ModifiedException, ConfigExistException {
        return delegate.addReference(reference);
    }

    @Override
    public int removeReference(Reference reference) {
        return delegate.removeReference(reference);
    }

    @Override
    public List<ReferenceLog> findConfigsRefer(ConfigMeta meta, RefType refType) {
        return delegate.findConfigsRefer(meta, refType);
    }

    @Override
    public ConfigMeta findReference(ConfigMeta meta) {
        return delegate.findReference(meta);
    }

    @Override
    public List<Reference> findEverReferences(String group, String profile) {
        return delegate.findEverReferences(group, profile);
    }

    @Override
    public int beReferenceCount(ConfigMeta refMeta) {
        return delegate.beReferenceCount(refMeta);
    }

    @Override
    public int beInheritCount(ConfigMeta refMeta) {
        return delegate.beInheritCount(refMeta);
    }

    @Override
    public List<RelativeReference> searchRelative(String group) {
        return delegate.searchRelative(group);
    }
}
