package qunar.tc.qconfig.client.impl;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-5-14.
 */

class FileVersion {

    private final Type type;

    private final VersionProfile version;

    public FileVersion(Type type, VersionProfile version) {
        this.type = type;
        this.version = version;
    }

    public Type getType() {
        return type;
    }

    public VersionProfile getVersion() {
        return version;
    }

    public enum Type {
        override, remote
    }

    public static boolean needUpdate(FileVersion v1, FileVersion v2) {

        if (v1 == null)
            return true;

        if (v1.type == Type.override) {
            return v2.type == Type.override && v2.version.getVersion() > v1.version.getVersion();
        }

        if (v2.type == Type.override) {
            return true;
        }

        return v1.version.needUpdate(v2.version);
    }
}
