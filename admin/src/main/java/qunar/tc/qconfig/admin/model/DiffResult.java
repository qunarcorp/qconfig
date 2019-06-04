package qunar.tc.qconfig.admin.model;

/**
 * @author zhenyu.nie created on 2015 2015/7/3 18:12
 */
public class DiffResult<T> {

    private final DiffCount diffCount;

    private final T result;

    public DiffResult(DiffCount diffCount, T result) {
        this.diffCount = diffCount;
        this.result = result;
    }

    public DiffCount getDiffCount() {
        return diffCount;
    }

    public T getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "DiffResult{" +
                "diffCount=" + diffCount +
                ", result=" + result +
                '}';
    }
}
