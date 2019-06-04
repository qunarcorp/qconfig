package qunar.tc.qconfig.server.web.servlet;

import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.domain.Changed;
import qunar.tc.qconfig.servercommon.bean.ConfigMeta;
import qunar.tc.qconfig.servercommon.bean.VersionData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author lepdou 2017-09-25
 */
public class GetGroupFilesServlet extends AbstractCheckConfigServlet {

    @Override
    protected String getVersion() {
        return V2Version;
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String group = (String) req.getAttribute(Constants.GROUP_NAME);
        String profile = clientInfoService.getProfile();

        List<VersionData<ConfigMeta>> groupFiles = getConfigStore().loadByGroupAndProfile(group, profile);

        List<Changed> changedList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(groupFiles)) {
            doReturn(changedList, resp);
        }

        for (VersionData<ConfigMeta> file: groupFiles) {
            changedList.add(new Changed(file.getData(), file.getVersion()));
        }

        doReturn(changedList, resp);
    }

}
