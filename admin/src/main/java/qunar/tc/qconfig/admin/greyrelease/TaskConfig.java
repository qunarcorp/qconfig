package qunar.tc.qconfig.admin.greyrelease;

import qunar.tc.qconfig.admin.cloud.vo.ConfigMetaVersion;
import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.util.List;
import java.util.Map;

public class TaskConfig {

    private List<ConfigMetaVersion> metaVersions;

    private int totalBatchNum;

    private boolean autoContinue;

    private boolean ignorePushFail;

    private boolean includeBastion;

    private int batchInterval;

    private Map<Integer, List<Host>> batches;

    public TaskConfig() {
    }

    public TaskConfig(int totalBatchNum, int batchInterval, boolean autoContinue, boolean ignorePushFail, boolean includeBastion, Map<Integer, List<Host>> batches) {
        this.totalBatchNum = totalBatchNum;
        this.autoContinue = autoContinue;
        this.ignorePushFail = ignorePushFail;
        this.includeBastion = includeBastion;
        this.batchInterval = batchInterval;
        this.batches = batches;
    }

    public TaskConfig(List<ConfigMetaVersion> metaVersions, int totalBatchNum, int batchInterval, boolean autoContinue, boolean ignorePushFail, boolean includeBastion, Map<Integer, List<Host>> batches) {
        this.metaVersions = metaVersions;
        this.totalBatchNum = totalBatchNum;
        this.autoContinue = autoContinue;
        this.ignorePushFail = ignorePushFail;
        this.includeBastion = includeBastion;
        this.batchInterval = batchInterval;
        this.batches = batches;
    }

    public List<ConfigMetaVersion> getMetaVersions() {
        return metaVersions;
    }

    public void setMetaVersions(List<ConfigMetaVersion> metaVersions) {
        this.metaVersions = metaVersions;
    }

    public int getTotalBatchNum() {
        return totalBatchNum;
    }

    public void setTotalBatchNum(int totalBatchNum) {
        this.totalBatchNum = totalBatchNum;
    }

    public boolean isAutoContinue() {
        return autoContinue;
    }

    public void setAutoContinue(boolean autoContinue) {
        this.autoContinue = autoContinue;
    }

    public boolean isIgnorePushFail() {
        return ignorePushFail;
    }

    public void setIgnorePushFail(boolean ignorePushFail) {
        this.ignorePushFail = ignorePushFail;
    }

    public boolean isIncludeBastion() {
        return includeBastion;
    }

    public void setIncludeBastion(boolean includeBastion) {
        this.includeBastion = includeBastion;
    }

    public int getBatchInterval() {
        return batchInterval;
    }

    public void setBatchInterval(int batchInterval) {
        this.batchInterval = batchInterval;
    }

    public Map<Integer, List<Host>> getBatches() {
        return batches;
    }

    public void setBatches(Map<Integer, List<Host>> batches) {
        this.batches = batches;
    }

    @Override
    public String toString() {
        return "TaskConfig{" +
                "totalBatchNum=" + totalBatchNum +
                ", autoContinue=" + autoContinue +
                ", ignorePushFail=" + ignorePushFail +
                ", includeBastion=" + includeBastion +
                ", batchInterval=" + batchInterval +
                ", batches=" + batches +
                '}';
    }
}
