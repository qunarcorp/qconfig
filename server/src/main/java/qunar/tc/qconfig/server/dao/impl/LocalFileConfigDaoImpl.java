package qunar.tc.qconfig.server.dao.impl;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.common.util.ChecksumAlgorithm;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.FileUtil;
import qunar.tc.qconfig.server.dao.FileConfigDao;
import qunar.tc.qconfig.server.exception.ChecksumFailedException;
import qunar.tc.qconfig.server.exception.FileDaoProcessException;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-7
 * Time: 下午12:36
 */
@Repository
public class LocalFileConfigDaoImpl implements FileConfigDao {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileConfigDaoImpl.class);

    private static final File CONF_HOME = new File(FileUtil.getFileStore());

    private static final String CHECKSUM_SUFFIX = ".checksum";

    @Override
    public boolean delete(ConfigMeta meta) {
        List<File> files = getFiles(meta);
        if (files.isEmpty()) {
            return true;
        }

        boolean allDelete = true;
        for (File file : files) {
            boolean delete = file.delete();
            if (!delete) {
                allDelete = false;
                logger.warn("delete file failOf, [{}]", file.getAbsolutePath());
            }
        }
        return allDelete;
    }

    private List<File> getFiles(final ConfigMeta meta) {
        File file = new File(CONF_HOME, meta.getGroup());
        if (!file.exists() || !file.isDirectory()) {
            return ImmutableList.of();
        }

        File[] files = file.listFiles(new FilenameFilter() {

            private String prefix = getLastFilePathPrefix(meta);

            @Override
            public boolean accept(File dir, String name) {
                if (!name.startsWith(prefix)) {
                    return false;
                }

                String versionPart;
                if (name.endsWith(CHECKSUM_SUFFIX)) {
                    versionPart = name.substring(prefix.length(), name.length() - CHECKSUM_SUFFIX.length());
                } else {
                    versionPart = name.substring(prefix.length());
                }

                try {
                    Integer.parseInt(versionPart);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });
        return files != null ? ImmutableList.copyOf(files) : ImmutableList.<File>of();
    }

    @Override
    public void save(VersionData<ConfigMeta> configId, ChecksumData<String> config) throws FileDaoProcessException {

        try {
            File file = getFilePath(configId);
            File checksumFile = new File(file.getAbsolutePath() + CHECKSUM_SUFFIX);

            if (file.exists()) {
                logger.warn("exist config file for {}", configId);
                return;
            }
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new FileDaoProcessException(configId, "can not create parent file " + file + "file is " + file.getParentFile());
                }
            }
            File tempFile = File.createTempFile("config", ".tmp");
            Files.asCharSink(tempFile, Constants.UTF_8).write(config.getData());
            Files.asCharSink(checksumFile, Constants.UTF_8).write(config.getCheckSum());
            boolean result = tempFile.renameTo(file);
            if (!result) {
                throw new FileDaoProcessException(configId, "rename config file error");
            }
        } catch (IOException e) {
            throw new FileDaoProcessException(configId, "write config file error");
        }
    }

    @Override
    public ChecksumData<String> find(VersionData<ConfigMeta> configId) throws FileDaoProcessException, ChecksumFailedException {
        try {
            File file = getFilePath(configId);
            if (!file.exists()) {
                return null;
            }
            File checksumFile = new File(file.getAbsolutePath() + ".checksum");
            if (!checksumFile.exists()) {
                file.delete();
                return null;
            }

            String content = Files.asCharSource(file, Constants.UTF_8).read();
            String checksum =  Files.asCharSource(checksumFile, Constants.UTF_8).read();
            String actualChecksum = ChecksumAlgorithm.getChecksum(content);
            if (!actualChecksum.equals(checksum)) {
                file.delete();
                checksumFile.delete();
                throw new ChecksumFailedException();
            }
            return ChecksumData.of(checksum, content);
        } catch (IOException e) {
            throw new FileDaoProcessException(configId, "read config file error");
        }
    }

    private File getFilePath(VersionData<ConfigMeta> configId) {
        ConfigMeta meta = configId.getData();
        return new File(CONF_HOME, meta.getGroup() + "/" + getLastFilePath(meta, configId.getVersion()));
    }

    private static String getLastFilePath(ConfigMeta meta, long version) {
        return getLastFilePathPrefix(meta) + version;
    }

    private static String getLastFilePathPrefix(ConfigMeta meta) {
        try {
            return URLEncoder.encode(meta.getDataId().toLowerCase() + "." + meta.getProfile().toLowerCase() + ".", "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("not support utf8");
        }
    }
}