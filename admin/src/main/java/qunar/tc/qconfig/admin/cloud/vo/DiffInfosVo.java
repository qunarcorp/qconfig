package qunar.tc.qconfig.admin.cloud.vo;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import qunar.tc.qconfig.admin.model.ConfigOpLog;
import qunar.tc.qconfig.admin.model.KeyValuePair;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.util.List;
import java.util.Map;

public class DiffInfosVo {

    private String group;

    private String dataId;

    private String profile;

    private String data;

    private JsonNode dataObject;

    private String templateDetail;

    private List<KeyValuePair<ConfigMetaVersion, Object>> diffs;

    private List<ConfigOpLog> oplogs;

    private boolean intercept = false;

    private boolean showDiffAlert = false;

    private String diffAlertText = "";

    public <T> DiffInfosVo(ConfigMeta meta, String data, JsonNode dataObject, String templateDetail, List<Map.Entry<VersionData<ConfigMeta>, T>> diffs, List<ConfigOpLog> oplogs) {
        this.group = meta.getGroup();
        this.dataId = meta.getDataId();
        this.profile = meta.getProfile();
        this.data = data;
        this.dataObject = dataObject;
        this.templateDetail = templateDetail;
        this.diffs = Lists.newArrayListWithCapacity(diffs.size());
        for (Map.Entry<VersionData<ConfigMeta>, T> diff : diffs) {
            this.diffs.add(new KeyValuePair<>(new ConfigMetaVersion(diff.getKey()), (Object)diff.getValue()));
        }
        this.oplogs = oplogs;
    }

    public String getGroup() {
        return group;
    }

    public String getDataId() {
        return dataId;
    }

    public String getProfile() {
        return profile;
    }

    public String getData() {
        return data;
    }

    public JsonNode getDataObject() {
        return dataObject;
    }

    public String getTemplateDetail() {
        return templateDetail;
    }

    public List<KeyValuePair<ConfigMetaVersion, Object>> getDiffs() {
        return diffs;
    }

    public List<ConfigOpLog> getOplogs() {
        return oplogs;
    }

    public boolean isIntercept() {
        return intercept;
    }

    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }

    public boolean isShowDiffAlert() {
        return showDiffAlert;
    }

    public void setShowDiffAlert(boolean showDiffAlert) {
        this.showDiffAlert = showDiffAlert;
    }

    public String getDiffAlertText() {
        return diffAlertText;
    }

    public void setDiffAlertText(String diffAlertText) {
        this.diffAlertText = diffAlertText;
    }

    @Override
    public String toString() {
        return "DiffInfosVo{" +
                "group='" + group + '\'' +
                ", dataId='" + dataId + '\'' +
                ", profile='" + profile + '\'' +
                ", intercept=" + intercept +
                '}';
    }
}