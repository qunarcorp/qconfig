package qunar.tc.qconfig.admin.cloud.vo;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;
import qunar.tc.qconfig.admin.model.Host;

import java.util.List;
import java.util.Map;

public class BatchGreyReleaseTaskVo {

    private List<ConfigMetaVersion> releaseList;

    private Integer totalBatchNum;

    private Integer batchInterval;

    private boolean autoContinue;

    private boolean ignorePushFail;

    private boolean includeBastion;

    private List<GreyReleaseTaskRequest.BatchInfo> batches;

    public List<ConfigMetaVersion> getReleaseList() {
        return releaseList;
    }

    public void setReleaseList(List<ConfigMetaVersion> releaseList) {
        this.releaseList = releaseList;
    }

    public Integer getTotalBatchNum() {
        return totalBatchNum;
    }

    public void setTotalBatchNum(Integer totalBatchNum) {
        this.totalBatchNum = totalBatchNum;
    }

    public Integer getBatchInterval() {
        return batchInterval;
    }

    public void setBatchInterval(Integer batchInterval) {
        this.batchInterval = batchInterval;
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

    public List<GreyReleaseTaskRequest.BatchInfo> getBatches() {
        return batches;
    }

    public void setBatches(List<GreyReleaseTaskRequest.BatchInfo> batches) {
        this.batches = batches;
    }

    public Map<Integer, List<Host>> getBatchMap() {
        if (CollectionUtils.isEmpty(batches)) {
            return ImmutableMap.of();
        }
        Map<Integer, List<Host>> batchMap = Maps.newHashMapWithExpectedSize(batches.size());
        for (GreyReleaseTaskRequest.BatchInfo batchInfo : batches) {
            batchMap.put(batchInfo.getBatchNum(), batchInfo.getServersHost());
        }
        return batchMap;
    }
}
