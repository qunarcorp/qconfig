package qunar.tc.qconfig.admin.web.rest;

import java.security.MessageDigest;
import java.util.Objects;

/**
 * Created by chenjk on 2018/2/27.
 */
public class RestUtil {

    private static final String SALT = "mysalt,delicious";

    public static String getToken(String groupId) {
        return getMD5(groupId + SALT);
    }

    //生成token
    public static String getToken(String groupId, String targetGroupId) {
        return getMD5(groupId + SALT + targetGroupId);
    }

    //token 校验
    public static boolean checkToken(String groupId, String targetGroupId, String token) {
        return Objects.equals(getToken(groupId, targetGroupId), token);
    }

    public static boolean checkToken(String groupId, String token) {
        return Objects.equals(getToken(groupId), token);
    }

    //生成MD5
    public static String getMD5(String message) {
        String md5 = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");  // 创建一个md5算法对象
            byte[] messageByte = message.getBytes("UTF-8");
            byte[] md5Byte = md.digest(messageByte);              // 获得MD5字节数组,16*8=128位
            md5 = bytesToHex(md5Byte);                            // 转换为16进制字符串
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5;
    }

    // 二进制转十六进制
    public static String bytesToHex(byte[] bytes) {
        StringBuffer hexStr = new StringBuffer();
        int num;
        for (int i = 0; i < bytes.length; i++) {
            num = bytes[i];
            if (num < 0) {
                num += 256;
            }
            if (num < 16) {
                hexStr.append("0");
            }
            hexStr.append(Integer.toHexString(num));
        }
        return hexStr.toString().toUpperCase();
    }
}
