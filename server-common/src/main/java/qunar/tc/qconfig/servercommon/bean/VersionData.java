package qunar.tc.qconfig.servercommon.bean;

import java.util.Objects;

/**
 * User: zhaohuiyu
 * Date: 5/13/14
 * Time: 6:11 PM
 */
public class VersionData<T> {

    private long version;

    private T data;

    public VersionData() {
    }

    public VersionData(long version, T data) {
        this.version = version;
        this.data = data;
    }

    public static <U> VersionData<U> of(long version, U data) {
        return new VersionData<U>(version, data);
    }

    public long getVersion() {
        return version;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "VersionData{" +
                "version=" + version +
                ", data=" + data +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionData<?> that = (VersionData<?>) o;
        return version == that.version &&
                data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, data);
    }
}
