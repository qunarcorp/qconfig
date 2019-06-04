package qunar.tc.qconfig.client.validate;

/**
 * @author zhenyu.nie created on 2017 2017/2/21 16:55
 */
public interface ValidateQRow extends ErrorRecorder {

    String getRowKey();

    int size();

    ValidateQCell get(String column);
}
