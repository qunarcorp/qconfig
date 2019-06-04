package qunar.tc.qconfig.admin.web.controller;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import qunar.tc.qconfig.admin.dao.ClientLogDao;
import qunar.tc.qconfig.admin.model.ClientLog;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author keli.wang
 * @since 2017/5/12
 */
@Controller
public class ClientLogController {
    private static final Ordering<ClientLog> CLIENT_LOG_ORDERING = new Ordering<ClientLog>() {
        @Override
        public int compare(ClientLog left, ClientLog right) {
            if (left == null || right == null) {
                throw new IllegalArgumentException("参数不能为空");
            }
            return ComparisonChain.start()
                    .compare(left.getTime(), right.getTime(), Ordering.natural().reverse())
                    .compare(left.getType(), right.getType(), Ordering.natural().reverse())
                    .result();
        }
    };

    @Resource
    private ClientLogDao clientLogDao;

    @RequestMapping("/view/recentClientLogs")
    @ResponseBody
    public List<ClientLog> recentClientLogs(@RequestParam("group") final String group,
                                            @RequestParam("profile") final String profile,
                                            @RequestParam("dataId") final String dataId,
                                            @RequestParam("basedVersion") final long basedVersion) {
        return CLIENT_LOG_ORDERING.immutableSortedCopy(clientLogDao.selectRecent(group, profile, dataId, basedVersion));
    }
}
