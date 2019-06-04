package qunar.tc.qconfig.admin.support;

/**
 * Created by pingyang.yang on 2019-02-14
 */
public class IntArrayUtil {

    public static int getSum(int [] array) {
        int result = 0;
        for (int temp : array) {
            result += temp;
        }
        return result;
    }
}
