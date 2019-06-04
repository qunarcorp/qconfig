package qunar.tc.qconfig.admin.model;

/**
 * @author zhenyu.nie created on 2015 2015/7/3 18:19
 */
public class DiffCount {

    private final int add;

    private final int delete;

    private final int modify;

    public DiffCount(int add, int delete) {
        this(add, delete, 0);
    }

    public DiffCount(int add, int delete, int modify) {
        this.add = add;
        this.delete = delete;
        this.modify = modify;
    }

    public int getAdd() {
        return add;
    }

    public int getDelete() {
        return delete;
    }

    public int getModify() {
        return modify;
    }

    public boolean hasDiff() {
        return add > 0 || delete > 0 || modify > 0;
    }

    public DiffCount add(DiffCount count) {
        return new DiffCount(add + count.add, delete + count.delete, modify + count.modify);
    }

    @Override
    public String toString() {
        return "DiffCount{" +
                "add=" + add +
                ", delete=" + delete +
                ", modify=" + modify +
                '}';
    }

}
