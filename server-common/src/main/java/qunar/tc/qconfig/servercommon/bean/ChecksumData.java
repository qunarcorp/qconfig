package qunar.tc.qconfig.servercommon.bean;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-7
 * Time: 下午12:54
 */
public class ChecksumData<T> {

    private String checkSum;

    private T data;

    private ChecksumData(String checkSum, T data) {
        this.checkSum = checkSum;
        this.data = data;
    }

    public static <U> ChecksumData<U> of(String checksum, U data) {
        return new ChecksumData<U>(checksum, data);
    }

    public String getCheckSum() {
        return checkSum;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ChecksumData{" +
                "checkSum=" + checkSum+
                ", data=" + data +
                '}';
    }
}
