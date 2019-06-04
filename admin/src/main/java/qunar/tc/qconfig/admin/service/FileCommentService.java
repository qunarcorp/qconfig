package qunar.tc.qconfig.admin.service;

import qunar.tc.qconfig.admin.cloud.vo.CommentVo;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import java.util.List;
import java.util.Map;

public interface FileCommentService {

    void setComment(ConfigMeta meta, long version, String comment);

    String getComment(ConfigMeta meta, long version);

    // in history list
    Map<Long, String> getComments(ConfigMeta meta);

    Map<Long, String> getComments(ConfigMeta meta, long startVersion, long endVersion);

    List<CommentVo> getCommentsByMeta(ConfigMeta meta);

    void completeDelete(ConfigMeta meta);
}
