package qunar.tc.qconfig.admin.greyrelease;

import qunar.tc.qconfig.admin.model.Host;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2018 2018/5/21 14:26
 */
public class StatusInfo {

    private String uuid;

    private GreyReleaseState state;

    private int finishedBatchNum;

    // taskConfig一开始定好之后就不变。
    private TaskConfig taskConfig;

    private Date createTime;

    private Date updateTime;

    private Date lastPushTime;

    private int lockVersion;

    private String operator;

    public StatusInfo() {
    }

    public StatusInfo(String uuid,  TaskConfig taskConfig, GreyReleaseState state,
                      int finishedBatchNum, Date createTime, Date updateTime, Date lastPushTime, int lockVersion, String operator) {
        this.uuid = uuid;
        this.taskConfig = taskConfig;
        this.state = state;
        this.finishedBatchNum = finishedBatchNum;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.lastPushTime = lastPushTime;
        this.lockVersion = lockVersion;
        this.operator = operator;
    }

    public StatusInfo copy() {
        return new StatusInfo(this.uuid, this.taskConfig, this.state, this.finishedBatchNum,
                this.createTime, this.updateTime, this.lastPushTime, this.lockVersion, this.operator);
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public StatusInfo setState(GreyReleaseState state) {
        this.state = state;
        return this;
    }

    public TaskConfig getTaskConfig() {
        return taskConfig;
    }

    public void setTaskConfig(TaskConfig taskConfig) {
        this.taskConfig = taskConfig;
    }

    public void setFinishedBatchNum(int finishedBatchNum) {
        this.finishedBatchNum = finishedBatchNum;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setLastPushTime(Date lastPushTime) {
        this.lastPushTime = lastPushTime;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public int getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(int lockVersion) {
        this.lockVersion = lockVersion;
    }

    public String getUuid() {
        return uuid;
    }

    public GreyReleaseState getState() {
        return state;
    }

    public Map<Integer, List<Host>> getBatches() {
        return taskConfig.getBatches();
    }

    public boolean includeBastion() {
        return taskConfig.isIncludeBastion();
    }

    public StatusInfo copyAndIncLockVersion() {
        StatusInfo newStatusInfo = copy();
        newStatusInfo.setLockVersion(getLockVersion() + 1);
        return newStatusInfo;
    }

    public StatusInfo incrementFinishedBatchNum() {
        this.finishedBatchNum++;
        return this;
    }

    public int getTotalBatchNum() {
        return this.taskConfig.getTotalBatchNum();
    }

    public int getFinishedBatchNum() {
        return finishedBatchNum;
    }

    public boolean isAutoContinue() {
        return taskConfig.isAutoContinue();
    }

    public int getAutoContinueMinutes() {
        return taskConfig.getBatchInterval();
    }

    public boolean isPublishing() {
        return state == GreyReleaseState.PUBLISHING;
    }

    public boolean isPause() {
        return state == GreyReleaseState.WAIT_PUBLISH;
    }

    public boolean isCancel() {
        return state == GreyReleaseState.CANCEL;
    }

    public String getOperator() {
        return operator;
    }

    public Date getLastPushTime() {
        return lastPushTime;
    }

    public StatusInfo setPublishTime(Date time) {
        lastPushTime = time;
        return this;
    }

    public boolean isIgnorePushFail() {
        return taskConfig.isIgnorePushFail();
    }
}
