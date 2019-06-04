package qunar.tc.qconfig.client;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-11-24
 * Time: 下午12:45
 */
public class Numbers {

    /**
     * 10进制整数的容错转换,无法确认的情况下返回 0
     *
     * @param str 整数的字符串表现形式
     * @return long
     * @see Numbers#toLong(String, long);
     */
    public static long toLong(String str) {
        return toLong(str, 0L);
    }

    /**
     * @param str 10进制整数的容错转换,无法确认的情况下返回 def
     * @param def 默认值
     * @return long
     * @see Long#parseLong(String);
     */
    public static long toLong(String str, long def) {
        if (str == null)
            return def;
        try {
            return Long.parseLong(str, 10);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * 10进制整数的容错转换,无法确认的情况下返回 0
     *
     * @param str 整数的字符串表现形式
     * @return int
     * @see Numbers#toInt(String, int);
     */
    public static int toInt(String str) {
        return toInt(str, 0);
    }

    /**
     * @param str 10进制整数的容错转换,无法确认的情况下返回 def
     * @param def 默认值
     * @return int
     * @see Integer#parseInt(String);
     */
    public static int toInt(String str, int def) {
        if (str == null)
            return def;
        try {
            return Integer.parseInt(str, 10);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
