package qunar.tc.qconfig.client.validate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.common.util.VersionedJsonResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

/**
 * @author zhenyu.nie created on 2017 2017/2/15 15:49
 */
public class ValidatorUtil {

    private static final Logger logger = LoggerFactory.getLogger(ValidatorUtil.class);

    private static final int version = 0;

    private static final ObjectMapper mapper = MapperHolder.getObjectMapper();

    public static void validate(HttpServletRequest request, HttpServletResponse response, Validator validator) {
        try {
            response.setContentType("application/json; charset=utf-8");
            validate(new BufferedReader(new InputStreamReader(request.getInputStream(), Charsets.UTF_8)), response.getWriter(), validator);
        } catch (Exception e) {
            logger.error("start http validate qconfig table error", e);
        }
    }

    public static void validate(Reader reader, Writer writer, Validator validator) {
        Object error;
        try {
            Object validateResult = validator.validate(reader);
            error = new VersionedJsonResponse<Object>(0, null, mapper.writeValueAsString(validateResult), version);
        } catch (Exception e) {
            logger.error("validate qconfig table error", e);
            error = new VersionedJsonResponse<Object>(-1, "doing validate error", null, version);
        }

        try {
            writer.write(mapper.writeValueAsString(error));
        } catch (Exception e) {
            logger.error("write qconfig table validate info error", e);
        }
    }

}
