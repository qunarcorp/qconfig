package qunar.tc.qconfig.client.impl;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-5-15.
 */
public class Meta {

    private final String key;
    private final String groupName;
    private final String fileName;

    public Meta(String groupName, String fileName) {
        this.key = createKey(groupName, fileName);
        this.groupName = groupName;
        this.fileName = fileName;
    }

    public String getKey() {
        return key;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getFileName() {
        return fileName;
    }

    public String toString() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Meta)) return false;

        Meta meta = (Meta) o;

        return key.equals(meta.key);

    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public static String createKey(String groupName, String fileName) {
        return groupName + "/" + fileName;
    }
}
