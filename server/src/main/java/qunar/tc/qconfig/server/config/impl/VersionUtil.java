package qunar.tc.qconfig.server.config.impl;

import com.google.common.base.Optional;

/**
 * @author zhenyu.nie created on 2018 2018/5/24 14:30
 */
public class VersionUtil {

    public static Optional<Long> getLoadVersion(Optional<Long> publishVersion, Optional<Long> pushVersion) {
        if (!publishVersion.isPresent()) {
            return Optional.absent();
        } else if (publishVersion.isPresent() && !pushVersion.isPresent()) {
            return publishVersion;
        } else {
            return publishVersion.get() > pushVersion.get() ? publishVersion : pushVersion;
        }
    }
}
