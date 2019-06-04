package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.common.bean.Candidate;

/**
 * Date: 14-10-21 Time: 下午3:18
 * 
 * @author: xiao.liang
 * @description:
 */
public class Conflict {

    public enum Type { REF, EXIST, INHERIT}

    private Candidate candidate;

    private Type type;

    public Conflict(Candidate candidate, Type type) {
        this.candidate = candidate;
        this.type = type;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public Type getType() {
        return type;
    }
}
