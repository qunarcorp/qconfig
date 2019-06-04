package qunar.tc.qconfig.admin.service;

/**
 * @author keli.wang
 * @since 2017/4/7
 */
public interface TableBatchOpWhitelistService {
    boolean allowBatchOp(final String appCode);
}
