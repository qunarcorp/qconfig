package qunar.tc.qconfig.server.config.inherit;

import com.google.common.base.Optional;
import qunar.tc.qconfig.common.util.Environment;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.util.PriorityUtil;

import java.util.List;

/**
 * @author zhenyu.nie created on 2018 2018/3/28 17:54
 */
public class InheritUtil {

    public static Optional<InheritMeta> getInheritRelationWithFuzzyRelation(InheritMeta fuzzyMeta, InheritJudgement inheritJudgement, String room) {
        List<ConfigMeta> orderedCandidateParents = PriorityUtil.createPriorityListWithRoom(fuzzyMeta.getParent(), room);
        List<ConfigMeta> orderedCandidateChildren = PriorityUtil.createPriorityListWithRoom(fuzzyMeta.getChild(), room);

        for (ConfigMeta candidateParent : orderedCandidateParents) {
            for (ConfigMeta candidateChild : orderedCandidateChildren) {
                if (isResources(candidateChild) && !isResources(candidateParent)) {
                    //子文件为resource环境，那么不可能继承非resource环境的文件
                    continue;
                }

                InheritMeta candidateInheritMeta = InheritMetaBuilder.builder().child(candidateChild).parent(candidateParent).build();
                boolean exist = inheritJudgement.exist(candidateInheritMeta);
                if (exist) {
                    return Optional.of(candidateInheritMeta);
                }
            }
        }

        return Optional.absent();
    }

    private static boolean isResources(ConfigMeta meta) {
        return Environment.fromProfile(meta.getProfile()).isResources();
    }
}
