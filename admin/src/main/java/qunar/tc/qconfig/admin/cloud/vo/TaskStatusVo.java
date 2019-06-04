package qunar.tc.qconfig.admin.cloud.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import qunar.tc.qconfig.admin.greyrelease.StatusInfo;
import qunar.tc.qconfig.admin.greyrelease.GreyReleaseState;
import qunar.tc.qconfig.admin.model.Host;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class TaskStatusVo {

    private String uuid;

    private List<ConfigMetaVersion> metaVersions;

    private String status;

    private String statusText;

    private int finishedBatchNum;

    private int totalBatchNum;

    private boolean autoContinue;

    private boolean ignoreErrorServer;

    private boolean includeBastion;

    private int batchInterval;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date updateTime;

    @JsonIgnore
    private Date lastPushTime;

    private int lockVersion;

    private String operator;

    private Map<Integer, Map<String, List<HostVo>>> batches;

    private Map<Integer, List<Host>> batchesAllocation;

    public TaskStatusVo() {
    }

    public TaskStatusVo(String uuid, GreyReleaseState status,
                        int finishedBatchNum, int totalBatchNum, boolean autoContinue, boolean ignoreErrorServer,
                        boolean includeBastion, int batchInterval, Date createTime, Date updateTime, Date lastPushTime,
                        int lockVersion, String operator, Map<Integer, Map<String, List<HostVo>>> batches,
                        List<ConfigMetaVersion> metaVersions, Map<Integer, List<Host>> batchesAllocation) {
        this.uuid = uuid;
        this.status = status.getText().toUpperCase();
        this.finishedBatchNum = finishedBatchNum;
        this.totalBatchNum = totalBatchNum;
        this.autoContinue = autoContinue;
        this.ignoreErrorServer = ignoreErrorServer;
        this.includeBastion = includeBastion;
        this.batchInterval = batchInterval;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.lastPushTime = lastPushTime;
        this.lockVersion = lockVersion;
        this.operator = operator;
        this.batches = batches;
        this.metaVersions = metaVersions;
        this.batchesAllocation = batchesAllocation;
    }

    public Map<Integer, List<Host>> getBatchesAllocation() {
        return batchesAllocation;
    }

    public List<ConfigMetaVersion> getMetaVersions() {
        return metaVersions;
    }

    public void setMetaVersions(List<ConfigMetaVersion> metaVersions) {
        this.metaVersions = metaVersions;
    }

    public String getUuid() {
        return uuid;
    }

    public GreyReleaseState getStatus() {
        return GreyReleaseState.textOf(status);
    }

    public String getStatusText() {
        return statusText;
    }

    public int getFinishedBatchNum() {
        return finishedBatchNum;
    }

    public int getTotalBatchNum() {
        return totalBatchNum;
    }

    public boolean isAutoContinue() {
        return autoContinue;
    }

    public boolean isIgnoreErrorServer() {
        return ignoreErrorServer;
    }

    public boolean isIncludeBastion() {
        return includeBastion;
    }

    public int getBatchInterval() {
        return batchInterval;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public Date getLastPushTime() {
        return lastPushTime;
    }

    public int getLockVersion() {
        return lockVersion;
    }

    public String getOperator() {
        return operator;
    }

    public Map<Integer, Map<String, List<HostVo>>> getBatches() {
        return batches;
    }

    public static class Builder {
        private String uuid;
        private GreyReleaseState status;
        private int finishedBatchNum;
        private int totalBatchNum;
        private boolean autoContinue;
        private boolean ignoreErrorServer;
        private boolean includeBastion;
        private int batchInterval;
        private Date createTime;
        private Date updateTime;
        private Date lastPushTime;
        private int lockVersion;
        private String operator;
        private Map<Integer, Map<String, List<HostVo>>> batches;
        Map<Integer, List<Host>> batchesAllocation;
        private List<ConfigMetaVersion> metaVersions;

        public Builder setInfo(StatusInfo info) {
            this.uuid = info.getUuid();
            this.metaVersions = info.getTaskConfig().getMetaVersions();
            this.status = info.getState();
            this.finishedBatchNum = info.getFinishedBatchNum();
            this.totalBatchNum = info.getTotalBatchNum();
            this.autoContinue = info.isAutoContinue();
            this.ignoreErrorServer = info.isIgnorePushFail();
            this.includeBastion = info.includeBastion();
            this.batchInterval = info.getAutoContinueMinutes();
            this.createTime = info.getCreateTime();
            this.updateTime = info.getUpdateTime();
            this.lastPushTime = info.getLastPushTime();
            this.lockVersion = info.getLockVersion();
            this.operator = info.getOperator();
            this.batchesAllocation = info.getBatches();
            return this;
        }

        public Builder setBatchStatus(Map<Integer, ? extends Map> batchStatus) {
            this.batches = (Map<Integer, Map<String, List<HostVo>>>) batchStatus;
            return this;
        }


        public Builder setBatchesAllocation(Map<Integer, List<Host>> batches) {
            Map<Integer, List<Host>> result = Maps.newHashMapWithExpectedSize(batches.size());
            for (Map.Entry<Integer, List<Host>> batch : batches.entrySet()) {
                result.put(batch.getKey(), Lists.transform(batch.getValue(), new Function<Host, Host>() {
                    @Override
                    public Host apply(Host host) {
                        return new Host(host.getIp());
                    }
                }));
            }

            this.batchesAllocation = result;
            return this;
        }

        public TaskStatusVo build() {
            return new TaskStatusVo(uuid, status, finishedBatchNum, totalBatchNum,
                    autoContinue, ignoreErrorServer, includeBastion, batchInterval, createTime, updateTime, lastPushTime,
                    lockVersion, operator, batches, metaVersions, batchesAllocation);
        }
    }

}
