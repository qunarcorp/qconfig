package qunar.tc.qconfig.common.codec;

/**
 * 加解密接口
 *
 * Created by chenjk on 2017/8/29.
 */
public interface Codec {

    /**
     * 加解密算法名称，用于标记一个加解密算法
     *
     * @return
     */
    String name();

    /**
     * 解密
     *
     * @parm encodedStr
     *
     * @throws Exception
     *
     * @return
     */
    String decode(String encodedStr) throws Exception;

    /**
     * 加密
     *
     * @param rawStr
     *
     * @param name
     *
     * @throws Exception
     *
     * @return
     */
    String encode(String rawStr, String name) throws Exception;
}
