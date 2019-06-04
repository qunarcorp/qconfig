package qunar.tc.qconfig.admin.model;

import java.sql.Timestamp;

/**
 * Created by chenjk on 2018/1/12.
 */
public class ApiPermission {
    private Long id;
    private String url;
    private Long parentid;
    private String method;
    private int type;
    private String description;
    private Timestamp dataChangeLasttime;

    public ApiPermission() {

    }

    public ApiPermission(String url, Long parentid, String method, int type, String description) {
        this.url = url;
        this.parentid = parentid;
        this.method = method;
        this.type = type;
        this.description = description;
    }

    public ApiPermission(Long id, String url, Long parentid, String method, int type, String description, Timestamp dataChangeLasttime) {
        this.id = id;
        this.url = url;
        this.parentid = parentid;
        this.method = method;
        this.type = type;
        this.description = description;
        this.dataChangeLasttime = dataChangeLasttime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getParentid() {
        return parentid;
    }

    public void setParentid(Long parentid) {
        this.parentid = parentid;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getDataChangeLasttime() {
        return dataChangeLasttime;
    }

    public void setDataChangeLasttime(Timestamp dataChangeLasttime) {
        this.dataChangeLasttime = dataChangeLasttime;
    }
}
