package qunar.tc.qconfig.admin.dao.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import qunar.tc.qconfig.admin.dao.SnapshotDao;
import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.model.DbOpType;
import qunar.tc.qconfig.admin.model.JdbcTemplateDelegated;
import qunar.tc.qconfig.admin.support.IntArrayUtil;
import qunar.tc.qconfig.common.bean.CandidateSnapshot;
import qunar.tc.qconfig.common.util.ChecksumAlgorithm;
import qunar.tc.qconfig.servercommon.bean.ChecksumData;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * User: zhaohuiyu
 * Date: 5/13/14
 * Time: 2:43 PM
 */
@Repository
public class SnapshotDaoImpl implements SnapshotDao {

    private static final String SAVE_SNAPSHOT = "INSERT IGNORE INTO config_snapshot(group_id,data_id,profile,based_version,version,checksum,content) VALUES(?,?,?,?,?,?,?)";

    private static final String SAVE_SNAPSHOT_BETA = "INSERT IGNORE INTO config_snapshot(group_id,data_id,profile,based_version,version,checksum,content,create_time) VALUES(?,?,?,?,?,?,?,?)";

    private static final String FIND_SNAPSHOT = "SELECT content,checksum FROM config_snapshot WHERE group_id=? AND data_id=? AND profile=? AND version=?";

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void save(VersionData<ConfigMeta> configId, ChecksumData<String> checksumData, long basedVersion) {
        ConfigMeta configMeta = configId.getData();
        jdbcTemplate.update(SAVE_SNAPSHOT, configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile(),
                basedVersion, configId.getVersion(), checksumData.getCheckSum(), checksumData.getData());
    }

    @Override
    public int batchSave(List<CandidateSnapshot> candidateSnapshotList) {
        List<Object[]> params = Lists.newLinkedList();
        for (CandidateSnapshot snapshot : candidateSnapshotList) {
            String checksum = ChecksumAlgorithm.getChecksum(snapshot.getData());
            Object[] param = {
                    snapshot.getGroup(), snapshot.getDataId(), snapshot.getProfile(),
                    snapshot.getBasedVersion(), snapshot.getEditVersion(), checksum, snapshot.getData()
            };
            params.add(param);
        }
        return IntArrayUtil.getSum(jdbcTemplate.batchUpdate(SAVE_SNAPSHOT, params));
    }

    @Override
    public void saveBeta(VersionData<ConfigMeta> configId, ChecksumData<String> checksumData, long basedVersion, Timestamp updateTime) {
        ConfigMeta meta = configId.getData();
        jdbcTemplate.update(SAVE_SNAPSHOT_BETA, meta.getGroup(), meta.getDataId(), meta.getProfile(),
                basedVersion, configId.getVersion(), checksumData.getCheckSum(), checksumData.getData(), updateTime);
    }

    @Override
    public ChecksumData<String> find(VersionData<ConfigMeta> configId) {
        ConfigMeta configMeta = configId.getData();
        return jdbcTemplate.query(FIND_SNAPSHOT, EXTRACTOR, configMeta.getGroup(), configMeta.getDataId(), configMeta.getProfile(), configId.getVersion());
    }

    @Override
    public int completeDelete(ConfigMeta meta) {
        return jdbcTemplate.update("DELETE FROM config_snapshot WHERE group_id=? AND data_id=? AND profile=?",
                meta.getGroup(), meta.getDataId(), meta.getProfile());
    }

    private static final ResultSetExtractor<ChecksumData<String>> EXTRACTOR = new ResultSetExtractor<ChecksumData<String>>() {
        @Override
        public ChecksumData<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (rs.next()) {
                return ChecksumData.of(rs.getString("checksum"), rs.getString("content"));
            }
            return null;
        }
    };
}
