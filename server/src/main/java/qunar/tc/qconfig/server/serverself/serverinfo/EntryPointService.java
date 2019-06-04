package qunar.tc.qconfig.server.serverself.serverinfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author keli.wang
 */
public interface EntryPointService {

    List<String> getHttpEntryPoints(HttpServletRequest request);

    List<String> getHttpsEntryPoints(HttpServletRequest request);
}
