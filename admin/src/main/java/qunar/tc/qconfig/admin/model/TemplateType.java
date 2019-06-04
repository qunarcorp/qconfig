package qunar.tc.qconfig.admin.model;

import com.google.common.collect.ImmutableMap;

/**
 * @author keli.wang
 * @since 2017/6/5
 */
public enum TemplateType {

    TABLE(0, "表格"), JSON_SCHEMA(1, "JSON"), PROPERTIES(2, "properties");

    private static final ImmutableMap<Integer, TemplateType> INSTANCES;

    static {
        final ImmutableMap.Builder<Integer, TemplateType> builder = ImmutableMap.builder();
        for (final TemplateType type : values()) {
            builder.put(type.getCode(), type);
        }
        INSTANCES = builder.build();
    }

    public static TemplateType fromCode(final int code) {
        if (INSTANCES.containsKey(code)) {
            return INSTANCES.get(code);
        }

        throw new RuntimeException("unsupported code");
    }

    private final int code;
    private final String name;

    TemplateType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
