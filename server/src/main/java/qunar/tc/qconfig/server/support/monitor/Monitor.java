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

    public static final QConfigCounter notFoundConfigFileCounter = Metrics.counter("configFile.notFound");

    public static final QConfigCounter notFoundConfigFileFromDiskCounter = Metrics.counter("configFile.notFound.disk");

    public static final QConfigCounter notFoundConfigFileFromDbCounter = Metrics.counter("configFile.notFound.db");

    public static final QConfigCounter checkSumFailCounter = Metrics.counter("checkSum.failOf");

    public static final QConfigCounter forbidAccessCounter = Metrics.counter("access.forbid");

    public static final QConfigCounter forbidAppAccessCounter = Metrics.counter("access.app.forbid");

    public static final QConfigCounter forbidUnpulbicAccessCounter = Metrics.counter("unpublic.forbid");

    public static final QConfigCounter syncConfigFileFailCounter = Metrics.counter("syncConfigFile.failOf");

    public static final QConfigCounter notifyConfigFailCounter = Metrics.counter("config.notify.failOf");

    public static final QConfigCounter notifyConfigTrivialCounter = Metrics.counter("config.notify.trivial");

    public static final QConfigTimer notifyConfigTimer = Metrics.timer("config.notify");

    public static final QConfigCounter parseErrorMailRejectCounter = Metrics.counter("mail.parse.error.reject");

    public static final QConfigCounter executeParseErrorMailFailCounter = Metrics.counter("execute.parse.error.mail.failOf");

    public static final QConfigTimer parseErrorMailTimer = Metrics.timer("mail.parse.error.time");

    public static final QConfigCounter sendMailFailCounter = Metrics.counter("server.mail.failOf");

    public static final QConfigCounter sendQTalkFailCounter = Metrics.counter("server.qtalk.failOf");

    public static final QConfigCounter getAppInfoFailCounter = Metrics.counter("server.getAppInfo.failOf");

    public static final QConfigCounter returnChangeFailCounter = Metrics.counter("server.change.return.failOf");

    public static final QConfigTimer fileOnChangeTimer = Metrics.timer("server.file.onchange");

    public static final QConfigTimer filePushOnChangeTimer = Metrics.timer("server.file.push.onchange");

    public static final QConfigTimer returnChangeTimer = Metrics.timer("server.change.return");

    public static final QConfigTimer batchReturnChangeTimer = Metrics.timer("server.change.batch.return");

    public static final QConfigCounter batchReturnChangeFailCounter = Metrics.counter("server.change.batch.return.failOf");

    public static final QConfigCounter forceLoadFixedVersionCounter = Metrics.counter("server.force.load.fix.version.select");

    public static final QConfigCounter checkChangeFixedVersionCounter = Metrics.counter("server.check.change.fix.version.select");

    public static final QConfigCounter inheritFixedVersionCounter = Metrics.counter("server.check.change.fix.version.select");

    public static final QConfigCounter noServerInstanceCounter = Metrics.counter("server.instance.no");

    public static final QConfigCounter serverInfoFreshError = Metrics.counter("server.info.fresh.error");

    public static final QConfigCounter serverIpRoomGetError = Metrics.counter("server.ip.room.get.error");

    public static final QConfigCounter serverLocalIpError = Metrics.counter("server.ip.local.error");

    public static final QConfigCounter updateRelationError = Metrics.counter("server.relation.update.error");

    public static final QConfigTimer consumerFixedVersionSelectTimer = Metrics.timer("server.consumer.fixed.version.select.time");

    public static final QConfigTimer consumerFixedIpAndVersionSelectTimer = Metrics.timer("server.consumer.fixed.ip.version.select.time");

    public static final QConfigCounter clientCheckRateUpToLimitCounter = Metrics.counter("client.check.rate.up.to.limit");

    public static final QConfigCounter entryPointMissCounter = Metrics.counter("server.entrypoint.argument.miss");

    public static final QConfigCounter accessFileMissCounter = Metrics.counter("server.access.argument.miss");

    public static final QConfigCounter refreshOtherClusterError = Metrics.counter("server.refresh.entrypoint.error");

    public static final QConfigCounter accessWithType = Metrics.counter("server.access.withtype");

    public static final QConfigTimer updateReferenceCache = Metrics.timer("qconfig.updateReferenceCache");

    public static final QConfigTimer updateConfigTypeCache = Metrics.timer("qconfig.freshConfigTypeCache");

    public static final QConfigTimer freshConfigVersionCacheTimer = Metrics.timer("qconfig.freshConfigVersionCache");

    public final static QConfigTimer freshFixedVersionConsumerCache = Metrics.timer("qconfig.freshFixedVersionConsumerCache");

    public static final QConfigTimer freshPushVersionCache = Metrics.timer("qconfig.freshPushVersionCache");

}
