package qunar.tc.qconfig.admin.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * @author keli.wang
 */
public class EncryptKeyBlacklist {
    // 空黑名单
    private static final EncryptKeyBlacklist EMPTY_BLACKLIST =
            new EncryptKeyBlacklist(Collections.<String>emptyList(),
                                    Collections.<String>emptyList(),
                                    Collections.<String>emptyList());

    // 若关键字包含列表中的某个字符串，则此关键字在黑名单中
    private final List<String> containsBlacklist;
    // 若关键字以列表中某个字符串为开头，则此关键字在黑名单中
    private final List<String> startWithBlacklist;
    // 若关键字以列表中某个字符串结尾，则此关键字在黑名单中
    private final List<String> endWithBlacklist;

    public static EncryptKeyBlacklist emptyBlacklist() {
        return EMPTY_BLACKLIST;
    }

    public EncryptKeyBlacklist(final List<String> containsBlacklist,
                               final List<String> startWithBlacklist,
                               final List<String> endWithBlacklist) {
        this.containsBlacklist = Lists.newArrayList(containsBlacklist);
        this.startWithBlacklist = Lists.newArrayList(startWithBlacklist);
        this.endWithBlacklist = Lists.newArrayList(endWithBlacklist);
    }

    public List<String> getContainsBlacklist() {
        return Collections.unmodifiableList(containsBlacklist);
    }

    public List<String> getStartWithBlacklist() {
        return Collections.unmodifiableList(startWithBlacklist);
    }

    public List<String> getEndWithBlacklist() {
        return Collections.unmodifiableList(endWithBlacklist);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                      .add("containsBlacklist", containsBlacklist)
                      .add("startWithBlacklist", startWithBlacklist)
                      .add("endWithBlacklist", endWithBlacklist)
                      .toString();
    }
}
