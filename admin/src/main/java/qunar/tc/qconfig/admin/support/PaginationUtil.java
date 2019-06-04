package qunar.tc.qconfig.admin.support;

/**
 * 分页工具类
 *
 * Created by chenjk on 2017/8/29.
 */
public class PaginationUtil {

    //返回分页起始位置
    public static long start(long page, long pageSize) {
        return ((page - 1) * pageSize);
    }

    //返回总页数
    public static long totalPage(long count, long pageSize) {
        if (count % pageSize == 0) {
            return (count / pageSize);
        } else {
            return ((count / pageSize) + 1);
        }
    }
}
