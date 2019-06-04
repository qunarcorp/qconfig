package qunar.tc.qconfig.common.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * @author zhenyu.nie created on 2014 2014/6/30 15:51
 */
public class Environment {

    public static final Environment RESOURCES = generateDefaultEnv("resources");
    public static final Environment PROD = generateDefaultEnv("prod");
    public static final Environment BETA = generateDefaultEnv("beta");

    private String env;

    private String subEnv;

    private String profile;

    private String defaultProfile;

    private static EnvironmentHelper environmentHelper = null;

    static {
        // TODO qcloud环境可以定义其他策略实现
        environmentHelper = new DefaultEnvironmentHelper();
    }

    public boolean isResources() {
        return environmentHelper.isResources(this);
    }

    public boolean isProd() {
        return environmentHelper.isProd(this);
    }

    public boolean isBeta() {
        return environmentHelper.isBeta(this);
    }

    public boolean isDev() {
        return environmentHelper.isDev(this);
    }

    public EnvironmentHelper.EnvType getEnvType() {
        return environmentHelper.getEnvType(this);
    }

    private static Environment generateDefaultEnv(String env) {
        String envName = QConfigAttributesLoader.getInstance().getSymbol(env);
        String profile = envName + ":";
        return new Environment(envName, "", profile, profile);
    }

    private Environment(String env, String subEnv, String profile, String defaultProfile) {
        this.env = env;
        this.subEnv = subEnv;
        this.profile = profile;
        this.defaultProfile = defaultProfile;
    }

    public String text() {
        return this.env;
    }

    public String env() {
        return this.env;
    }

    public String subEnv() {
        return this.subEnv;
    }

    public String profile() {
        return profile;
    }

    public String defaultProfile() {
        return defaultProfile;
    }

    public static Environment fromEnvName(String envName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(envName), "illegal env name [%s]", envName);
        Preconditions.checkArgument(!envName.contains(":"), "illegal env name [%s]", envName);
        return fromProfile(envName + ":");
    }

    public static Environment fromProfile(String profile) {
        Preconditions.checkNotNull(profile);
        Preconditions.checkArgument(profile.length() <= PROFILE_MAX_LENGTH, "illegal profile [%s]", profile);

        int indexOfColon = profile.indexOf(":");
        Preconditions.checkArgument(indexOfColon > 0, "illegal profile [%s]", profile);

        String env;
        String subEnv;
        if (indexOfColon == profile.length() - 1) {
            env = profile.substring(0, profile.length() - 1);
            subEnv = "";
        } else {
            env = profile.substring(0, indexOfColon);
            subEnv = profile.substring(indexOfColon + 1);
        }

        return generate(env, subEnv, profile);
    }

    public static Environment generate(String env, String subEnv) {
        Preconditions.checkNotNull(env);
        Preconditions.checkNotNull(subEnv);
        Preconditions.checkArgument(env.length() <= ENV_MAX_LENGTH, "illegal profile [%s:%s]", env, subEnv);
        Preconditions.checkArgument(subEnv.length() <= BUILD_GROUP_MAX_LENGTH, "illegal profile [%s:%s]", env, subEnv);

        String profile = env + ":" + subEnv;
        return generate(env, subEnv, profile);
    }

    private static Environment generate(String env, String subEnv, String profile) {
        Preconditions.checkArgument(!env.isEmpty() && legalStr(env), "illegal profile [%s]", profile);
        Preconditions.checkArgument(legalStr(subEnv), "illegal profile [%s]", profile);
        String defaultProfile;
        if (subEnv.isEmpty()) {
            defaultProfile = profile;
        } else {
            defaultProfile = env + ":";
        }
        return new Environment(env, subEnv, profile, defaultProfile);
    }

    private static boolean legalStr(String str) {
        return ProfileUtil.BUILD_GROUP_LETTER_DIGIT_PATTERN.matcher(str).find();
    }

    public static boolean hasAffect(Environment lhs, Environment rhs) {
        if (lhs == null) {
            return false;
        }
        return lhs.equalsEnv(rhs) || lhs.isResources() || rhs.isResources();
    }

    public static Environment extractDefaultProfile(String profile) {
        int indexOfColon = profile.indexOf(":");
        if (indexOfColon < 0) {
            throw new IllegalArgumentException("Illegal profile. profile = " + profile);
        }
        return fromProfile(profile.substring(0, indexOfColon + 1));
    }

    // 仅env相同
    public boolean equalsEnv(Environment anotherEnv) {
        if (this.env == null || anotherEnv == null) {
            return false;
        }
        return this.env.equals(anotherEnv.env);
    }


    @Override
    public String toString() {
        return "Environment{" +
                "env='" + env + '\'' +
                ", subEnv='" + subEnv + '\'' +
                ", profile='" + profile + '\'' +
                ", defaultProfile='" + defaultProfile + '\'' +
                '}';
    }

    // profile相同
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Environment that = (Environment) o;

        return profile != null ? profile.equals(that.profile) : that.profile == null;
    }

    @Override
    public int hashCode() {
        return profile != null ? profile.hashCode() : 0;
    }

    public static final int ENV_MAX_LENGTH = 9;

    public static final int BUILD_GROUP_MAX_LENGTH = 20;

    public static final int PROFILE_MAX_LENGTH = BUILD_GROUP_MAX_LENGTH + ENV_MAX_LENGTH + 1;
}
