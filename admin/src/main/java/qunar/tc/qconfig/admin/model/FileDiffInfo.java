package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.admin.cloud.vo.FileMetaVo;

/**
 * @author zhenyu.nie created on 2016 2016/9/1 15:40
 */
public class FileDiffInfo {

    private String name;

    private boolean lExist;

    private boolean rExist;

    private String error;

    private Object diff;

    private FileMetaVo metaFrom;

    private FileMetaVo metaTo;

    public FileDiffInfo(String name, boolean lExist, boolean rExist, String error, Object diff, FileMetaVo metaFrom, FileMetaVo metaTo) {
        this.name = name;
        this.lExist = lExist;
        this.rExist = rExist;
        this.error = error;
        this.diff = diff;
        this.metaFrom = metaFrom;
        this.metaTo = metaTo;
    }

    public String getName() {
        return name;
    }

    public boolean isLExist() {
        return lExist;
    }

    public boolean isRExist() {
        return rExist;
    }

    public String getError() {
        return error;
    }

    public Object getDiff() {
        return diff;
    }

    public FileMetaVo getMetaFrom() {
        return metaFrom;
    }

    public FileMetaVo getMetaTo() {
        return metaTo;
    }

    @Override
    public String toString() {
        return "FileDiffInfo{" +
                "name='" + name + '\'' +
                ", lExist=" + lExist +
                ", rExist=" + rExist +
                ", error='" + error + '\'' +
                ", diff=" + diff +
                ", metaFrom=" + metaFrom +
                ", metaTo=" + metaTo +
                '}';
    }
}
