package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.model.PublishedConfigInfo;
import qunar.tc.qconfig.common.bean.PaginationResult;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

public interface UserBehaviorService {

    PaginationResult<PublishedConfigInfo> listFavoriteFileInfo(String user, int page, int pageSize);

    List<String> listFavoriteGroups(String user, int page, int pageSize);

    boolean isFavoriteGroup(String group, String user);

    boolean isFavoriteFile(ConfigMeta meta, String user);

    void insertFavoriteGroup(String group, String user);

    void insertFavoriteFile(ConfigMeta meta, String user);

    void deleteFavoriteGroup(String group, String user);

    void deleteFavoriteFile(ConfigMeta meta, String user);

    void deleteFavorites(ConfigMeta meta);

    List<PublishedConfigInfo> listUserLastModifiedFile(String user, int size);
}
