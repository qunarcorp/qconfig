package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.exception.ModifiedException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhenyu.nie created on 2014 2014/5/28 22:52
 */
public interface ProfileService {

    void create(String group, String profile) throws ModifiedException;

    void batchCreate(String group, Set<String> profile);

    List<String> find(String group);

    List<Map.Entry<String, String>> find(Collection<String> group);

    boolean exist(String group, String profile);

    void delete(String group, String profile);

}
