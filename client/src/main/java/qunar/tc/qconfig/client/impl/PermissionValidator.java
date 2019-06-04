package qunar.tc.qconfig.client.impl;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lepdou 2017-07-06
 */
class PermissionValidator {

    private static final Logger logger = LoggerFactory.getLogger(PermissionValidator.class);
    private static final PermissionValidator INSTANCE = new PermissionValidator();
    private static final Gson gson = new Gson();

    private QConfigServerClient client;

    private PermissionValidator() {
        client = QConfigServerClientFactory.create();
    }

    public static PermissionValidator getInstance() {
        return INSTANCE;
    }

    private boolean isEmpty(String appInfoJsonStr) {
        return appInfoJsonStr == null || appInfoJsonStr.equals("");
    }

    private boolean contain(String[] set, String target) {
        if (set == null || set.length == 0) {
            return false;
        }

        for (String ele : set) {
            if (ele.equals(target)) {
                return true;
            }
        }

        return false;
    }

    private static class AppInfoResponse {
        private AppInfo data;

        public AppInfo getData() {
            return data;
        }

        public void setData(AppInfo data) {
            this.data = data;
        }
    }

    private static class AppInfo {
        private String[] developer;
        private String[] owner;

        public String[] getDeveloper() {
            return developer;
        }

        public void setDeveloper(String[] developer) {
            this.developer = developer;
        }

        public String[] getOwner() {
            return owner;
        }

        public void setOwner(String[] owner) {
            this.owner = owner;
        }
    }
}
