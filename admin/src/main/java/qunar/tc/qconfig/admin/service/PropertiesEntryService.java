package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.exception.ModifiedException;
import qunar.tc.qconfig.admin.model.PropertiesEntry;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;

import java.util.List;
import java.util.Set;

/**
 * @author keli.wang
 */
public interface PropertiesEntryService {
    /**
     * 若snapshot中保存的是properties文件，则将文件中的key-value对解析出来并保存，方便搜索。
     * 保存时会与以前的数据进行比对，删除properties文件中已被删除的key。
     */
    void saveEntries(final CandidateSnapshot snapshot) throws ModifiedException;

    /**
     * 若snapshot中保持的是properties文件，则将properties文件中包含的所有key-value对标记为不可搜索。
     * 标记为不可搜索就相当与被删除了。
     */
    void removeEntries(final CandidateSnapshot snapshot) throws ModifiedException;

    /**
     * 通过snapshot中的StatusType来决定执行saveEntries还是removeEntries。
     * {@code StatusType.DELETE} -> removeEntries
     * {@code StatusType.PUBLISH} -> saveEntries
     */
    void handleSnapshotByStatus(final CandidateSnapshot snapshot) throws ModifiedException;

    /**
     * 按照(key, groups, profile)来搜索相关PropertiesEntry
     */
    List<PropertiesEntry> searchEntries(final String key,
                                        final Set<String> groups,
                                        final String profile,
                                        final int pageNo,
                                        final int pageSize);

    int countEntries(final String key, final Set<String> groups, final String profile);

    /**
     * 搜索(groups, profile)下面的所用引用文件里的key对应的PropertiesEntry
     */
    List<PropertiesEntry> searchRefEntries(final String key,
                                           final Set<String> groups,
                                           final String profile);
}
