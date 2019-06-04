package qunar.tc.qconfig.server.serverself.serverinfo;

/**
 * Server分集群prod/beta
 */
public enum ClusterType {
    PROD, BETA;

    public static ClusterType of(String name) {
        for (ClusterType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        if ("pro".equalsIgnoreCase(name)) {
            return PROD;
        }
        throw new RuntimeException("illegal cluster type: " + name);
    }
}
