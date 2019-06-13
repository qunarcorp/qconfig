package qunar.tc.qconfig.admin.monitor;

import qunar.tc.qconfig.common.metrics.Metrics;
import qunar.tc.qconfig.common.metrics.QConfigCounter;
import qunar.tc.qconfig.common.metrics.QConfigTimer;

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



    public static void clientUpdateFileCountInc(int code) {
        Metrics.counter(CLIENT_UPLOAD_FAIL, MonitorConstants.FAIL_CODE, new String[]{String.valueOf(code)});
    }

    public static final QConfigCounter APPLY_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"apply"});

    public static final QConfigCounter FORCE_APPLY_STATICS = Metrics.counter( MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"force_apply"});

    public static final QConfigCounter MULTI_APPLY_STATICS = Metrics.counter( MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"multi_apply"});

    public static final QConfigCounter APPROVE_STATICS = Metrics.counter( MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"approve"});

    public static final QConfigCounter BATCH_APPROVE_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"batch_approve"});

    public static final QConfigCounter REJECT_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"reject"});

    public static final QConfigCounter BATCH_REJECT_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"batch_reject"});

    public static final QConfigCounter RETURN_APPROVE_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"return_approve"});

    public static final QConfigCounter DELETE_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"delete"});

    public static final QConfigCounter PUBLISH_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"publish"});

    public static final QConfigCounter BATCH_PUBLISH_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"batch_publish"});

    public static final QConfigCounter PUBLIC_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"public"});

    public static final QConfigCounter ROLLBACK_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"rollback"});

    public static final QConfigCounter INHERIT_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"inherit"});

    public static final QConfigCounter REFERENCE_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"reference"});

    public static final QConfigCounter CANCEL_REFERENCE_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"cancel_reference"});

    public static final QConfigCounter ONE_BUTTON_PUBLISH_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"one_button_publish"});

    public static final QConfigCounter PUSH_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"push"});

    public static final QConfigCounter EDIT_PUSH_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"edit_push"});

    public static final QConfigCounter BATCH_PUSH_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"batch_push"});

    public static final QConfigCounter HISTORY_DIFF_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"history_diff"});

    public static final QConfigCounter ENCRYPT_KEY_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"encrypt_key"});

    public static final QConfigCounter SET_PERMISSION_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"set_permission"});

    public static final QConfigCounter SET_FILE_PERMISSION_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"set_file_permission"});

    public static final QConfigCounter UPLOAD_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"upload"});

    public static final QConfigCounter PROD_BETA_COMPARE_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"prod_beta_compare"});

    public static final QConfigCounter API_DIFF_PROFILE_STATICS = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"api_diff_profile"});

    public static final QConfigCounter SET_FILE_VALIDATE_URL = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"set_file_validate_url"});

    public static final QConfigCounter REMOVE_ENTRIES_FAILED_COUNT = Metrics.counter("removeEntries_failed_count");

    public static final QConfigCounter SAVE_ENTRIES_FAILED_COUNT = Metrics.counter("saveEntries_failed_count");

    public static final QConfigCounter notifyServerFailCounter = Metrics.counter("notifyServer_failOf");

    public static final QConfigCounter sendMailFailCounter = Metrics.counter("sendMail_failOf");

    public static final QConfigCounter getAppInfoFailCounter = Metrics.counter("getAppInfo_request_failOf");

    public static final QConfigCounter configOperateConflictCounter = Metrics.counter("configOperateConflict_count");

    public static final QConfigCounter REST_CONFIGS_POST_COUNT = Metrics.counter(MonitorConstants.STATICS, MonitorConstants.TYPE_TAG, new String[]{"configs_post_count"});

    public static final QConfigCounter freshQaFailCounter = Metrics.counter("fresh_qa_failOf");

    public static final QConfigTimer referenceRelativeDoSearchTimer = Metrics.timer("reference_relative_do_search_timer");

    public static final QConfigCounter referenceRelativeDoSearchCounter = Metrics.counter("reference_relative_do_search_count");

    public static final QConfigCounter referenceRelativeDoSearchErrorCounter = Metrics.counter("reference_relative_do_search_error_count");

    public static final QConfigCounter referenceRelativeSearchErrorCounter = Metrics.counter("reference_relative_search_error_count");

    public static final QConfigCounter PROPERTIES_KEY_TOO_LONG_COUNTER = Metrics.counter("propertiesKeyTooLong_count");

}
