package qunar.tc.qconfig.server.security;

import qunar.tc.qconfig.server.exception.AccessForbiddenException;

/**
 * @author zhenyu.nie created on 2016 2016/4/21 17:10
 */
public interface DifferentGroupAccessService {

    void checkAccessPermission(String clientGroup, String fileGroup, String fileName) throws AccessForbiddenException;
}
