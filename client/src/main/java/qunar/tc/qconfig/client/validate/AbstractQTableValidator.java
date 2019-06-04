package qunar.tc.qconfig.client.validate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qunar.tc.qconfig.client.DefaultQTable;
import qunar.tc.qconfig.client.TableConfig;
import qunar.tc.qconfig.common.util.QTableError;
import qunar.tc.qconfig.common.util.VersionedJsonRequest;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author zhenyu.nie created on 2017 2017/2/17 11:51
 */
public abstract class AbstractQTableValidator implements Validator {

    private static final Logger logger = LoggerFactory.getLogger(AbstractQTableValidator.class);

    private static final ObjectMapper mapper = MapperHolder.getObjectMapper();

    private boolean trimValue;

    public AbstractQTableValidator() {
        this(true);
    }

    public AbstractQTableValidator(boolean trimValue) {
        this.trimValue = trimValue;
    }

    private DefaultValidateQTable parse(Reader reader) throws IOException {
        String input = CharStreams.toString(reader);
        TableConfig.TableParser parser = TableConfig.getParser(trimValue);
        return new DefaultValidateQTable(new DefaultQTable(parser.parse(input)));
    }

    @Override
    public QTableError validate(Reader reader) {
        try {
            VersionedJsonRequest<String> request = mapper.readValue(reader, new TypeReference<VersionedJsonRequest<String>>() {});
            DefaultValidateQTable qTable = parse(new StringReader(request.getData()));
            doValidate(qTable);
            return qTable.getError();
        } catch (IOException e) {
            logger.error("read input from reader error", e);
            throw new RuntimeException(e);
        }
    }

    protected abstract void doValidate(ValidateQTable table);
}
