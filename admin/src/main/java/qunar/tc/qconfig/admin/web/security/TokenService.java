package qunar.tc.qconfig.admin.web.security;

/**
 * @author zhenyu.nie created on 2015 2015/4/20 23:30
 */
public interface TokenService {

    App decode(String token);

    public static class App {
        public final String name;

        public final String env;

        public final String ip;

        public App(String name, String env, String ip) {
            this.name = name;
            this.env = env;
            this.ip = ip;
        }
    }
}
