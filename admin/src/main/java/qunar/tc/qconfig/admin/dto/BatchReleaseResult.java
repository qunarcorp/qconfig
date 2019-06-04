package qunar.tc.qconfig.admin.dto;

import qunar.tc.qconfig.admin.greyrelease.ReleaseStatus;

import java.util.Map;

public class BatchReleaseResult {

    private ReleaseStatus releaseStatuses;

    private Map<String, String> failMap;

    public BatchReleaseResult(ReleaseStatus releaseStatuses, Map<String, String> failMap) {
        this.releaseStatuses = releaseStatuses;
        this.failMap = failMap;
    }

    public ReleaseStatus getReleaseStatuses() {
        return releaseStatuses;
    }

    public void setReleaseStatuses(ReleaseStatus releaseStatuses) {
        this.releaseStatuses = releaseStatuses;
    }

    public Map<String, String> getFailMap() {
        return failMap;
    }

    public void setFailMap(Map<String, String> failMap) {
        this.failMap = failMap;
    }
}