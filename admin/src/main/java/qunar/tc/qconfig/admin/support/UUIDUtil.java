package qunar.tc.qconfig.admin.support;

import java.util.UUID;

public class UUIDUtil {

    public static String generate() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static void checkValid(String uuid) {
        if(uuid == null || uuid.length() != 32) {
            throw new IllegalArgumentException("非法的uuid: " + uuid);
        }
    }
}
