package qunar.tc.qconfig.common.codec;

import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Base64;

/**
 * Base64加解密
 * <p>
 * Created by chenjk on 2017/8/30.
 */
public class Rc4 implements Codec {

    public static final String NAME = "RC4";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String decode(String encodedStr) throws Exception {
        if (Strings.isNullOrEmpty(encodedStr)) {
            return "";
        }
        byte[] sources = Base64.decodeBase64(encodedStr);
        int datalen = sources.length;
        int keyLen = (int) sources[0];
        int len = datalen - keyLen - 1;
        byte[] datas = new byte[len];
        int offset = datalen - 1;
        int i = 0;
        int j = 0;
        byte t;
        for (int o = 0; o < len; o++) {
            i = (i + 1) % keyLen;
            j = (j + sources[offset - i]) % keyLen;
            t = sources[offset - i];
            sources[offset - i] = sources[offset - j];
            sources[offset - j] = t;
            datas[o] = (byte) (sources[o + 1] ^ sources[offset - ((sources[offset - i] + sources[offset - j]) % keyLen)]);
        }
        return new String(datas);
    }

    @Override
    public String encode(String rawStr, String name) throws Exception {
        if (rawStr == null || rawStr.length() == 0) {
            return "";
        }
        if (name == null || name.length() == 0) {
            name = "DataSourceKey";
        }
        byte[] sources = rawStr.getBytes();
        byte[] names = name.getBytes();
        int len = sources.length;
        int keyLen = names.length;
        int j = 0;
        int i = 0;

        if (keyLen > 128) {
            keyLen = 127;
        }
        byte[] rltBytes = new byte[len + keyLen + 1];
        rltBytes[0] = (byte) keyLen;
        for (int offset = len + keyLen; offset > len; offset--) {
            rltBytes[offset] = names[i++];
        }
        i = 0;
        for (int offset = 0; offset < len; offset++) {
            i = (i + 1) % keyLen;
            j = (j + names[i]) % keyLen;
            byte b = names[i];
            names[i] = names[j];
            names[j] = b;
            rltBytes[offset + 1] = (byte) (sources[offset] ^ names[(names[i] + names[j]) % keyLen]);
        }
        return Base64.encodeBase64String(rltBytes);
    }
}