package qunar.tc.qconfig.admin.model.rest;

/**
 * Created by chenjk on 2018/1/18.
 */
public class RestApiRequest {

    private String requesttime;

    private String operator;

    public RestApiRequest() {

    }

    public RestApiRequest(String requesttime, String operator) {
        this.requesttime = requesttime;
        this.operator = operator;
    }

    public String getRequesttime() {
        return requesttime;
    }

    public void setRequesttime(String requesttime) {
        this.requesttime = requesttime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
