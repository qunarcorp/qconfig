package qunar.tc.qconfig.server.support;

/**
 * @author zhenyu.nie created on 2014 2014/7/8 14:33
 */
public class AddressUtil {

    public static final String INVALID_IP = "0.0.0.0";

    public static boolean isLegalPortInput(int port) {
        return port >= 0 && port <= 65535;
    }

    /**
     * 这里请自行根据规则解析
     *
     * @param ip ip
     * @return ip对应机房
     */
    public static String roomOf(final String ip) {
        return "";
    }
}
