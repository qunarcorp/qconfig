package qunar.tc.qconfig.client.validate;

import java.io.Reader;

/**
 * @author zhenyu.nie created on 2017 2017/2/15 15:51
 */
public interface Validator {

    Object validate(Reader reader);
}
