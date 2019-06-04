package qunar.tc.qconfig.admin.service.template.valuechecker;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.http.util.Asserts;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.Assert;


/**
 * @author zhenyu.nie created on 2016 2016/10/12 12:02
 */
public abstract class AbstractTimeRelativeValueChecker extends NullableChecker {

    public AbstractTimeRelativeValueChecker(ObjectNode node) {
        super(node);
    }

    protected abstract DateTimeFormatter getFormatter();

    @Override
    public void doCheck(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return;
        }
        try {
            getFormatter().parseDateTime(value);
        } catch (Exception e) {
            try {   // 允许使用时间戳格式
                Assert.isTrue(Long.parseLong(value) >= 0);
            } catch (Exception e2) {
                throw new IllegalArgumentException("时间值[" + value + "]格式错误");
            }
        }
    }
}
