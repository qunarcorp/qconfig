package qunar.tc.qconfig.admin.service;

/**
 * @author keli.wang
 */
public interface EncryptKeyBlacklistService {

    /**
     * 判断关键字是否在黑名单里
     * @param key 需要判断的关键字
     * @return 是否在黑名单中
     */
    boolean inBlacklist(final String key);
}
