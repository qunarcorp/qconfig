package qunar.tc.qconfig.admin.monitor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import qunar.tc.qconfig.client.spring.QConfig;
import qunar.tc.qconfig.common.metrics.Metrics;
import qunar.tc.qconfig.common.metrics.QConfigCounter;
import qunar.tc.qconfig.common.metrics.QConfigTimer;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * User: zhaohuiyu
 * Date: 5/27/14
 * Time: 3:28 PM
 */
public class Monitor {

    public static final QConfigCounter batchOperateConfigRejectedCounter = Metrics.counter("batch_operate_config_rejected_count");

    public static final QConfigTimer getAppInfoTimer = Metrics.timer("getAppInfo_request");

    public static final QConfigTimer applyTimer = Metrics.timer("apply_request");

    public static final QConfigTimer publishTimer = Metrics.timer("publish_request");

    public static final QConfigTimer normalOpTimer = Metrics.timer("normalOperate_request");

    public static final QConfigTimer clientUploadTimer = Metrics.timer("client_upload_request");

    public static final QConfigTimer batchSaveTimerWithoutPostEvent = Metrics.timer("batch_save_noevent_cost");

    public static final QConfigTimer batchSaveTimer = Metrics.timer("batch_save_cost");

    public static final QConfigTimer pluginCallTimer = Metrics.timer("client_set_public_request");

    public static final QConfigTimer freshQaTimer = Metrics.timer("fresh_qa");

    public static final String CLIENT_UPLOAD_FAIL = "client_upload_request_failOf_code";

    public static final QConfigTimer SAVE_ENTRIES_TIMER = Metrics.timer("saveEntries_timer");

    public static final QConfigTimer REMOVE_ENTRIES_TIMER = Metrics.timer("removeEntries_timer");

    public static final QConfigTimer SEARCH_ENTRIES_TIMER = Metrics.timer("searchEntries_timer");

    public static final QConfigTimer SEARCH_REF_ENTRIES_TIMER = Metrics.timer("searchRefEntries_timer");

    public static final QConfigTimer REBUILD_PROPERTIES_ENTRIES_TIMER = Metrics.timer("rebuildPropertiesEntries_timer");

    public static final QConfigTimer REST_CONFIGS_POST_TIMER = Metrics.timer("rest_configs_post_timer");

    public static final QConfigTimer MONITOR_GET_TIMER = Metrics.timer("monitor_data_get_timer");

    public static final QConfigTimer NEW_PORTAL_BATCH_ONEBUTTON_TIMER = Metrics.timer("new_portal_batch_onebutton_timer");

    public static final QConfigTimer OLD_PORTAL_ONEBUTTON_TIMER = Metrics.timer("old_portal_onebutton_timer");

    public static final QConfigTimer NEW_API_BATCH_ONEBUTTON_TIMER = Metrics.timer("new_api_batch_onebutton_timer");

    private static final String TYPE = "type";
    private static final String STATICS = "user_statistics";

    public static final QConfigCounter CLIENT_UPDATE_FILE_COUNT = Metrics.counter(CLIENT_UPLOAD_FAIL);

    public static final QConfigCounter APPLY_STATICS = Metrics.counter( "user_statistics_apply");

    public static final QConfigCounter FORCE_APPLY_STATICS = Metrics.counter( "user_statistics_force_apply");

    public static final QConfigCounter MULTI_APPLY_STATICS = Metrics.counter( "user_statistics_multi_apply");

    public static final QConfigCounter APPROVE_STATICS = Metrics.counter( "user_statistics_approve");

    public static final QConfigCounter BATCH_APPROVE_STATICS = Metrics.counter("user_statistics_batch_approve");

    public static final QConfigCounter REJECT_STATICS = Metrics.counter("user_statistics_reject");

    public static final QConfigCounter BATCH_REJECT_STATICS = Metrics.counter("user_statistics_batch_reject");

    public static final QConfigCounter RETURN_APPROVE_STATICS = Metrics.counter("user_statistics_return_approve");

