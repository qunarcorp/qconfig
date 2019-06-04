package qunar.tc.qconfig.admin.cloud.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * Created by pingyang.yang on 2018/12/12
 */
public class CommentVo {

    @JsonFormat(pattern = "yyyy-MM-dd HH.mm.ss", locale = "zh", timezone = "GMT+8")
    private Date time;

    private String comment;

    private long version;

    private String operator;

    private String type;

    public CommentVo() {
    }

    public CommentVo(Date time, String comment, long version, String operator, String type) {
        this.time = time;
        this.comment = comment;
        this.version = version;
        this.operator = operator;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
