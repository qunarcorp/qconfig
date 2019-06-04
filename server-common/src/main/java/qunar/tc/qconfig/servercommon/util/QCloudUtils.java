package qunar.tc.qconfig.servercommon.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author zhenyu.nie created on 2017 2017/10/31 17:26
 */
public class QCloudUtils {

    private static final Logger logger = LoggerFactory.getLogger(QCloudUtils.class);

    private static final char SEPARATOR = ':';

    public static String getAppFromGroup(String group) {
        int index = group.indexOf(SEPARATOR);
        if (index < 0) {
            return group;
        } else {
            return group.substring(index + 1);
        }
    }

}
