package qunar.tc.qconfig.admin.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

/**
 * @author zhenyu.nie created on 2016 2016/3/9 15:04
 */
public class JsonUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    public static Optional<JsonNode> read(String data) {
        try {
            if (StringUtils.isBlank(data)) {
                data = "\"\"";
            }
            return Optional.of(mapper.readTree(data));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
