package qunar.tc.qconfig.server.serverself.serverinfo;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * @author keli.wang
 */
public interface CustomEntryPointMappingService {
    boolean hasCustomMapping(final HttpServletRequest request);

    Set<String> getCustomEntryPoints(final HttpServletRequest request);
}
