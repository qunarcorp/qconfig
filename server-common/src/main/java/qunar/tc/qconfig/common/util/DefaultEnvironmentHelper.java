package qunar.tc.qconfig.common.util;

public class DefaultEnvironmentHelper extends EnvironmentHelper {

    @Override
    public boolean isResources(Environment env) {
        return RESOURCES_SET.contains(env.env());
    }

    @Override
    public boolean isProd(Environment env) {
        return PROD_SET.contains(env.env());
    }

    @Override
    public boolean isBeta(Environment env) {
        return BETA_SET.contains(env.env());
    }

    @Override
    public boolean isDev(Environment env) {
        // 未定义的环境暂时都归入DEV
        return DEV_SET.contains(env.env()) || (!isResources(env) && !isProd(env) && !isBeta(env));
    }

    @Override
    public EnvType getEnvType(Environment env) {
        EnvType envType = fromName(env.env());
        // 未定义的环境暂时都归入DEV
        return envType != EnvType.OTHER ? envType : EnvType.DEV;
    }
}
