package qunar.tc.qconfig.admin.model;

/**
 * @author zhenyu.nie created on 2014 2014/10/15 14:17
 */
public enum ApplyResult {

    NEW, UPDATE;

    public static ApplyResult of(long editVersion) {
        return (editVersion == 0 ? NEW : UPDATE);
    }
}
