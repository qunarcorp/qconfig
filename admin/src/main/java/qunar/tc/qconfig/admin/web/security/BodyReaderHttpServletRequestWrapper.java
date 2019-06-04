package qunar.tc.qconfig.admin.web.security;
import com.google.common.io.ByteStreams;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * Date: 14-5-22
 * Time: 上午11:15
 *
 * @author: xiao.liang
 * @description:
 */
public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public BodyReaderHttpServletRequestWrapper(final HttpServletRequest request)
            throws IOException {
        super(request);
        try (InputStream inputStream = request.getInputStream()) {
            body = ByteStreams.toByteArray(inputStream);
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }

}
