package qunar.tc.qconfig.server.config.apiversion;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.server.domain.CheckRequest;
import qunar.tc.qconfig.server.support.context.ClientInfoService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

/**
 * @author zhenyu.nie created on 2017 2017/3/29 18:04
 */
@Service("v2Parser")
public class RequestParserV2Impl implements RequestParser {

    private static final Logger logger = LoggerFactory.getLogger(RequestParserV2Impl.class);

    private static final Splitter SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

    @Resource
    private ClientInfoService clientInfoService;

    @Override
    public List<CheckRequest> parse(HttpServletRequest req) throws IOException {
        List<String> list;
        try (final InputStream inputStream = req.getInputStream()) {
            CharSource charSource = new CharSource() {
                @Override
                public Reader openStream() throws IOException {
                    return new InputStreamReader(inputStream, Constants.UTF_8);
                }
            };
            list = charSource.readLines();
        }

        String profile = clientInfoService.getProfile();
        List<CheckRequest> result = Lists.newArrayList();
        for (String aList : list) {
            Iterator<String> iterator = SPLITTER.split(aList).iterator();
            CheckRequest request = new CheckRequest();
            try {
                request.setGroup(iterator.next());
                request.setDataId(iterator.next());
                request.setVersion(Integer.valueOf(iterator.next()));
                request.setLoadProfile(iterator.next());
                request.setProfile(profile);
            } catch (Exception e) {
                logger.warn("iterator no next, line: {}", aList, e);
                return ImmutableList.of();
            }
            result.add(request);
        }
        return result;

    }
}
