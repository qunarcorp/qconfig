package qunar.tc.qconfig.admin.cloud.vo;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class FileMetaVo {

    private String profile;

    private Long version;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date updateTime;

    public FileMetaVo() {
    }

    public FileMetaVo(String profile, Long version, Date updateTime) {
        this.profile = profile;
        this.version = version;
        this.updateTime = updateTime;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "FileMetaVo{" +
                "profile='" + profile + '\'' +
                ", version=" + version +
                ", updateTime=" + updateTime +
                '}';
    }
}
