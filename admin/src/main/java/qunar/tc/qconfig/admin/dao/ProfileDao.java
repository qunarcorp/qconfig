package qunar.tc.qconfig.admin.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author zhenyu.nie created on 2014 2014/5/28 22:23
 */
public interface ProfileDao {

    void create(String group, String profile, String operator);

    void batchCreate(String group, List<String> profiles, String operator);

    List<String> selectProfiles(String group);

    List<Map.Entry<String, String>> selectProfiles(Collection<String> groups);

    boolean exist(String group, String profile);

    int completeDelete(String group, String profile);
}
