package qunar.tc.qconfig.server.support.monitor;

import qunar.tc.qconfig.common.metrics.Metrics;
import qunar.tc.qconfig.common.metrics.QConfigCounter;
import qunar.tc.qconfig.common.metrics.QConfigTimer;

/**
 * User: zhaohuiyu
 * Date: 5/15/14
 * Time: 10:59 AM
 */
public class Monitor {

    public static final QConfigCounter notFoundConfigFileCounter = Metrics.counter("configFile_notFound");

    public static final QConfigCounter notFoundConfigFileFromDiskCounter = Metrics.counter("configFile_notFound_disk");

    public static final QConfigCounter notFoundConfigFileFromDbCounter = Metrics.counter("configFile_notFound_db");

    public static final QConfigCounter checkSumFailCounter = Metrics.counter("checkSum_failOf");

    public static final QConfigCounter forbidAccessCounter = Metrics.counter("access_forbid");

    public static final QConfigCounter forbidAppAccessCounter = Metrics.counter("access_app_forbid");

    public static final QConfigCounter forbidUnpulbicAccessCounter = Metrics.counter("unpublic_forbid");

    public static final QConfigCounter syncConfigFileFailCounter = Metrics.counter("syncConfigFile_failOf");

    public static final QConfigCounter notifyConfigFailCounter = Metrics.counter("config_notify_failOf");

    public static final QConfigCounter notifyConfigTrivialCounter = Metrics.counter("config_notify_trivial");

    public static final QConfigTimer notifyConfigTimer = Metrics.timer("config_notify");

    public static final QConfigCounter parseErrorMailRejectCounter = Metrics.counter("mail_parse_error_reject");

    public static final QConfigCounter executeParseErrorMailFailCounter = Metrics.counter("execute_parse_error_mail_failOf");

    public static final QConfigTimer parseErrorMailTimer = Metrics.timer("mail_parse_error_time");

    public static final QConfigCounter sendMailFailCounter = Metrics.counter("server_mail_failOf");

    public static final QConfigCounter sendQTalkFailCounter = Metrics.counter("server_qtalk_failOf");

    public static final QConfigCounter getAppInfoFailCounter = Metrics.counter("server_getAppInfo_failOf");

    public static final QConfigCounter returnChangeFailCounter = Metrics.counter("server_change_return_failOf");

    public static final QConfigTimer fileOnChangeTimer = Metrics.timer("server_file_onchange");

    public static final QConfigTimer filePushOnChangeTimer = Metrics.timer("server_file_push_onchange");

    public static final QConfigTimer returnChangeTimer = Metrics.timer("server_change_return");

    public static final QConfigTimer batchReturnChangeTimer = Metrics.timer("server_change_batch_return");

    public static final QConfigCounter batchReturnChangeFailCounter = Metrics.counter("server_change_batch_return_failOf");

    public static final QConfigCounter forceLoadFixedVersionCounter = Metrics.counter("server_force_load_fix_version_select");

    public static final QConfigCounter checkChangeFixedVersionCounter = Metrics.counter("server_check_change_fix_version_select");

    public static final QConfigCounter inheritFixedVersionCounter = Metrics.counter("server_check_change_fix_version_select");

    public static final QConfigCounter noServerInstanceCounter = Metrics.counter("server_instance_no");

    public static final QConfigCounter serverInfoFreshError = Metrics.counter("server_info_fresh_error");

    public static final QConfigCounter serverIpRoomGetError = Metrics.counter("server_ip_room_get_error");

    public static final QConfigCounter serverLocalIpError = Metrics.counter("server_ip_local_error");

    public static final QConfigCounter updateRelationError = Metrics.counter("server_relation_update_error");

    public static final QConfigTimer consumerFixedVersionSelectTimer = Metrics.timer("server_consumer_fixed_version_select_time");

    public static final QConfigTimer consumerFixedIpAndVersionSelectTimer = Metrics.timer("server_consumer_fixed_ip_version_select_time");

    public static final QConfigCounter clientCheckRateUpToLimitCounter = Metrics.counter("client_check_rate_up_to_limit");

    public static final QConfigCounter entryPointMissCounter = Metrics.counter("server_entrypoint_argument_miss");

    public static final QConfigCounter accessFileMissCounter = Metrics.counter("server_access_argument_miss");

    public static final QConfigCounter refreshOtherClusterError = Metrics.counter("server_refresh_entrypoint_error");

    public static final QConfigCounter accessWithType = Metrics.counter("server_access_withtype");

    public static final QConfigTimer updateReferenceCache = Metrics.timer("qconfig_updateReferenceCache");

    public static final QConfigTimer updateConfigTypeCache = Metrics.timer("qconfig_freshConfigTypeCache");

    public static final QConfigTimer freshConfigVersionCacheTimer = Metrics.timer("qconfig_freshConfigVersionCache");

    public final static QConfigTimer freshFixedVersionConsumerCache = Metrics.timer("qconfig_freshFixedVersionConsumerCache");

    public static final QConfigTimer freshPushVersionCache = Metrics.timer("qconfig_freshPushVersionCache");

}
