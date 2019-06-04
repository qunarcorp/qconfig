package qunar.tc.qconfig.admin.cloud.vo;

import qunar.tc.qconfig.admin.model.DiffResult;

import java.util.List;

public class FileDiffVo {

    private String dataId;

    private int exist;

    private String error;

    private FileMetaVo metaFrom;

    private FileMetaVo metaTo;

    private DiffResult<List<PropertyDiffVo>> keyDiffResult;

    public FileDiffVo() {
    }

    public FileDiffVo(String dataId, int exist, String error, FileMetaVo metaFrom, FileMetaVo metaTo,
                      DiffResult<List<PropertyDiffVo>> keyDiffResult) {
        this.dataId = dataId;
        this.exist = exist;
        this.error = error;
        this.metaFrom = metaFrom;
        this.metaTo = metaTo;
        this.keyDiffResult = keyDiffResult;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public int getExist() {
        return exist;
    }

    public void setExist(int exist) {
        this.exist = exist;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public FileMetaVo getMetaFrom() {
        return metaFrom;
    }

    public void setMetaFrom(FileMetaVo metaFrom) {
        this.metaFrom = metaFrom;
    }

    public FileMetaVo getMetaTo() {
        return metaTo;
    }

    public void setMetaTo(FileMetaVo metaTo) {
        this.metaTo = metaTo;
    }

    public DiffResult<List<PropertyDiffVo>> getKeyDiffResult() {
        return keyDiffResult;
    }

    public void setKeyDiffResult(DiffResult<List<PropertyDiffVo>> keyDiffResult) {
        this.keyDiffResult = keyDiffResult;
    }

    @Override
    public String toString() {
        return "FileDiffVo{" +
                "dataId='" + dataId + '\'' +
                ", exist=" + exist +
                ", error='" + error + '\'' +
                ", metaFrom=" + metaFrom +
                ", metaTo=" + metaTo +
                ", keyDiffResult=" + keyDiffResult +
                '}';
    }
}
