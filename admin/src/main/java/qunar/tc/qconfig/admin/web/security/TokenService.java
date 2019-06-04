package qunar.tc.qconfig.admin.web.security;

/**
 * @author zhenyu.nie created on 2015 2015/4/20 23:30
 */
public interface TokenService {

    String decode(String token);

}
