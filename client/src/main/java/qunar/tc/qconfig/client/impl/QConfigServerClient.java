package qunar.tc.qconfig.client.impl;

import com.google.common.util.concurrent.ListenableFuture;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.common.util.ConfigLogType;

import java.io.IOException;
import java.util.Map;

/**
 * Created by zhaohui.yu
 * 1/28/18
 */
public interface QConfigServerClient {
    ListenableFuture<TypedCheckResult> checkUpdate(Map<Meta, VersionProfile> files);

    ListenableFuture<TypedCheckResult> loadGroupFiles();

    ListenableFuture<TypedCheckResult> longPollingCheckUpdate(Map<Meta, VersionProfile> files);

    ListenableFuture<Snapshot<String>> loadData(Meta key, VersionProfile version, Feature feature);

    ListenableFuture<Snapshot<String>> forceReload(Meta key, long minVersion, Feature feature);

    void recordLoading(ConfigLogType type, Meta meta, long version, String errorInfo) throws IOException;
}
