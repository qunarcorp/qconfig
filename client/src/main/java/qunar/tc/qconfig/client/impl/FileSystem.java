package qunar.tc.qconfig.client.impl;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by zhaohui.yu
 * 1/23/18
 */
class FileSystem {
    private static final Logger logger = LoggerFactory.getLogger(FileSystem.class);

    static void atomicWriteFile(File target, String content) {
        File tempFile = null;
        try {
            if (target.exists() && !target.delete()) return;
            target.getParentFile().mkdirs();

            tempFile = File.createTempFile("config", ".tmp");
            Files.asCharSink(tempFile, Charsets.UTF_8).write(content);
            if (!target.exists() && !tempFile.renameTo(target)) {
                synchronized (FileStore.class) {
                    if (!target.exists()) {
                        Files.copy(tempFile, target);
                    }
                }
            }

            //只允许创建的用户读取
            target.setReadable(false, false);
            target.setReadable(true, true);
        } catch (Throwable e) {
            logger.info("write file failed: " + target.getAbsolutePath(), e);
        } finally {
            try {
                if (tempFile != null && tempFile.exists() && !tempFile.delete())
                    logger.warn("delete temp version file failed, name={}", tempFile);
            } catch (Throwable e) {
                logger.debug("delete temp version file failed", e);
            }
        }
    }
}
