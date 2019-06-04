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
 * Date: 14-6-27 Time: 下午3:10
 * 
 * @author: xiao.liang
 * @description:
 */
public interface ReferenceService {

    ReferenceInfo getReferenceInfo(String group, String profile);

    ReferenceInfo getReferenceInfo(String group, String profile, String groupLike, String dataIdLike, int page, int pageSize, boolean pagination);

    int addReference(Reference reference) throws ModifiedException, ConfigExistException;

    int removeReference(Reference reference);

    List<ReferenceLog> findConfigsRefer(ConfigMeta meta, RefType refType);

    ConfigMeta findReference(ConfigMeta meta);

    List<Reference> findEverReferences(String group, String profile);

    int beReferenceCount(ConfigMeta refMeta);

    int beInheritCount(ConfigMeta refMeta);

    List<RelativeReference> searchRelative(String group);
}
