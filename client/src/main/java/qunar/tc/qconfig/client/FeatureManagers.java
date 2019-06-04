package qunar.tc.qconfig.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import qunar.tc.qconfig.client.impl.ConfigEngine;

import java.util.concurrent.ConcurrentMap;

/**
 * @author zhenyu.nie created on 2017 2017/5/23 15:24
 */
public class FeatureManagers {

    private static final String groupName = ((ConfigEngine) ConfigEngine.getInstance()).getGroupName();

    private static final ConcurrentMap<FileMeta, FeatureManager> managers = Maps.newConcurrentMap();

    public static FeatureManager get(String fileName) {
        return get(groupName, fileName);
    }

    public static FeatureManager get(String group, String fileName) {
        FileMeta meta = new FileMeta(group, fileName);
        FeatureManager manager = managers.get(meta);
        if (manager != null) {
            return manager;
        } else {
            MapConfig mapConfig = MapConfig.get(meta.getGroup(), meta.getFileName(), Feature.DEFAULT);
            FeatureManager newManager = new FeatureManager(mapConfig.asMap());
            FeatureManager oldManager = managers.putIfAbsent(meta, newManager);
            return oldManager == null ? newManager : oldManager;
        }
    }

    private static class FileMeta {
        private String group;
        private String fileName;

        public FileMeta(String group, String fileName) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(group), "app必须提供");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(fileName), "fileName必须提供");
            this.group = group;
            this.fileName = fileName;
        }

        public String getGroup() {
            return group;
        }

        public String getFileName() {
            return fileName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FileMeta fileMeta = (FileMeta) o;

            if (group != null ? !group.equals(fileMeta.group) : fileMeta.group != null) return false;
            return fileName != null ? fileName.equals(fileMeta.fileName) : fileMeta.fileName == null;
        }

        @Override
        public int hashCode() {
            int result = group != null ? group.hashCode() : 0;
            result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "FileMeta{" +
                    "group='" + group + '\'' +
                    ", fileName='" + fileName + '\'' +
                    '}';
        }
    }
}
