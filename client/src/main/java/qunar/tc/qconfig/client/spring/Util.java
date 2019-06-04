package qunar.tc.qconfig.client.spring;

import com.google.common.collect.Maps;
import qunar.tc.qconfig.client.Feature;
import qunar.tc.qconfig.client.Numbers;

import java.util.Map;

/**
 * User: zhaohuiyu
 * Date: 6/9/14
 * Time: 12:34 PM
 */
class Util {
    public static Map.Entry<File, Feature> parse(String file, boolean trimValue) {
        String name = file;
        String group = null;
        long version = -1;

        int idx = file.indexOf('#');
        if (idx != -1) {
            group = file.substring(0, idx);
            name = file = file.substring(idx + 1);
        }
        idx = file.indexOf(':');
        if (idx != -1) {
            name = file.substring(0, idx);
            version = Numbers.toLong(file.substring(idx + 1), -1);
        }
        Feature fe = Feature.create().minimumVersion(version).autoReload(true).setTrimValue(trimValue).build();
        return Maps.immutableEntry(new File(group, name), fe);
    }

    public static class File {
        public final String group;

        public final String file;

        public File(String group, String file) {
            this.group = group;
            this.file = file;
        }

        @Override
        public String toString() {
            return "File{" +
                    "group='" + group + '\'' +
                    ", file='" + file + '\'' +
                    '}';
        }
    }
}
