package qunar.tc.qconfig.common.util;

import com.google.common.base.Splitter;
import qunar.tc.qconfig.common.executor.DirectExecutorService;

import java.nio.charset.Charset;
import java.util.concurrent.Executor;

/**
 * User: zhaohuiyu
 * Date: 5/9/14
 * Time: 12:02 PM
 */
public class Constants {

    /**
     * 文件不存在时候使用
     */
    public static final String NO_FILE_CHECKSUM = "7e67fcaf3f6b180bae35bc5ed2bd6a10";

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static final String LINE = System.getProperty("line.separator");

    public static final String PROFILE_NAME = "profile";

    public static final String ENV_NAME = "env";

    public static final String SUB_ENV = "subEnv";

    public static final String BUILD_GROUP = "buildGroup";

    public static final String TOKEN_NAME = "token";

    public static final String GROUP_NAME = "group";

    public static final String DATAID_NAME = "dataId";

    public static final String VERSION_NAME = "version";

    public static final String LOAD_PROFILE_NAME = "loadProfile";

    public static final String FILE_PROFILE = "fileProfile";

    public static final String CODE = "code";

    public static final String SUB_CODE = "sub-code";

    public static final String CONTENT = "content";

    public static final String CONFIG_LOG_TYPE_CODE = "configLogTypeCode";

    public static final String REMARKS_NAME = "remarks";

    public static final String CHECKSUM_NAME = "checksum";

    public static final String ROW = "row";

    public static final String ROWS = "rows";

    public static final String COLUMNS = "columns";

    public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=utf8";

    public static final String PUSH_URL = "qconfigpush";

    public static final String REF_GROUP_NAME = "refGroup";

    public static final String REF_DATAID_NAME = "refDataId";

    public static final String REF_PROFILE = "refProfile";

    public static final String REF_CHANGE_TYPE = "refChangeType";

    public static final String PORT = "port";

    public static final java.lang.String EMPTY = "";

    public static final String API_VERSION = "apiVersion";

    public static final Executor CURRENT_EXECUTOR = new DirectExecutorService();

    public static final String ROW_COLUMN_SEPARATOR = "/";

    public static final long PURGE_FILE_VERSION = -100;

    public static final long NO_FILE_VERSION = -1;

    public static final String NEED_PURGE = "needPurge";

    public static final String FORBIDDEN_FILE = "forbidden_file";

    public static final String UPDATE_TYPE = "updateType";

    public static final String PULL = "pull";

    public static final String UPDATE = "update";

    public static final String PROPERTIES_FILE_SUFFIX = ".properties";

    public static final String ISPUBLIC = "isPublic";

    public static final String OPERATOR = "operator";

    public static final String ISDIRECTPUBLISH = "isdirectpublish";

    public static final String EDIT_VERSION = "editversion";

    public static final String STATUS_CODE = "statuscode";

    public static final String DESCRIPTION = "description";

    public static final String CONFIG_DETAILS = "configDetails";

    public static final String CONTENT_ENCODING = "qconfig-content-encoding";

    public static final String ACCEPT_ENCODING = "qconfig-accept-encoding";

    public static final String CACHE_CONTROL = "qconfig-cache-control";

    public static final String CACHE_NO_STORE = "no-store";

    public static final int OK_STATUS = 200;

    public static final int ONE_ZERO_TWO_FOUR = 1024;

    public static final int FUTURE_DEFAULT_TIMEOUT_SECONDS = 5;

    public static final Splitter SPLIT_COMMA = Splitter.on(',').trimResults();
}
