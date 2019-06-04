package qunar.tc.qconfig.admin.service.impl;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.model.QConfigFileType;
import qunar.tc.qconfig.admin.service.QConfigFileTypeService;
import qunar.tc.qconfig.client.QTable;
import qunar.tc.qconfig.client.spring.QConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yunfeng.yang
 * @since 2017/4/25
 */
@Service
public class QConfigFileTypeServiceImpl implements QConfigFileTypeService {

    @QConfig("qconfig_file_type.t")
    private QTable qconfigFileTypeTemplate;

    @Override
    public List<QConfigFileType> findAllQConfigFileTypes() {
        Map<String, Map<String, String>> table = qconfigFileTypeTemplate.rowMap();
        List<QConfigFileType> qConfigFileTypes = Lists.newArrayList();
        Set<String> types = table.keySet();
        for (String type : types) {
            Map<String, String> map = table.get(type);
            QConfigFileType qConfigFileType = new QConfigFileType(type, map.get("suffix"), map.get("description"), map.get("icon"));
            qConfigFileTypes.add(qConfigFileType);
        }
        return qConfigFileTypes;
    }
}
