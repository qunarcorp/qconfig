package qunar.tc.qconfig.admin.service.impl;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.impl.InheritConfigDaoImpl;
import qunar.tc.qconfig.admin.model.InheritConfigMeta;
import qunar.tc.qconfig.common.bean.PaginationResult;
import qunar.tc.qconfig.admin.support.PaginationUtil;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import javax.annotation.Resource;
import java.util.List;

/**
 * 处理继承关系的配置文件服务
 * <p>
 * Created by chenjk on 2017/4/1.
 */
@Service
public class InheritConfigServiceImpl {

    @Resource(name = "inheritConfigDao")
    private InheritConfigDaoImpl inheritConfigDao;

    public boolean parentFileExists(String groupId, String dataId, String profile) {
        return inheritConfigDao.parentFileExists(groupId, dataId, profile);
    }

    public InheritConfigMeta queryInheritInfoDetail(InheritConfigMeta meta) {
        return inheritConfigDao.queryDetail(meta);
    }

    public PaginationResult countParentFile(String groupId, String profile, String term, int page, int pageSize) {
        List<ConfigMeta> data = Lists.newLinkedList();
        PaginationResult<ConfigMeta> paginationResult = new PaginationResult<>();
        paginationResult.setData(data);
        long start = PaginationUtil.start(page, pageSize);
        long count = inheritConfigDao.countInheritableFiles(groupId, profile, term);
        paginationResult.setPage(page);
        paginationResult.setPageSize(pageSize);
        paginationResult.setTotal(count);
        paginationResult.setTotalPage((int) PaginationUtil.totalPage(count, pageSize));
        if (count == 0 || start > count) {
            return paginationResult;
        } else {
            paginationResult.setData(inheritConfigDao.inheritableFile(groupId, profile, term, start, pageSize));
        }
        return paginationResult;
    }

    public boolean childFileExists(String groupId, String dataId, String profile) {//留着也许以后有用
        return inheritConfigDao.childFileExists(new ConfigMeta(groupId, dataId, profile));
    }
}
