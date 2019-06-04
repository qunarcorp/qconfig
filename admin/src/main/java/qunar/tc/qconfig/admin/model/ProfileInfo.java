package qunar.tc.qconfig.admin.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author zhenyu.nie created on 2014 2014/5/28 11:53
 */
public class ProfileInfo {

    private String group;

    private String profile;

    private String environment;

    private String buildGroup;

    private boolean originEdit;

    private boolean edit;

    private boolean approve;

    private boolean publish;

    private boolean leader;

    private List<? extends ConfigInfo> referenceDatas;

    private List<? extends ConfigInfo> pendingDatas;

    private List<? extends ConfigInfo> passedDatas;

    private List<? extends ConfigInfo> publishedDatas;

    private List<? extends ConfigInfo> rejectedDatas;

    private List<? extends ConfigInfo> canceledDatas;

    private int countFile;



    public ProfileInfo(String group, String profile, String environment, String buildGroup, boolean originEdit, boolean edit,
                       boolean approve, boolean publish, boolean leader, List<? extends ConfigInfo> referenceDatas,
                       List<? extends ConfigInfo> pendingDatas, List<? extends ConfigInfo> passedDatas,
                       List<? extends ConfigInfo> publishedDatas, List<? extends ConfigInfo> rejectedDatas,
                       List<? extends ConfigInfo> canceledDatas) {
        this.group = group;
        this.profile = profile;
        this.environment = environment;
        this.buildGroup = buildGroup;
        this.originEdit = originEdit;
        this.edit = edit;
        this.approve = approve;
        this.publish = publish;
        this.leader = leader;
        this.referenceDatas = referenceDatas;
        this.pendingDatas = pendingDatas;
        this.passedDatas = passedDatas;
        this.publishedDatas = publishedDatas;
        this.rejectedDatas = rejectedDatas;
        this.canceledDatas = canceledDatas;
        sort();
    }

    public ProfileInfo(String group, String profile, String environment, String buildGroup, boolean originEdit, boolean edit,
                       boolean approve, boolean publish, boolean leader, List<? extends ConfigInfo> referenceDatas,
                       List<? extends ConfigInfo> pendingDatas, List<? extends ConfigInfo> passedDatas,
                       List<? extends ConfigInfo> publishedDatas, List<? extends ConfigInfo> rejectedDatas,
                       List<? extends ConfigInfo> canceledDatas,
                       int countFile) {
        this.group = group;
        this.profile = profile;
        this.environment = environment;
        this.buildGroup = buildGroup;
        this.originEdit = originEdit;
        this.edit = edit;
        this.approve = approve;
        this.publish = publish;
        this.leader = leader;
        this.referenceDatas = referenceDatas;
        this.pendingDatas = pendingDatas;
        this.passedDatas = passedDatas;
        this.publishedDatas = publishedDatas;
        this.rejectedDatas = rejectedDatas;
        this.canceledDatas = canceledDatas;
        this.countFile = countFile;
        sort();
    }

    private void sort() {
        List<List<? extends ConfigInfo>> lists = Arrays.asList(referenceDatas, pendingDatas, passedDatas,
                rejectedDatas, canceledDatas, publishedDatas);
        for (List<? extends ConfigInfo> datas : lists) {
            sortByTime(datas);
        }
    }

    private void sortByDataId(List<? extends ConfigInfo> datas) {
        Collections.sort(datas, new Comparator<ConfigInfo>() {
            @Override
            public int compare(ConfigInfo o1, ConfigInfo o2) {
                return o1.getConfigMeta().getDataId().compareTo(o2.getConfigMeta().getDataId());
            }
        });
    }

    private void sortByTime(List<? extends ConfigInfo> datas) {
        Collections.sort(datas, new Comparator<ConfigInfo>() {
            @Override
            public int compare(ConfigInfo o1, ConfigInfo o2) {
                return o2.getUpdateTime().compareTo(o1.getUpdateTime());
            }
        });
    }

    public String getGroup() {
        return group;
    }

    public String getProfile() {
        return profile;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getBuildGroup() {
        return buildGroup;
    }

    public boolean isLeader() {
        return leader;
    }

    public List<? extends ConfigInfo> getReferenceDatas() {
        return referenceDatas;
    }

    public List<? extends ConfigInfo> getPendingDatas() {
        return pendingDatas;
    }

    public List<? extends ConfigInfo> getPassedDatas() {
        return passedDatas;
    }

    public List<? extends ConfigInfo> getPublishedDatas() {
        return publishedDatas;
    }

    public List<? extends ConfigInfo> getRejectedDatas() {
        return rejectedDatas;
    }

    public List<? extends ConfigInfo> getCanceledDatas() {
        return canceledDatas;
    }

    public int getCountFile() {
        return countFile;
    }


    public boolean isOriginEdit() {
        return originEdit;
    }

    public boolean isEdit() {
        return edit;
    }

    public boolean isApprove() {
        return approve;
    }

    public boolean isPublish() {
        return publish;
    }

    @Override
    public String toString() {
        return "ProfileInfo{" +
                "group='" + group + '\'' +
                ", profile='" + profile + '\'' +
                ", environment='" + environment + '\'' +
                ", buildGroup='" + buildGroup + '\'' +
                ", originEdit=" + originEdit +
                ", edit=" + edit +
                ", approve=" + approve +
                ", publish=" + publish +
                ", leader=" + leader +
                ", referenceDatas=" + referenceDatas +
                ", pendingDatas=" + pendingDatas +
                ", passedDatas=" + passedDatas +
                ", publishedDatas=" + publishedDatas +
                ", rejectedDatas=" + rejectedDatas +
                ", canceledDatas=" + canceledDatas +
                '}';
    }
}
