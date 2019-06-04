package qunar.tc.qconfig.admin.cloud.vo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileMetaRequest extends ProfileRequest {

    private String dataId;

    private Long version;

    public FileMetaRequest() {
    }

    public FileMetaRequest(String group, String profile, String dataId, Long version) {
        super(group, profile);
        this.dataId = dataId;
        this.version = version;
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

    @Override
    public String toString() {
        return "FileMetaRequest{" +
                "dataId='" + dataId + '\'' +
                ", version=" + version +
                super.toString() +
                '}';
    }
}
