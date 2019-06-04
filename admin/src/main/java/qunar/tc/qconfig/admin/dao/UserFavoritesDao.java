package qunar.tc.qconfig.admin.dao;

import qunar.tc.qconfig.admin.cloud.enums.UserFavoriteType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;

public interface UserFavoritesDao {

    List<String> listFavoriteGroups(String user, int page, int pageSize);

    List<ConfigMeta> listFavoriteFiles(String user, int page, int pageSize);

    int countFavoriteItems(String user, UserFavoriteType type);

    boolean isFavoriteGroup(String group, String user);

    boolean isFavoriteFile(ConfigMeta meta, String user);

    void insertFavoriteGroup(String group, String user);

    void insertFavoriteFile(ConfigMeta meta, String user);

    void deleteFavoriteGroup(String group, String user);

    void deleteFavoriteFile(ConfigMeta meta, String user);

    void deleteFavorites(ConfigMeta meta);
}
