package qunar.tc.qconfig.client.impl;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import qunar.tc.qconfig.common.application.ServerManagement;
import qunar.tc.qconfig.common.application.ServerManager;

/**
 * @author zhenyu.nie created on 2015 2015/4/20 14:49
 */
class Tokens {

    private static final Supplier<String> tokenSup = Suppliers.memoize(new Supplier<String>() {
        @Override
        public String get() {
            ServerManagement serviceInstance = ServerManager.getInstance();
            if (serviceInstance == null) {
                throw new RuntimeException("请检测是否正确配置ServerManagement");
        }
            try {
                return serviceInstance.getAppServerConfig().getToken();
            } catch (Exception e) {
                throw new RuntimeException("请检查是否在app-info中配置token");
            }
        }
    });

    public static String getToken() {
        return tokenSup.get();
    }
}
