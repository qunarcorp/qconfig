package qunar.tc.qconfig.admin.support;

import com.google.common.base.Preconditions;

/**
 * @author keli.wang
 */
public class SQLUtil {

    public static String generateQuestionMarks(final int size) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; ++i) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append("?");
        }
        return sb.toString();
    }

    public static String generateStubs(int size) {
        Preconditions.checkArgument(size > 0);
        StringBuilder sb = new StringBuilder(size * 2 + 1);
        sb.append('(');
        for (int i = 0; i < size; ++i) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * 生成多组参数占位符，例如:groups=2, size=3, 则返回"((?,?,?),(?,?,?))"
     * @param groups
     * @param size
     * @return
     */
    public static String generateGroupStubs(int groups, int size) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (int i = 0; i< groups; ++i) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append('(');
            for (int j = 0; j < size; ++j) {
                if (j != 0) {
                    sb.append(',');
                }
                sb.append('?');
            }
            sb.append(')');
        }
        sb.append(')');
        return sb.toString();
    }

    public static String escapeWildcards(String input) {
        return input.replaceAll("([\\%\\_])", "\\\\$1");
    }
}
