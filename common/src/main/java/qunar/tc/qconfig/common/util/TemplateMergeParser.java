package qunar.tc.qconfig.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaohui.yu
 * 4/22/15
 */
public class TemplateMergeParser {
    private final Map<String, String> parameters;

    public TemplateMergeParser(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Result mergeParse(String content) {
        if (parameters == null) return new Result(content);

        StringBuilder result = new StringBuilder(content.length());
        int i = 0;
        Map<Param, String> parameters = new HashMap<Param, String>();
        while (true) {
            if (i >= content.length()) break;
            char token = content.charAt(i++);
            if (token == '$') {
                Param param = readParam(content, i);
                String value = readValue(param);
                //没有读取到合法参数，回退
                if (value == null) {
                    result.append('$');
                } else {
                    parameters.put(param, value);
                    //skip to end of param
                    i = param.endIndex;
                    result.append(value);
                }
            } else {
                result.append(token);
            }
        }
        return new Result(result.toString(), parameters);
    }

    private String readValue(Param param) {
        if (param == Param.NONE) return null;
        String value = this.parameters.get(param.name);
        if (value == null) return param.defaultValue;
        return value;
    }

    /**
     * 读取模板中的参数
     *
     * @param content 模板内容
     * @param index   开始读取位置
     * @return 参数
     */
    private Param readParam(String content, int index) {
        if (index >= content.length()) return Param.NONE;
        char token = content.charAt(index);
        if (token != '{') return Param.NONE;
        //skip {
        ++index;

        if (index >= content.length()) return Param.NONE;

        int startIndex = index;
        //读取参数名
        String name = readName(content, index);
        if (name == null) return Param.NONE;
        index += name.length();
        //读取默认值
        String defaultValue = readDefaultValue(content, index);

        int endIndex = computeEndIndex(startIndex, name, defaultValue);
        return new Param(name, defaultValue, endIndex);
    }

    private String readDefaultValue(String content, int index) {
        char token = content.charAt(index++);
        if (token == '}') return null;

        boolean begin = false;
        StringBuilder result = new StringBuilder(20);
        while (true) {
            if (index >= content.length()) return null;
            token = content.charAt(index++);
            if (isInvalidValue(token)) {
                return null;
            }
            if (token == '{') {
                begin = true;
            }
            if (token == '}') {
                if (begin) {
                    begin = false;
                } else {
                    return result.toString();
                }
            }
            result.append(token);
        }
    }

    private boolean isInvalidValue(char token) {
        return (token == '\n'
                || token == '\t'
                || token == '\r');
    }

    private String readName(String content, int index) {
        StringBuilder result = new StringBuilder(20);
        while (true) {
            if (index >= content.length()) return null;
            char token = content.charAt(index++);
            if (isInValidName(token)) return null;
            if (isEndName(token)) {
                return result.toString();
            }
            result.append(token);
        }
    }

    private boolean isInValidName(char token) {
        return token != '/'
                && token != '.'
                && token != '_'
                && token != '-'
                && token != ':'
                && token != '|'
                && token != '}'
                && !Character.isLetterOrDigit(token);
    }

    private boolean isEndName(char token) {
        return token == '|' || token == '}';
    }

    private int computeEndIndex(int startIndex, String name, String defaultValue) {
        int endIndex = startIndex + name.length();
        if (defaultValue != null && defaultValue.length() > 0) {
            // |
            endIndex += 1;
            endIndex += defaultValue.length();
        }
        // }
        endIndex += 1;
        return endIndex;
    }

    public static class Param {
        static final Param NONE = new Param("", "", 0);

        public final String name;

        public final String defaultValue;

        final int endIndex;

        private Param(String name, String defaultValue, int endIndex) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.endIndex = endIndex;
        }
    }

    public static class Result {
        public final String content;

        public final Map<Param, String> parameters;

        Result(String content) {
            this(content, null);
        }

        Result(String content, Map<Param, String> parameters) {
            this.content = content;
            this.parameters = parameters;
        }
    }
}
