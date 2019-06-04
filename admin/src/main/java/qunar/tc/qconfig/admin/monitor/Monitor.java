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

    public static final QConfigCounter batchOperateConfigRejectedCounter = Metrics.counter("batch.operate.config.rejected.count");

    public static final QConfigTimer getAppInfoTimer = Metrics.timer("getAppInfo.request");

    public static final QConfigTimer applyTimer = Metrics.timer("apply.request");

    public static final QConfigTimer publishTimer = Metrics.timer("publish.request");

    public static final QConfigTimer normalOpTimer = Metrics.timer("normalOperate.request");

    public static final QConfigTimer clientUploadTimer = Metrics.timer("client.upload.request");

    public static final QConfigTimer batchSaveTimerWithoutPostEvent = Metrics.timer("batch.save.noevent.cost");

    public static final QConfigTimer batchSaveTimer = Metrics.timer("batch.save.cost");

    public static final QConfigTimer pluginCallTimer = Metrics.timer("client.set.public.request");

    public static final QConfigTimer freshQaTimer = Metrics.timer("fresh.qa");

    public static final String CLIENT_UPLOAD_FAIL = "client.upload.request.failOf.code";

    public static final QConfigTimer SAVE_ENTRIES_TIMER = Metrics.timer("saveEntries.timer");

    public static final QConfigTimer REMOVE_ENTRIES_TIMER = Metrics.timer("removeEntries.timer");

    public static final QConfigTimer SEARCH_ENTRIES_TIMER = Metrics.timer("searchEntries.timer");

    public static final QConfigTimer SEARCH_REF_ENTRIES_TIMER = Metrics.timer("searchRefEntries.timer");

    public static final QConfigTimer REBUILD_PROPERTIES_ENTRIES_TIMER = Metrics.timer("rebuildPropertiesEntries.timer");

    public static final QConfigTimer REST_CONFIGS_POST_TIMER = Metrics.timer("rest.configs.post.timer");

    public static final QConfigTimer MONITOR_GET_TIMER = Metrics.timer("monitor_data_get.timer");

    public static final QConfigTimer NEW_PORTAL_BATCH_ONEBUTTON_TIMER = Metrics.timer("new.portal.batch.onebutton.timer");

    public static final QConfigTimer OLD_PORTAL_ONEBUTTON_TIMER = Metrics.timer("old.portal.onebutton.timer");

    public static final QConfigTimer NEW_API_BATCH_ONEBUTTON_TIMER = Metrics.timer("new.api.batch.onebutton.timer");

    private static final String TYPE = "type";
    private static final String STATICS = "user.statistics";

    public static final QConfigCounter CLIENT_UPDATE_FILE_COUNT = Metrics.counter(CLIENT_UPLOAD_FAIL);

    public static final QConfigCounter APPLY_STATICS = Metrics.counter( "user.statistics.apply");

    public static final QConfigCounter FORCE_APPLY_STATICS = Metrics.counter( "user.statistics.force_apply");

    public static final QConfigCounter MULTI_APPLY_STATICS = Metrics.counter( "user.statistics.multi_apply");

    public static final QConfigCounter APPROVE_STATICS = Metrics.counter( "user.statistics.approve");

    public static final QConfigCounter BATCH_APPROVE_STATICS = Metrics.counter("user.statistics.batch_approve");

    public static final QConfigCounter REJECT_STATICS = Metrics.counter("user.statistics.reject");

    public static final QConfigCounter BATCH_REJECT_STATICS = Metrics.counter("user.statistics.batch_reject");

    public static final QConfigCounter RETURN_APPROVE_STATICS = Metrics.counter("user.statistics.return_approve");

    public static final QConfigCounter DELETE_STATICS = Metrics.counter("user.statistics.delete");

    public static final QConfigCounter PUBLISH_STATICS = Metrics.counter("user.statistics.publish");

    public static final QConfigCounter BATCH_PUBLISH_STATICS = Metrics.counter("user.statistics.batch_publish");

    public static final QConfigCounter PUBLIC_STATICS = Metrics.counter("user.statistics.public");

    public static final QConfigCounter ROLLBACK_STATICS = Metrics.counter("user.statistics.rollback");

    public static final QConfigCounter INHERIT_STATICS = Metrics.counter("user.statistics.inherit");

    public static final QConfigCounter REFERENCE_STATICS = Metrics.counter("user.statistics.reference");

    public static final QConfigCounter CANCEL_REFERENCE_STATICS = Metrics.counter("user.statistics.cancel_reference");

    public static final QConfigCounter ONE_BUTTON_PUBLISH_STATICS = Metrics.counter("user.statistics.one_button_publish");

    public static final QConfigCounter PUSH_STATICS = Metrics.counter("user.statistics.push");

    public static final QConfigCounter EDIT_PUSH_STATICS = Metrics.counter("user.statistics.edit_push");

    public static final QConfigCounter BATCH_PUSH_STATICS = Metrics.counter("user.statistics.batch_push");

    public static final QConfigCounter HISTORY_DIFF_STATICS = Metrics.counter("user.statistics.history_diff");

    public static final QConfigCounter ENCRYPT_KEY_STATICS = Metrics.counter("user.statistics.encrypt_key");

    public static final QConfigCounter SET_PERMISSION_STATICS = Metrics.counter("user.statistics.set_permission");

    public static final QConfigCounter SET_FILE_PERMISSION_STATICS = Metrics.counter("user.statistics.set_file_permission");

    public static final QConfigCounter UPLOAD_STATICS = Metrics.counter("user.statistics.upload");

    public static final QConfigCounter PROD_BETA_COMPARE_STATICS = Metrics.counter("user.statistics.prod_beta_compare");

    public static final QConfigCounter API_DIFF_PROFILE_STATICS = Metrics.counter("user.statistics.api_diff_profile");

    public static final QConfigCounter SET_FILE_VALIDATE_URL = Metrics.counter("user.statistics.set_file_validate_url");

    public static final QConfigCounter REMOVE_ENTRIES_FAILED_COUNT = Metrics.counter("removeEntries.failed.count");

    public static final QConfigCounter SAVE_ENTRIES_FAILED_COUNT = Metrics.counter("saveEntries.failed.count");

    public static final QConfigCounter notifyServerFailCounter = Metrics.counter("notifyServer.failOf");

    public static final QConfigCounter sendMailFailCounter = Metrics.counter("sendMail.failOf");

    public static final QConfigCounter getAppInfoFailCounter = Metrics.counter("getAppInfo.request.failOf");

    public static final QConfigCounter configOperateConflictCounter = Metrics.counter("configOperateConflict.count");

    public static final QConfigCounter REST_CONFIGS_POST_COUNT = Metrics.counter("user.statistics.configs.post.count");

    public static final QConfigCounter freshQaFailCounter = Metrics.counter("fresh.qa.failOf");

    public static final QConfigTimer referenceRelativeDoSearchTimer = Metrics.timer("reference.relative.do.search.timer");

    public static final QConfigCounter referenceRelativeDoSearchCounter = Metrics.counter("reference.relative.do.search.count");

    public static final QConfigCounter referenceRelativeDoSearchErrorCounter = Metrics.counter("reference.relative.do.search.error.count");

    public static final QConfigCounter referenceRelativeSearchErrorCounter = Metrics.counter("reference.relative.search.error.count");

    public static final QConfigCounter PROPERTIES_KEY_TOO_LONG_COUNTER = Metrics.counter("propertiesKeyTooLong.count");

}
