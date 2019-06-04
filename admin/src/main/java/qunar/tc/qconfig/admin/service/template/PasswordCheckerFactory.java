package qunar.tc.qconfig.admin.service.template;

import org.springframework.stereotype.Service;

/**
 * @author zhenyu.nie created on 2016 2016/10/24 18:36
 */
@Service
public class PasswordCheckerFactory extends TextCheckerFactory {

    @Override
    public String type() {
        return "password";
    }
}
