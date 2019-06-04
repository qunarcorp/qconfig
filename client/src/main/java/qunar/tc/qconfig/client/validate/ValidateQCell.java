package qunar.tc.qconfig.client.validate;

import java.util.Date;

/**
 * @author zhenyu.nie created on 2017 2017/2/21 16:56
 */
public interface ValidateQCell extends ErrorRecorder {

    String getRow();

    String getColumn();

    boolean isPresent();

    boolean absent();

    String value();

    String getString();

    boolean getBoolean();

    byte getByte();

    short getShort();

    int getInt();

    long getLong();

    float getFloat();

    double getDouble();

    Date getDate();
}
