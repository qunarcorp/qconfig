package qunar.tc.qconfig.client.validate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author zhenyu.nie created on 2017 2017/2/20 12:24
 */
public class MapperHolder {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper getObjectMapper() {
        return mapper;
    }
}
