package qunar.tc.qconfig.client.validate;

/**
 * @author zhenyu.nie created on 2017 2017/2/21 16:56
 */
public interface ValidateQColumn extends ErrorRecorder {

    String getColumnKey();

    int size();

    ValidateQCell get(String row);
}
