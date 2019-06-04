package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import qunar.tc.qconfig.common.util.Constants;

import java.util.Set;

/**
 * @author zhenyu.nie created on 2016/3/29 15:57
 */
public class RowValueChecker {

    private Set<String> rows = Sets.newHashSet();

    public void check(String row) {
        Preconditions.checkArgument(row.length() == row.trim().length(), "行名前后不能有空格, [%s]", row);
        Preconditions.checkArgument(!row.contains(Constants.ROW_COLUMN_SEPARATOR), "行名不能有斜杠, [%s]", row);
        boolean added = rows.add(row);
        Preconditions.checkArgument(added, "重复的行名[%s]", row);
    }
}
