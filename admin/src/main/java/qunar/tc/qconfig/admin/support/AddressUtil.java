package qunar.tc.qconfig.admin.support;

/**
 * @author zhenyu.nie created on 2014 2014/7/17 18:08
 */
public class AddressUtil {

    public static boolean canHttpPushPort(int port) {
        return port > 0 && port <= 65535;
    }

    public static boolean canPushPort(int port) {
        return port >= 0 && port <= 65535;
    }
}
