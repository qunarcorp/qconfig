package qunar.tc.qconfig.server.config.qfile.impl;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import qunar.tc.qconfig.server.config.inherit.InheritMeta;
import qunar.tc.qconfig.server.config.qfile.QFile;
import qunar.tc.qconfig.server.config.qfile.QFileFactory;
import qunar.tc.qconfig.server.config.inherit.InheritJudgementAdaptor;
import qunar.tc.qconfig.server.config.inherit.InheritMetaBuilder;
import qunar.tc.qconfig.server.config.inherit.InheritUtil;
import qunar.tc.qconfig.server.support.context.ClientInfoService;
import qunar.tc.qconfig.server.config.ConfigInfoService;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.util.PriorityUtil;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhenyu.nie created on 2016 2016/8/24 17:09
 */
public abstract class AbstractQFileFactory implements QFileFactory {

    @Resource
    private ClientInfoService clientInfoService;

    protected ClientInfoService getClientInfoService() {
        return clientInfoService;
    }

    public Optional<QFile> internalCreate(ConfigMeta meta, ConfigMeta candidate, ConfigInfoService configInfoService) {
        if (configInfoService.getVersion(candidate).isPresent()) {
            if (meta.equals(candidate)) {
                return Optional.of(createFile(candidate));
            } else {
                QFile sharedFile = createFile(candidate);
                return Optional.of(createShareFile(meta, sharedFile));
            }
        }

        Optional<ConfigMeta> reference = configInfoService.getReference(candidate);
        if (reference.isPresent()) {
            if (meta.equals(candidate)) {
                QFile referencedFile = createFile(reference.get());
                return Optional.of(createRefFile(meta, referencedFile));
            } else {
                QFile referencedFile = createFile(reference.get());
                QFile sharedFile = createRefFile(candidate, referencedFile);
                return Optional.of(createShareFile(meta, sharedFile));
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<QFile> create(ConfigMeta meta, ConfigInfoService configInfoService) {
        if (!Objects.equal(meta.getGroup(), clientInfoService.getGroup())) {
            ConfigMeta childRequestMeta = new ConfigMeta(clientInfoService.getGroup(), meta.getDataId(), meta.getProfile());
            InheritMeta fuzzyInheritMeta = InheritMetaBuilder.builder().parent(meta).child(childRequestMeta).build();
            Optional<InheritMeta> inheritMeta = InheritUtil.getInheritRelationWithFuzzyRelation(fuzzyInheritMeta, InheritJudgementAdaptor.create(configInfoService), clientInfoService.getRoom());
            if (inheritMeta.isPresent()) {
                QFile childFile = createFile(inheritMeta.get().getChild());
                QFile parentFile = createFile(inheritMeta.get().getParent());
                return Optional.of(createInheritFile(meta, childFile, parentFile));
            }
        }

        List<ConfigMeta> priorityList = PriorityUtil.createPriorityListWithRoom(meta, clientInfoService.getRoom());
        for (ConfigMeta candidate : priorityList) {
            Optional<QFile> tmpQFile = internalCreate(meta, candidate, configInfoService);
            if (tmpQFile.isPresent()) {
                return tmpQFile;
            }
        }
        return Optional.absent();
    }

    protected abstract QFile createFile(ConfigMeta meta);

    protected abstract QFile createShareFile(ConfigMeta meta, QFile sharedFile);

    protected abstract QFile createRefFile(ConfigMeta meta, QFile referencedFile);

    protected abstract QFile createInheritFile(ConfigMeta meta, QFile childFile, QFile inheritedFile);
}
