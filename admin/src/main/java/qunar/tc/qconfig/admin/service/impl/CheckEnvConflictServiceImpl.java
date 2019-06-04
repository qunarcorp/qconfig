package qunar.tc.qconfig.admin.service.impl;

import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.CandidateDao;
import qunar.tc.qconfig.admin.dao.ReferenceDao;
import qunar.tc.qconfig.admin.dao.impl.InheritConfigDaoImpl;
import qunar.tc.qconfig.admin.model.Conflict;
import qunar.tc.qconfig.admin.model.Reference;
import qunar.tc.qconfig.admin.service.CheckEnvConflictService;
import qunar.tc.qconfig.common.bean.Candidate;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author zhenyu.nie created on 2014 2014/7/2 17:10
 */
@Service
public class CheckEnvConflictServiceImpl implements CheckEnvConflictService {

    @Resource
    private CandidateDao candidateDao;

    @Resource
    private ReferenceDao referenceDao;

    @Resource
    private InheritConfigDaoImpl inheritConfigDao;

    @Override
    public Optional<Conflict> getConflict(ConfigMeta configMeta) {
        Reference ref = referenceDao.findEverReference(configMeta);
        if (ref != null) {
            return Optional.of(new Conflict(new Candidate(configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile()), Conflict.Type.REF));
        }

        Candidate candidate = candidateDao.find(configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile());
        if (candidate != null) {
            return Optional.of(new Conflict(candidate, Conflict.Type.EXIST));
        }

        return Optional.empty();
    }

    public Optional<Conflict> parentFileExistsInOtherGroup(ConfigMeta configMeta) {
            if(inheritConfigDao.parentFileExistsInOtherGroup(configMeta.getGroup(), configMeta.getDataId())) {
              return Optional.of(new Conflict(new Candidate(configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile()), Conflict.Type.EXIST));
            } else {
             return Optional.empty();
            }
    }
}
