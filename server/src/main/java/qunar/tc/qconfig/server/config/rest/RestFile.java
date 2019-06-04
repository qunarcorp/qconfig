package qunar.tc.qconfig.server.config.rest;

import qunar.tc.qconfig.server.config.qfile.QFile;

/**
 * @author zhenyu.nie created on 2018 2018/3/27 19:52
 */
public class RestFile {

    private final QFile qFile;

    private final long version;

    private final String data;

    private final String checksum;

    public RestFile(QFile qFile, long version, String data, String checksum) {
        this.qFile = qFile;
        this.version = version;
        this.data = data;
        this.checksum = checksum;
    }

    public QFile getQFile() {
        return qFile;
    }

    public long getVersion() {
        return version;
    }

    public String getData() {
        return data;
    }

    public String getChecksum() {
        return checksum;
    }

    @Override
    public String toString() {
        return "RestFile{" +
                "qFile=" + qFile +
                ", version=" + version +
                ", data='" + data + '\'' +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}
