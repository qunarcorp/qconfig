package qunar.tc.qconfig.admin.exception;

import qunar.tc.qconfig.admin.model.Conflict;

/**
 * Date: 14-6-30
 * Time: 下午3:45
 *
 * @author: xiao.liang
 * @description:
 */
public class ConfigExistException extends RuntimeException {

    private static final long serialVersionUID = -3650737665750308373L;

    private Conflict conflict;

    public ConfigExistException(Conflict conflict) {
        this.conflict = conflict;
    }

    public Conflict getConflict() {
        return conflict;
    }
}
