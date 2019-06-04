package qunar.tc.qconfig.admin.cloud.vo;


public class ClientFileVersionRequest extends FileMetaRequest {

    private String ip;

    public ClientFileVersionRequest() {
    }

    public ClientFileVersionRequest(String group, String profile, String dataId, Long version, String ip) {
        super(group, profile, dataId, version);
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "ClientFileVersionRequest{" +
                "ip='" + ip + '\'' +
                super.toString() +
                '}';
    }
}
