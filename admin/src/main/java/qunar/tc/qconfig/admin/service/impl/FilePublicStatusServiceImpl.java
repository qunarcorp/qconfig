package qunar.tc.qconfig.admin.service.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.FilePublicStatusDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.service.FilePublicStatusService;
import qunar.tc.qconfig.admin.support.CheckUtil;
import qunar.tc.qconfig.common.util.PublicType;
import qunar.tc.qconfig.servercommon.bean.ConfigMetaWithoutProfile;
import qunar.tc.qconfig.servercommon.bean.PublicConfigInfo;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2016 2016/5/3 15:58
 */
@Service
public class FilePublicStatusServiceImpl implements FilePublicStatusService {

    @Resource
    private FilePublicStatusDao filePublicStatusDao;

    // todo: 这个感觉写错了啊，不过好像也没有用到的地方
    @Override
    public boolean isReference(ConfigMetaWithoutProfile configMetaWithoutProfile) {
        CheckUtil.checkLegalGroup(configMetaWithoutProfile.getGroup());
        CheckUtil.checkLegalDataId(configMetaWithoutProfile.getDataId());
        PublicType publicType = filePublicStatusDao.getPublicType(configMetaWithoutProfile);
        if (publicType == null) {
            return false;
        }
        return publicType.isReference();
    }

    @Override
    public boolean isPublic(ConfigMetaWithoutProfile configMetaWithoutProfile) {
        CheckUtil.checkLegalGroup(configMetaWithoutProfile.getGroup());
        CheckUtil.checkLegalDataId(configMetaWithoutProfile.getDataId());
        PublicType publicType = filePublicStatusDao.getPublicType(configMetaWithoutProfile);
        if (publicType == null) {
            return false;
        }
        return publicType.isPublic();
    }

    @Override
    public boolean isInherit(ConfigMetaWithoutProfile configMetaWithoutProfile) {
        CheckUtil.checkLegalGroup(configMetaWithoutProfile.getGroup());
        CheckUtil.checkLegalDataId(configMetaWithoutProfile.getDataId());
        PublicType publicType = filePublicStatusDao.getPublicType(configMetaWithoutProfile);
        if (publicType == null) {
            return false;
        }
        return publicType.isInherit();
    }

    @Override
    public boolean isRest(ConfigMetaWithoutProfile configMetaWithoutProfile) {
        CheckUtil.checkLegalGroup(configMetaWithoutProfile.getGroup());
        CheckUtil.checkLegalDataId(configMetaWithoutProfile.getDataId());
        PublicType publicType = filePublicStatusDao.getPublicType(configMetaWithoutProfile);
        if (publicType == null) {
            return false;
        }
        return publicType.isRest();
    }

    @Override
    public void setPublic(ConfigMetaWithoutProfile configMetaWithoutProfile) {
        setPublicType(configMetaWithoutProfile, PublicType.PUBLIC_MASK);
    }

    @Override
    public void batchSetPublic(List<CandidateDTO> candidateDTOList) {
        batchSetPublicType(candidateDTOList, PublicType.PUBLIC_MASK);
    }

    @Override
    public void setInherit(ConfigMetaWithoutProfile configMetaWithoutProfile) {
        setPublicType(configMetaWithoutProfile, PublicType.INHERIT_MASK | PublicType.PUBLIC_MASK);//继承默认是public的
    }

    @Override
    public void setRest(ConfigMetaWithoutProfile configMetaWithoutProfile) {
        setPublicType(configMetaWithoutProfile, PublicType.REST_MASK | PublicType.PUBLIC_MASK);//rest默认也是public的
    }


    private void setPublicType(ConfigMetaWithoutProfile configMetaWithoutProfile, int type) {
        CheckUtil.checkLegalGroup(configMetaWithoutProfile.getGroup());
        CheckUtil.checkLegalDataId(configMetaWithoutProfile.getDataId());
        PublicType publicType = filePublicStatusDao.getPublicType(configMetaWithoutProfile);
        if (publicType != null) {
            filePublicStatusDao.insert(new ConfigMetaWithoutProfile(configMetaWithoutProfile.getGroup(), configMetaWithoutProfile.getDataId()), publicType.code() | type);
        } else {
            filePublicStatusDao.insert(new ConfigMetaWithoutProfile(configMetaWithoutProfile.getGroup(), configMetaWithoutProfile.getDataId()), type);
        }
    }

    void batchSetPublicType(List<CandidateDTO> candidateDTOList, int type) {
        List<PublicConfigInfo> publicConfigInfoList = Lists.newLinkedList();
        for (CandidateDTO candidateDTO : candidateDTOList) {
            PublicConfigInfo publicConfigInfo = new PublicConfigInfo(candidateDTO.getGroup(), candidateDTO.getDataId());
            PublicType publicType = filePublicStatusDao.getPublicType(publicConfigInfo.getConfigMetaWithoutProfile());
            if (publicType != null) {
                publicConfigInfo.setPublicType(new PublicType(publicType.code() | type));
            } else {
                publicConfigInfo.setPublicType(new PublicType(type));
            }
            publicConfigInfoList.add(publicConfigInfo);
        }
        filePublicStatusDao.batchSetPublic(publicConfigInfoList);
    }

    @Override
    public Set<String> getPublicFileNames(String group) {
        return ImmutableSet.copyOf(filePublicStatusDao.selectPublicDataIds(group));
    }

    @Override
    public Set<String> getInheritFileNames(String group) {
        return ImmutableSet.copyOf(filePublicStatusDao.selectDataIds(group, PublicType.INHERIT_MASK));
    }
}
