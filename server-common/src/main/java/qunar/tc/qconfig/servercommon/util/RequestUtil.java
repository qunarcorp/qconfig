package qunar.tc.qconfig.servercommon.util;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by dongcao on 2018/8/15.
 */
public class RequestUtil {

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";


    private static final String HEADER_X_REAL_IP = "X-Real-IP";
    /**
     * 获取客户端IP
     *
     * @param req
     * @return
     */
    public static String getRealIP(HttpServletRequest req) {
        // nginx, squid 等反向代理一般利用此字段来传递中间链路上的节点ip
        // X-Forwarded-For[0]一般表示客户端，即代理看到的对端remote address
        // 用逗号+空格分隔，代理将上一跳追加到此header中
        String ip = req.getHeader(HEADER_X_FORWARDED_FOR);
        if (!emptyIp(ip)) {
            int pos = ip.indexOf(',');
            if (pos >= 0) {
                ip = ip.substring(0, pos);
            }
        } else {
            // 反向代理会把自己的上一跳放入X-Real-IP，不追加
            ip = req.getHeader(HEADER_X_REAL_IP);
            if (emptyIp(ip)) {
                ip = req.getRemoteAddr();
            }
        }

        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    private static boolean emptyIp(String ip) {
        return StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip);
    }

}
