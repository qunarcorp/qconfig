package qunar.tc.qconfig.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.io.CharSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.exception.PropertiesConflictException;
import qunar.tc.qconfig.admin.service.ConfigEditorSettingsService;
import qunar.tc.qconfig.admin.service.PropertiesCheckService;
import qunar.tc.qconfig.client.Configuration;
import qunar.tc.qconfig.client.MapConfig;
import qunar.tc.qconfig.client.TypedConfig;
import qunar.tc.qconfig.common.util.FileChecker;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author keli.wang
 */
@Service
public class PropertiesCheckServiceImpl implements PropertiesCheckService {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesCheckServiceImpl.class);

    private volatile CheckControl checkControl;

    private volatile boolean check;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Resource
    private ConfigEditorSettingsService configEditorSettingsService;

    @PostConstruct
    public void init() {
        TypedConfig<String> config = TypedConfig.get("property-conflict-whitelist", TypedConfig.STRING_PARSER);
        config.current();
        config.addListener(conf -> {
            Set<String> apps = Sets.newHashSet();
            Set<File> files = Sets.newHashSet();
            try {
                List<String> lines = CharSource.wrap(conf).readLines();
                for (String line : lines) {
                    line = line.trim();
                    if (Strings.isNullOrEmpty(line)) {
                        continue;
                    }
                    int index = line.indexOf(":");
                    if (index < 0) {
                        apps.add(line);
                    } else {
                        String app = line.substring(0, index).trim();
                        String fileName = line.substring(index + 1).trim();
                        files.add(new File(app, fileName));
                    }
                }
                checkControl = new CheckControl(apps, files);
            } catch (IOException e) {
                logger.error("unexpect error", e);
                // never happen
            }
        });

        MapConfig mapConfig = MapConfig.get("config.properties");
        mapConfig.asMap();
        mapConfig.addListener(conf -> check = Boolean.parseBoolean(conf.get("admin.properties.conflict.check")));
    }

    @Override
    public void checkConflictProperty(String group, String dataId, String data) {
        if (!checkControl.needCheck(group, dataId)) {
            return;
        }

        try {
            mapper.readTree(data);
            return;// json file
        } catch (Exception e) {
            // ignore
        }

        try (StringReader reader = new StringReader(data)) {
            check(new LineReader(reader));
        } catch (IOException e) {
            logger.error("unexpect error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isRealPropertyFile(String group, String dataId, String data) {
        if (!checkControl.needCheckFileRegardlessGroupAndCheckFlag(group, dataId)) {
            return false;
        }

        return !isJsonFile(data);

    }

    private boolean isJsonFile(String data) {
        data = Strings.nullToEmpty(data).trim();
        if (data.startsWith("{") || data.startsWith("[")) {
            try {
                mapper.readTree(data);
                return true;
            } catch (Exception e) {
                // ignore
            }
        }
        return false;
    }

    private class CheckControl {
        private final Set<String> whiteApps;
        private final Set<File> whiteFiles;

        public CheckControl(Set<String> whiteApps, Set<File> whiteFiles) {
            this.whiteApps = whiteApps;
            this.whiteFiles = whiteFiles;
        }

        boolean needCheckFileRegardlessGroupAndCheckFlag(String group, String dataId) {
            if (!FileChecker.isPropertiesFile(dataId)) {
                return false;
            }

            return !whiteFiles.contains(new File(group, dataId));

        }

        boolean needCheck(String group, String dataId) {
            if (!FileChecker.isPropertiesFile(dataId)) {
                return false;
            }

            if (!check) {
                return false;
            }

            if (whiteApps.contains(group)) {
                return false;
            }

            if (whiteFiles.contains(new File(group, dataId))) {
                return false;
            }

            return configEditorSettingsService.settingsOf(group, dataId).isUseAdvancedEditor();
        }
    }


    private static class File {
        private final String group;
        private final String dataId;

        public File(String group, String dataId) {
            this.group = group;
            this.dataId = dataId;
        }

        public String getGroup() {
            return group;
        }

        public String getDataId() {
            return dataId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof File)) return false;

            File file = (File) o;

            if (!Objects.equals(group, file.group)) return false;
            return Objects.equals(dataId, file.dataId);

        }

        @Override
        public int hashCode() {
            int result = group != null ? group.hashCode() : 0;
            result = 31 * result + (dataId != null ? dataId.hashCode() : 0);
            return result;
        }
    }

    // from java.util.Properties
    private static void check(LineReader reader) throws IOException {
        Set<String> keys = Sets.newHashSet();

        char[] convtBuf = new char[1024];
        int limit;
        int keyLen;
        int valueStart;
        char c;
        boolean hasSep;
        boolean precedingBackslash;

        while ((limit = reader.readLine()) >= 0) {
            c = 0;
            keyLen = 0;
            valueStart = limit;
            hasSep = false;

            //System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
            precedingBackslash = false;
            while (keyLen < limit) {
                c = reader.lineBuf[keyLen];
                //need check if escaped.
                if ((c == '=' ||  c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                } else if ((c == ' ' || c == '\t' ||  c == '\f') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < limit) {
                c = reader.lineBuf[valueStart];
                if (c != ' ' && c != '\t' &&  c != '\f') {
                    if (!hasSep && (c == '=' ||  c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            String key = loadConvert(reader.lineBuf, 0, keyLen, convtBuf);

            boolean add = keys.add(key);
            if (!add) {
                throw new PropertiesConflictException(key);
            }
        }
    }

    private static class LineReader {

        public LineReader(InputStream inStream) {
            this.inStream = inStream;
            inByteBuf = new byte[8192];
        }

        public LineReader(Reader reader) {
            this.reader = reader;
            inCharBuf = new char[8192];
        }

        byte[] inByteBuf;
        char[] inCharBuf;
        char[] lineBuf = new char[1024];
        int inLimit = 0;
        int inOff = 0;
        InputStream inStream;
        Reader reader;

        int readLine() throws IOException {
            int len = 0;
            char c = 0;

            boolean skipWhiteSpace = true;
            boolean isCommentLine = false;
            boolean isNewLine = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
            boolean skipLF = false;

            while (true) {
                if (inOff >= inLimit) {
                    inLimit = (inStream==null)?reader.read(inCharBuf)
                            :inStream.read(inByteBuf);
                    inOff = 0;
                    if (inLimit <= 0) {
                        if (len == 0 || isCommentLine) {
                            return -1;
                        }
                        if (precedingBackslash) {
                            len--;
                        }
                        return len;
                    }
                }
                if (inStream != null) {
                    //The line below is equivalent to calling a
                    //ISO8859-1 decoder.
                    c = (char) (0xff & inByteBuf[inOff++]);
                } else {
                    c = inCharBuf[inOff++];
                }
                if (skipLF) {
                    skipLF = false;
                    if (c == '\n') {
                        continue;
                    }
                }
                if (skipWhiteSpace) {
                    if (c == ' ' || c == '\t' || c == '\f') {
                        continue;
                    }
                    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                        continue;
                    }
                    skipWhiteSpace = false;
                    appendedLineBegin = false;
                }
                if (isNewLine) {
                    isNewLine = false;
                    if (c == '#' || c == '!') {
                        isCommentLine = true;
                        continue;
                    }
                }

                if (c != '\n' && c != '\r') {
                    lineBuf[len++] = c;
                    if (len == lineBuf.length) {
                        int newLength = lineBuf.length * 2;
                        if (newLength < 0) {
                            newLength = Integer.MAX_VALUE;
                        }
                        char[] buf = new char[newLength];
                        System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
                        lineBuf = buf;
                    }
                    //flip the preceding backslash flag
                    if (c == '\\') {
                        precedingBackslash = !precedingBackslash;
                    } else {
                        precedingBackslash = false;
                    }
                }
                else {
                    // reached EOL
                    if (isCommentLine || len == 0) {
                        isCommentLine = false;
                        isNewLine = true;
                        skipWhiteSpace = true;
                        len = 0;
                        continue;
                    }
                    if (inOff >= inLimit) {
                        inLimit = (inStream==null)
                                ?reader.read(inCharBuf)
                                :inStream.read(inByteBuf);
                        inOff = 0;
                        if (inLimit <= 0) {
                            if (precedingBackslash) {
                                len--;
                            }
                            return len;
                        }
                    }
                    if (precedingBackslash) {
                        len -= 1;
                        //skip the leading whitespace characters in following line
                        skipWhiteSpace = true;
                        appendedLineBegin = true;
                        precedingBackslash = false;
                        if (c == '\r') {
                            skipLF = true;
                        }
                    } else {
                        return len;
                    }
                }
            }
        }
    }

    private static String loadConvert (char[] in, int off, int len, char[] convtBuf) {
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convtBuf = new char[newLen];
        }
        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if(aChar == 'u') {
                    // Read the xxxx
                    int value=0;
                    for (int i=0; i<4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                            case '0': case '1': case '2': case '3': case '4':
                            case '5': case '6': case '7': case '8': case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a': case 'b': case 'c':
                            case 'd': case 'e': case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A': case 'B': case 'C':
                            case 'D': case 'E': case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed \\uxxxx encoding.");
                        }
                    }
                    out[outLen++] = (char)value;
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = aChar;
            }
        }
        return new String (out, 0, outLen);
    }
}
