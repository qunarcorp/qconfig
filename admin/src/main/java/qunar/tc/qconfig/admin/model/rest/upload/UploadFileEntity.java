package qunar.tc.qconfig.admin.model.rest.upload;

/**
 * Created by chenjk on 2018/1/18.
 */
public class UploadFileEntity {

    private String groupid;

    private String targetgroupid;

    private String env;//配置文件的环境

    private String subenv;

    private String serverenv;//服务器上server.properties配置的环境

    private String serversubenv;

    private String dataid;

    private Long version;

    private String content;

    private boolean isPublic;

    private String description;

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getTargetgroupid() {
        return targetgroupid;
    }

    public void setTargetgroupid(String targetgroupid) {
        this.targetgroupid = targetgroupid;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getSubenv() {
        return subenv;
    }

    public void setSubenv(String subenv) {
        this.subenv = subenv;
    }

    public String getServerenv() {
        return serverenv;
    }

    public void setServerenv(String serverenv) {
        this.serverenv = serverenv;
    }

    public String getServersubenv() {
        return serversubenv;
    }

    public void setServersubenv(String serversubenv) {
        this.serversubenv = serversubenv;
    }

    public String getDataid() {
        return dataid;
    }

    public void setDataid(String dataid) {
        this.dataid = dataid;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
