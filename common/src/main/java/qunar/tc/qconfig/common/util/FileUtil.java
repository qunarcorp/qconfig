package qunar.tc.qconfig.common.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-11-26
 * Time: 下午12:50
 */
public class FileUtil {

    private static Supplier<String> store = Suppliers.memoize(new Supplier<String>() {
        @Override
        public String get() {
            String path = System.getProperty("file.cache", null);

            if (path == null) {
                path = System.getProperty("catalina.base");
                if (path == null) path = System.getProperty("java.io.tmpdir");
                path = path + File.separator + "cache";
                System.setProperty("file.cache", path);
            }

            File file = new File(path);
            file.mkdirs();

            try {
                path = file.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return path;
        }
    });



    public static String getFileStore() {
        return store.get();
    }
}