    public static final QConfigCounter DELETE_STATICS = Metrics.counter("user_statistics_delete");

    public static final QConfigCounter PUBLISH_STATICS = Metrics.counter("user_statistics_publish");

    public static final QConfigCounter BATCH_PUBLISH_STATICS = Metrics.counter("user_statistics_batch_publish");

    public static final QConfigCounter PUBLIC_STATICS = Metrics.counter("user_statistics_public");

    public static final QConfigCounter ROLLBACK_STATICS = Metrics.counter("user_statistics_rollback");

    public static final QConfigCounter INHERIT_STATICS = Metrics.counter("user_statistics_inherit");

    public static final QConfigCounter REFERENCE_STATICS = Metrics.counter("user_statistics_reference");

    public static final QConfigCounter CANCEL_REFERENCE_STATICS = Metrics.counter("user_statistics_cancel_reference");

    public static final QConfigCounter ONE_BUTTON_PUBLISH_STATICS = Metrics.counter("user_statistics_one_button_publish");

    public static final QConfigCounter PUSH_STATICS = Metrics.counter("user_statistics_push");

    public static final QConfigCounter EDIT_PUSH_STATICS = Metrics.counter("user_statistics_edit_push");

    public static final QConfigCounter BATCH_PUSH_STATICS = Metrics.counter("user_statistics_batch_push");

    public static final QConfigCounter HISTORY_DIFF_STATICS = Metrics.counter("user_statistics_history_diff");

    public static final QConfigCounter ENCRYPT_KEY_STATICS = Metrics.counter("user_statistics_encrypt_key");

    public static final QConfigCounter SET_PERMISSION_STATICS = Metrics.counter("user_statistics_set_permission");

    public static final QConfigCounter SET_FILE_PERMISSION_STATICS = Metrics.counter("user_statistics_set_file_permission");

    public static final QConfigCounter UPLOAD_STATICS = Metrics.counter("user_statistics_upload");

    public static final QConfigCounter PROD_BETA_COMPARE_STATICS = Metrics.counter("user_statistics_prod_beta_compare");

    public static final QConfigCounter API_DIFF_PROFILE_STATICS = Metrics.counter("user_statistics_api_diff_profile");

    public static final QConfigCounter SET_FILE_VALIDATE_URL = Metrics.counter("user_statistics_set_file_validate_url");

    public static final QConfigCounter REMOVE_ENTRIES_FAILED_COUNT = Metrics.counter("removeEntries_failed_count");

    public static final QConfigCounter SAVE_ENTRIES_FAILED_COUNT = Metrics.counter("saveEntries_failed_count");

    public static final QConfigCounter notifyServerFailCounter = Metrics.counter("notifyServer_failOf");

    public static final QConfigCounter sendMailFailCounter = Metrics.counter("sendMail_failOf");

    public static final QConfigCounter getAppInfoFailCounter = Metrics.counter("getAppInfo_request_failOf");

    public static final QConfigCounter configOperateConflictCounter = Metrics.counter("configOperateConflict_count");

    public static final QConfigCounter REST_CONFIGS_POST_COUNT = Metrics.counter("user_statistics_configs_post_count");

    public static final QConfigCounter freshQaFailCounter = Metrics.counter("fresh_qa_failOf");

    public static final QConfigTimer referenceRelativeDoSearchTimer = Metrics.timer("reference_relative_do_search_timer");

    public static final QConfigCounter referenceRelativeDoSearchCounter = Metrics.counter("reference_relative_do_search_count");

    public static final QConfigCounter referenceRelativeDoSearchErrorCounter = Metrics.counter("reference_relative_do_search_error_count");

    public static final QConfigCounter referenceRelativeSearchErrorCounter = Metrics.counter("reference_relative_search_error_count");

    public static final QConfigCounter PROPERTIES_KEY_TOO_LONG_COUNTER = Metrics.counter("propertiesKeyTooLong_count");

}
