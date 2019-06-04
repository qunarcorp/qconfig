package qunar.tc.qconfig.common.util;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-8
 * Time: 上午11:54
 */
public final class ChecksumAlgorithm {

    public static String getChecksum(String data) {
        if (Strings.isNullOrEmpty(data)) return Constants.NO_FILE_CHECKSUM;
        return Hashing.murmur3_128().hashString(data, Charsets.UTF_8).toString();
    }
}
