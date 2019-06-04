package qunar.tc.qconfig.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;

public final class Strings {

    public static final String lineSeparator;
    private static final int[] dateCodex = {2, 3, 5, 6, 8, 9, 19, 11, 12, 14, 15, 17, 18};
    private static final AtomicInteger messageOrder = new AtomicInteger(0);

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static {
        // 获取操作系统的换行符,类似org.apache.commons.io.IOUtils中的做法
        StringWriter w = new StringWriter(4);
        (new PrintWriter(w)).println();
        lineSeparator = w.toString();
    }

    /**
     * 通常情况下请不要使用构造方法创建这个类的实例.
     */
    public Strings() {
    }

    /**
     * 按顺序判断Object是否为空,如果非空，则调用其toString方法,如果toString方法返回为非null且非空的字符串，则返回该字符串
     * 如果所有参数都为null或者其 toString方法返回null或空字符串,则判断最后一个参数是否是null,如果是,返回null,否则返回其toString方法的返回结果
     *
     * @param o    确保用户调用时至少传入一个参数
     * @param args 对象序列
     * @return
     */
    public static String getString(Object o, Object... args) {

        String str = o == null ? null : o.toString();

        if (args == null || args.length == 0 || (str != null && !str.isEmpty()))
            return str;

        for (int i = 0; i < args.length; i++) {
            str = args[i] == null ? null : args[i].toString();
            if (str != null && !str.isEmpty())
                return str;
        }

        return str;
    }

    /**
     * 判断参数是否为null或空字符串
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     * @since 2.0
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将数组用指定符号连接成字符串
     *
     * @param arr       输入的数组
     * @param separator 连接符
     * @return 链接好的字符串
     */
    public static String arrayJoin(Object[] arr, String separator) {
        if (arr == null)
            return null;

        if (arr.length == 0)
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            sb.append(separator);
            if (arr[i] != null)
                sb.append(arr[i]);
        }
        return sb.toString();
    }

    public static String convertQuote(String scriptValue) {
        return convertQuote(scriptValue, '\'');
    }

    /**
     * 将文本中的特殊字符转换成脚本变量.
     * 如 :
     * <pre>
     * 	您要查询的城市:
     * 		'北京'
     * 	转换后为 -> (您要查询的城市:\r\n\'北京\')
     * </pre>
     *
     * @param scriptValue 文本或脚本变量
     * @param quote       单引号或双引号
     * @return content
     */
    public static String convertQuote(String scriptValue, char quote) {
        if (scriptValue == null)
            return null;

        StringBuilder builder = new StringBuilder(scriptValue.length());
        for (int i = 0; i < scriptValue.length(); i++) {
            char c = scriptValue.charAt(i);
            switch (c) {

                case '\\':
                    builder.append("\\\\");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '"':
                case '\'':
                    if (quote == c)
                        builder.append('\\');
                default:
                    builder.append(c);
            }
        }
        return (builder.toString());
    }

    /**
     * 将Sql92语句 中 单引号(') 转换为两个 ''
     *
     * @param sqlValue 输入到sql中的字符串
     * @return 转换后安全的字符变量
     */
    public static String convertSQL(String sqlValue) {

        StringBuilder builder = new StringBuilder(sqlValue.length());

        for (int i = 0; i < sqlValue.length(); i++) {
            char c = sqlValue.charAt(i);
            if (c == '\'') {
                builder.append('\'').append('\'');
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    /**
     * 将文本中的特殊标签转化成 html
     * <pre>
     *  '&lt;' -- '&amp;lt;'
     *  '&gt;' -- '&amp;gt;'
     *  '&quot;' -- '&amp;quot;'
     *  '&amp;' -- '&amp;amp;'
     * </pre>
     *
     * @param text
     * @return
     */
    public static String convertXML(String text) {
        if (text == null)
            return null;

        StringBuilder builder = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\'':
                    builder.append("&apos;");
                    break;
                case '&':
                    builder.append("&amp;");
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * 将文本中的space符号(空格,换行,制表符)转换成 html
     *
     * @param text content
     * @return html content
     */
    public static String convertHTMLSpace(String text) {
        if (text == null) return null;

        text = text.replaceAll("  ", " &nbsp;");
        StringBuilder builder = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\r':
                    break;
                case '\n':
                    builder.append("<br />");
                    break;
                case '\t':
                    builder.append(" &nbsp; &nbsp;");
                    break;
                default:
                    builder.append(c);
            }
        }

        return builder.toString();
    }

    /**
     * 把文字转换为 boolean 型, 当str 为 true,yes,on,1 时返回 true 否则返回false . 当str为空时返回def
     *
     * @param str boolean string
     * @param def default value
     * @return
     */
    public static boolean getBoolean(String str, boolean def) {

        if (isEmpty(str)) {
            return def;
        }
        str = str.trim().toUpperCase();
        return "TRUE".equals(str) || "YES".equals(str) || "ON".equals(str) || "1".equals(str);
    }

    /**
     * 将文字以 UTF-8 编码转换为 URL Encode,如果不能转码,则返回原始字符串
     *
     * @param text
     * @return
     */
    public static String encodeURL(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text;
        }
    }

    public static String subleft(String text, char c) {
        if (text == null || text.isEmpty())
            return text;
        int index = text.indexOf(c);
        if (index == -1)
            return text;
        return text.substring(0, index);
    }

    public static String subright(String text, char c) {
        if (text == null || text.isEmpty())
            return text;
        int index = text.lastIndexOf(c);
        if (index == -1)
            return text;
        return text.substring(index + 1);
    }

    public static String encodeHex(byte[] data) {

        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }

        return new String(out);
    }

    /**
     * 计算两个字串的相似度
     *
     * @return
     */
    public static int distance(CharSequence str1, CharSequence str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        int[][] distance = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++)
            distance[i][0] = i;
        for (int j = 0; j <= len2; j++)
            distance[0][j] = j;

        for (int i = 1; i <= len1; i++) {
            int v = len1 + 1 - i;
            for (int j = 1; j <= len2; j++)
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? -v : v));
        }

        return distance[len1][len2];
    }

    private static int minimum(int a, int b, int c) {
        int x = (a <= b) ? a : b;
        return (x <= c) ? x : c;
    }

}
