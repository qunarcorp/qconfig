package qunar.tc.qconfig.admin.web.security;

/**
 * @author zhenyu.nie created on 2014 2014/5/21 10:35
 */
public final class PermissionType {
    public final static PermissionType VIEW = new PermissionType(Type.VIEW);
    public final static PermissionType EDIT = new PermissionType(Type.EDIT);
    public final static PermissionType APPROVE = new PermissionType(Type.APPROVE);
    public final static PermissionType PUBLISH = new PermissionType(Type.PUBLISH);
    public final static PermissionType LEADER = new PermissionType(Type.LEADER);

    private static final int MAX_MASK_COUNT = 2;

    private int mask;
    private String text;
    private String name;

    private PermissionType(Type type) {
        this.mask = type.mask;
        this.text = type.text;
        this.name = type.name().toLowerCase();
    }

    private PermissionType(int mask, String text) {
        this.mask = mask;
        this.text = text;
    }

    public String text() {
        return text;
    }

    public String name() {
        return name;
    }

    public int mask() {
        return mask;
    }

    public boolean hasPermission(int permission) {
        return this.mask == 0 || (mask & permission) != 0;
    }

    public static PermissionType of(int permission) {
        if (permission == Type.VIEW.mask) {
            return VIEW;
        } else if (permission == Type.EDIT.mask) {
            return EDIT;
        } else if (permission == Type.APPROVE.mask) {
            return APPROVE;
        } else if (permission == Type.PUBLISH.mask) {
            return PUBLISH;
        } else {
            if (permission != Type.LEADER.mask) {
                PermissionType permissionType = build(permission);
                if (permissionType != null) {
                    return permissionType;
                }
            }
            throw new IllegalArgumentException("无效的权限: " + permission);
        }
    }

    private static PermissionType build(int permission) {
        int mask = 0;
        int maskCount = 0;
        StringBuilder builder = new StringBuilder();
        for (Type type : Type.values()) {
            if (type == Type.LEADER) continue;
            if ((permission & type.mask) != 0) {
                maskCount++;
                mask |= type.mask;
                if (builder.length() > 0) {
                    builder.append("、");
                }
                builder.append(type.text);
            }
        }
        if (mask == 0 || maskCount > MAX_MASK_COUNT) {
            throw new IllegalArgumentException("无效的权限: " + permission);
        }

        return new PermissionType(mask, builder.toString());
    }

    public static int setPermission(int permission, PermissionType permissionType) {
        return permission | permissionType.mask;
    }

    private static enum Type {
        VIEW(0, "查看"), EDIT(1, "编辑"), APPROVE(2, "审核"), PUBLISH(4, "发布"), LEADER(-1, "leader");

        private int mask;
        private String text;

        private Type(int mask, String text) {
            this.mask = mask;
            this.text = text;
        }
    }
}
