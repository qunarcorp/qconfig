package qunar.tc.qconfig.admin.service.impl;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.cloud.vo.CommentVo;
import qunar.tc.qconfig.admin.dao.CandidateSnapshotDao;
import qunar.tc.qconfig.admin.dao.FileCommentDao;
import qunar.tc.qconfig.admin.service.FileCommentService;
import qunar.tc.qconfig.admin.support.CheckUtil;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.bean.StatusType;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FileCommentServiceImpl implements FileCommentService {

    private final Logger logger = LoggerFactory.getLogger(FileCommentServiceImpl.class);
    private final static int MAX_COMMENT_SIZE = 150;

    @Resource
    private FileCommentDao fileCommentDao;

    @Resource
    private CandidateSnapshotDao candidateSnapshotDao;

    @Override
    public void setComment(ConfigMeta meta, long version, String comment) {
        CheckUtil.checkLegalMeta(meta);
        if (comment == null) {
            return;
        } else if (comment.length() > MAX_COMMENT_SIZE) {
            comment = comment.substring(0, MAX_COMMENT_SIZE);
        }
        try {
            fileCommentDao.insertOrUpdate(meta.getGroup(), meta.getProfile(), meta.getDataId(), version, comment);
        } catch (Exception e) {
            logger.error("set comment error, meta={}, version={}, comment={}", meta, version, comment, e);
        }
    }

    @Override
    public String getComment(ConfigMeta meta, long version) {
        return fileCommentDao.query(meta.getGroup(), meta.getProfile(), meta.getDataId(), version);
    }

    @Override
    public Map<Long, String> getComments(ConfigMeta meta) {
        return fileCommentDao.query(meta.getGroup(), meta.getProfile(), meta.getDataId());
    }

    @Override
    public Map<Long, String> getComments(ConfigMeta meta, long startVersion, long endVersion) {
        return fileCommentDao.query(meta.getGroup(), meta.getProfile(), meta.getDataId(), startVersion, endVersion);
    }

    @Override
    public List<CommentVo> getCommentsByMeta(ConfigMeta meta) {
        CandidateSnapshot snapshot = candidateSnapshotDao.findLatestCandidateSnapshot(meta);
        if (snapshot == null) return ImmutableList.of();
        final Map<Long, String> comments = getComments(meta, snapshot.getBasedVersion(), snapshot.getEditVersion());
        List<CandidateSnapshot> candidates = candidateSnapshotDao.getSnapshotAfterVersion(meta, snapshot.getBasedVersion());
        return candidates.stream().map(input -> new CommentVo(input.getUpdateTime(), comments.get(input.getEditVersion()),
                input.getEditVersion(), input.getOperator(), getOperationTypeByStatus(input.getStatus()))).collect(Collectors.toList());
    }

    @Override
    public void completeDelete(ConfigMeta meta) {
        CheckUtil.checkLegalMeta(meta);
        fileCommentDao.delete(meta.getGroup(), meta.getProfile(), meta.getDataId());
    }

    private String getOperationTypeByStatus(StatusType statusType) {
        String result = "";

        switch (statusType) {
            case PUBLISH:
                result = "发布";
                break;
            case PASSED:
                result = "审核";
                break;
            case PENDING:
                result = "编辑";
                break;
            case REJECT:
                result = "审核不通过";
                break;
            case CANCEL:
                result = "回退审核";
                break;
            case DELETE:
                result = "删除";
        }
        return result;
    }
}
