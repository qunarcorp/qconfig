package qunar.tc.qconfig.admin.cloud.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.util.CollectionUtils;
import qunar.tc.qconfig.admin.model.Host;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GreyReleaseTaskRequest {

    private String group;

    private String profile;

    private String dataId;

    private Long version;

    private Integer totalBatchNum;

    private Integer batchInterval;

    private boolean autoContinue;

    private boolean ignorePushFail;

    private boolean includeBastion;

    private List<BatchInfo> batches;

    public GreyReleaseTaskRequest() {
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

    public List<BatchInfo> getBatches() {
        return batches;
    }

    public void setBatches(List<BatchInfo> batches) {
        this.batches = batches;
    }

    public Map<Integer, List<Host>> getBatchMap() {
        if (CollectionUtils.isEmpty(batches)) {
            return ImmutableMap.of();
        }
        Map<Integer, List<Host>> batchMap = Maps.newHashMapWithExpectedSize(batches.size());
        for (BatchInfo batchInfo : batches) {
            batchMap.put(batchInfo.batchNum, batchInfo.getServersHost());
        }
        return batchMap;
    }

    @Override
    public String toString() {
        return "GreyReleaseTaskRequest{" +
                "group='" + group + '\'' +
                ", profile='" + profile + '\'' +
                ", dataId='" + dataId + '\'' +
                ", version=" + version +
                ", totalBatchNum=" + totalBatchNum +
                ", batchInterval=" + batchInterval +
                ", autoContinue=" + autoContinue +
                ", ignorePushFail=" + ignorePushFail +
                ", includeBastion=" + includeBastion +
                ", batches=" + batches +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchInfo {
        private int batchNum;
        private List<HostVo> servers;

        public BatchInfo() {
        }

        public BatchInfo(int batchNum, List<HostVo> servers) {
            this.batchNum = batchNum;
            this.servers = servers;
        }

        public int getBatchNum() {
            return batchNum;
        }

        public void setBatchNum(int batchNum) {
            this.batchNum = batchNum;
        }

        public List<HostVo> getServers() {
            return servers;
        }

        public void setServers(List<HostVo> servers) {
            this.servers = servers;
        }

        public List<Host> getServersHost() {
            return Lists.transform(servers, new Function<HostVo, Host>() {
                @Override
                public Host apply(HostVo hostVo) {
                    return new Host(hostVo.getIp());
                }
            });
        }

        @Override
        public String toString() {
            return "BatchInfo{" +
                    "totalBatchNum=" + batchNum +
                    ", servers=" + servers +
                    '}';
        }
    }

}
