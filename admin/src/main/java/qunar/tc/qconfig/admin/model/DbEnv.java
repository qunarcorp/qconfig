package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.common.util.Environment;

/**
 * Created by dongcao on 2018/7/3.
 */
public enum DbEnv {

    PROD, BETA;

    public static DbEnv fromProfile(String profile) {
        Environment env = Environment.fromProfile(profile);
        if (env.isResources() || env.isProd()) {
            return PROD;
        }

        return BETA;
    }

}
