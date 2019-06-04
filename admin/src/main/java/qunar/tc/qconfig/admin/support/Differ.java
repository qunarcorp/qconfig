package qunar.tc.qconfig.admin.support;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.sksamuel.diffpatch.DiffMatchPatch;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import qunar.tc.qconfig.admin.dao.EncryptKeyDao;
import qunar.tc.qconfig.admin.model.DiffCount;
import qunar.tc.qconfig.admin.model.DiffResult;
import qunar.tc.qconfig.admin.model.EncryptKey;
import qunar.tc.qconfig.admin.service.EncryptKeyService;
import qunar.tc.qconfig.client.TableConfig;
import qunar.tc.qconfig.common.util.Constants;
import qunar.tc.qconfig.common.util.FileChecker;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Date: 14-6-9
 * Time: 下午6:55
 *
 * @author: xiao.liang
 * @description:
 */
@Service
public class Differ {

    private final static Logger logger = LoggerFactory.getLogger(Differ.class);

    private final static Splitter LINE_SPLITTER = Splitter.on(Pattern.compile("\r?\n"));

    private final static Joiner JOINER = Joiner.on("\n");

    @Resource
    private EncryptKeyDao encryptKeyDao;

    @Resource
    private EncryptKeyService encryptKeyService;

    public DiffResult<List<DiffMatchPatch.Diff>> diffWithEncrypt(String text1, String text2, String group, String filename) {
        return diff(text1, text2, group, filename, true);
    }

    public DiffResult<List<DiffMatchPatch.Diff>> diff(String text1, String text2, String filename) {
        return diff(text1, text2, null, filename, false);
    }

    private DiffResult<List<DiffMatchPatch.Diff>> diff(String text1, String text2, String group, String filename, boolean needEncrypt) {
        if (FileChecker.isTemplateFile(filename)) {
            try {
                return diffTemplateFile(text1, text2, group, filename, needEncrypt);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (FileChecker.isPropertiesFile(filename)) {
            try {
                return diffProperties(text1, text2);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
        LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diff_main(text1, text2, false);
        diffMatchPatch.diff_cleanupSemantic(diffs);

        int added = 0;
        int deleted = 0;
        for (DiffMatchPatch.Diff diff : diffs) {
            if (diff.operation == DiffMatchPatch.Operation.INSERT) {
                added++;
            } else if (diff.operation == DiffMatchPatch.Operation.DELETE) {
                deleted++;
            }
        }
        return new DiffResult<List<DiffMatchPatch.Diff>>(new DiffCount(added, deleted), diffs);
    }

    public DiffResult<String> uniDiff(String lText, String rText, String filename) {
        if (FileChecker.isPropertiesFile(filename)) {
            return getPropertiesUniDiff(lText, rText, filename);
        } else {
            return getFileUniDiff(lText, rText, filename);
        }
    }

    private DiffResult<String> getFileUniDiff(String lText, String rText, String filename) {
        List<String> lines1 = Strings.isNullOrEmpty(lText) ? ImmutableList.<String>of() : LINE_SPLITTER.splitToList(lText);
        List<String> lines2 = Strings.isNullOrEmpty(rText) ? ImmutableList.<String>of() : LINE_SPLITTER.splitToList(rText);
        Patch<String> diff = DiffUtils.diff(lines1, lines2);
        int insert=0, delete = 0, change = 0;
        for (Delta delta : diff.getDeltas()) {
            switch (delta.getType()) {
                case INSERT:
                    insert++;
                    break;
                case DELETE:
                    delete++;
                    break;
                case CHANGE:
                    change++;
                    break;
            }
        }
        //tole contextSize
        List<String> diffLines = DiffUtils.generateUnifiedDiff(filename, filename, lines1, diff, 2);
        String uniDiffText = JOINER.join(diffLines);
        return new DiffResult<>(new DiffCount(insert, delete, change), uniDiffText);
    }

    private DiffResult<String> getPropertiesUniDiff(String lText, String rText, String filename) {
        DiffResult<List<PropertyDiffDto>> propertiesDiffs = getPropertiesDiff(lText, rText);
        if (!propertiesDiffs.getDiffCount().hasDiff()) {
            return new DiffResult<>(propertiesDiffs.getDiffCount(), "");
        }
        Map<String, Integer> lKeyLineMap = getKeyLineMapping(lText);
        Map<String, Integer> rKeyLineMap = getKeyLineMapping(rText);
        StringBuilder sb = new StringBuilder();
        sb.append("--- ").append(filename).append("\n+++ ").append(filename).append("\n");
        for (PropertyDiffDto diff : propertiesDiffs.getResult()) {
            int lLineNum = lKeyLineMap.get(diff.getKey()) == null ? 0 : lKeyLineMap.get(diff.getKey());
            int rLineNum = rKeyLineMap.get(diff.getKey()) == null ? 0 : rKeyLineMap.get(diff.getKey());
            switch(diff.getType()) {
                case ADDED:
                    sb.append("@@ -0,0 +").append(rLineNum).append(",1 @@\n+")
                            .append(diff.getKey()).append("=").append(diff.getrValue()).append("\n");
                    break;

                case DELETED:
                    sb.append("@@ -").append(lLineNum).append(",1 +0,0 @@\n-")
                            .append(diff.getKey()).append("=").append(diff.getlValue()).append("\n");
                    break;
                case MODIFIED:
                    sb.append("@@ -").append(lLineNum).append(",1 +").append(rLineNum).append(",1 @@\n-")
                            .append(diff.getKey()).append("=").append(diff.getlValue()).append("\n+")
                            .append(diff.getKey()).append("=").append(diff.getrValue()).append("\n");
                    break;
            }

        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return new DiffResult<>(propertiesDiffs.getDiffCount(), sb.toString());
    }

    private Map<String, Integer> getKeyLineMapping(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return ImmutableMap.of();
        }
        List<String> lines = LINE_SPLITTER.splitToList(input);
        Map<String, Integer> keyLineMapping = Maps.newHashMapWithExpectedSize(lines.size());
        for (int n = 0; n < lines.size(); ++n) {
            String line = lines.get(n);
            if (parseComment(line) != null) {
                continue;
            }
            String key = parseKey(line);
            if (key != null) {
                keyLineMapping.put(key, n + 1);
            }
        }
        return keyLineMapping;
    }

    public Map<String, String> getKeyCommentMapping(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return ImmutableMap.of();
        }
        List<String> lines = LINE_SPLITTER.splitToList(input);
        Map<String, String> keyCommentMapping = Maps.newHashMapWithExpectedSize(lines.size());
        String lastLineComment = null;
        for (int n = 0; n < lines.size(); ++n) {
            String line = lines.get(n);
            String comment = parseComment(line);
            if (comment == null) {
                String key = parseKey(line);
                if (key != null && lastLineComment != null) {
                    keyCommentMapping.put(key, lastLineComment);
                }
            }
            lastLineComment = comment;
        }
        return keyCommentMapping;
    }

    private String parseKey(String line) {
        int idx = line.indexOf('=');
        if (idx <= 0) {
            return null;
        }
        String key = line.substring(0, idx).trim();
        return Strings.emptyToNull(key);
    }

    private String parseComment(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("#")) {
            return trimmed.substring(1);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private DiffResult<List<DiffMatchPatch.Diff>> diffTemplateFile(String lhs, String rhs, String group, String dataId, boolean needEncrypt) throws IOException {
        List<EncryptKey> encryptKeys;
        if (needEncrypt) {
            encryptKeys = encryptKeyDao.select(group, dataId);
        } else {
            encryptKeys = ImmutableList.of();
        }

        if (Strings.isNullOrEmpty(lhs)) {
            lhs = "[]";
        }
        if (Strings.isNullOrEmpty(rhs)) {
            rhs = "[]";
        }

        Table<String, String, String> lhsNode = TableConfig.TRIM_PARSER.parse(lhs);
        Table<String, String, String> rhsNode = TableConfig.TRIM_PARSER.parse(rhs);
        Sets.SetView<String> rowsDifference = Sets.difference(lhsNode.rowKeySet(), rhsNode.rowKeySet());
        Sets.SetView<String> rowsReverseDifference = Sets.difference(rhsNode.rowKeySet(), lhsNode.rowKeySet());
        DiffResult<List<DiffMatchPatch.Diff>> deleteDiffs = calculateDiffInfo(lhsNode, rowsDifference, DiffMatchPatch.Operation.DELETE, needEncrypt, encryptKeys);
        DiffResult<List<DiffMatchPatch.Diff>> insertDiffs = calculateDiffInfo(rhsNode, rowsReverseDifference, DiffMatchPatch.Operation.INSERT, needEncrypt, encryptKeys);

        Sets.SetView<String> rowIntersections = Sets.intersection(lhsNode.rowKeySet(), rhsNode.rowKeySet());
        DiffResult<List<DiffMatchPatch.Diff>> intersectionDiffs = calculateDiffInfo(lhsNode, rhsNode, rowIntersections, needEncrypt, encryptKeys);
        return add(deleteDiffs, insertDiffs, intersectionDiffs);
    }

    private DiffResult<List<DiffMatchPatch.Diff>> calculateDiffInfo(Table<String, String, String> lhsNode, Table<String, String, String> rhsNode, Set<String> rowIntersections, boolean needEncrypt, List<EncryptKey> encryptKeys) {
        DiffResult<List<DiffMatchPatch.Diff>> result = new DiffResult<List<DiffMatchPatch.Diff>>(new DiffCount(0, 0), ImmutableList.<DiffMatchPatch.Diff>of());
        for (String row : rowIntersections) {
            DiffResult<List<DiffMatchPatch.Diff>> listDiffResult = diffProperties(lhsNode.row(row), rhsNode.row(row), needEncrypt, encryptKeys, row);
            result = new DiffResult<List<DiffMatchPatch.Diff>>(result.getDiffCount().add(listDiffResult.getDiffCount()), concat(result.getResult(), listDiffResult.getResult()));
        }
        return result;
    }

    private static <T> List<T> concat(List<T> lhs, List<T> rhs) {
        List<T> list = Lists.newArrayListWithCapacity(lhs.size() + rhs.size());
        for (T t : lhs) {
            list.add(t);
        }
        for (T t : rhs) {
            list.add(t);
        }
        return list;
    }

    private DiffResult<List<DiffMatchPatch.Diff>> add(DiffResult<List<DiffMatchPatch.Diff>>... diffs) {
        DiffResult<List<DiffMatchPatch.Diff>> result = new DiffResult<List<DiffMatchPatch.Diff>>(new DiffCount(0, 0), ImmutableList.<DiffMatchPatch.Diff>of());
        for (DiffResult<List<DiffMatchPatch.Diff>> diff : diffs) {
            result = new DiffResult<>(result.getDiffCount().add(diff.getDiffCount()), concat(result.getResult(), diff.getResult()));
        }
        return result;
    }

    private DiffResult<List<DiffMatchPatch.Diff>> calculateDiffInfo(Table<String, String, String> node, Set<String> rowsDifference, DiffMatchPatch.Operation operation, boolean needEncrypt, List<EncryptKey> encryptKeys) {
        List<DiffMatchPatch.Diff> diffs = Lists.newArrayList();
        for (String rowDifference : rowsDifference) {
            Map<String, String> row = node.row(rowDifference);
            for (Map.Entry<String, String> column : row.entrySet()) {
                String key = column.getKey();
                String prefix;
                if (rowDifference.isEmpty()) {
                    prefix = key;
                } else {
                    prefix = rowDifference + SEPARATOR + key;
                }

                String value = column.getValue();
                if (needEncrypt && encryptKeyService.isEncryptedKey(encryptKeys, key)) {
                    value = "加密数据";
                }
                diffs.add(new DiffMatchPatch.Diff(operation, prefix + "=" + value + "\n"));
            }
        }
        DiffCount count = new DiffCount(0, 0);
        switch (operation) {
            case INSERT:
                count = new DiffCount(diffs.size(), 0);
                break;
            case DELETE:
                count = new DiffCount(0, diffs.size());
        }
        return new DiffResult<>(count, diffs);
    }

    private static final String SEPARATOR = Constants.ROW_COLUMN_SEPARATOR;

    // 对properties文件，不按文本内容匹配，按key-value匹配
    private DiffResult<List<DiffMatchPatch.Diff>> diffProperties(String lhs, String rhs) throws IOException {
        Properties lp = new Properties();
        lp.load(new StringReader(lhs));
        Properties rp = new Properties();
        rp.load(new StringReader(rhs));
        Map<String, String> lMap = toStringMap(lp);
        Map<String, String> rMap = toStringMap(rp);

        return diffProperties(lMap, rMap, false, ImmutableList.<EncryptKey>of());
    }

    private Map<String, String> toStringMap(Properties p) {
        Map<String, String> map = Maps.newHashMapWithExpectedSize(p.size());
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
        return map;
    }

    private DiffResult<List<DiffMatchPatch.Diff>> diffProperties(Map<String, String> lhs, Map<String, String> rhs, boolean needEncrypt, List<EncryptKey> encryptKeys) {
        return diffProperties(lhs, rhs, needEncrypt, encryptKeys, "");
    }

    private DiffResult<List<DiffMatchPatch.Diff>> diffProperties(Map<String, String> lhs, Map<String, String> rhs, boolean needEncrypt, List<EncryptKey> encryptKeys, String row) {
        MapDifference<String, String> mapDiffs = Maps.difference(lhs, rhs);
        Map<String, String> added = mapDiffs.entriesOnlyOnRight();
        Map<String, String> deleted = mapDiffs.entriesOnlyOnLeft();
        Map<String, MapDifference.ValueDifference<String>> modified = mapDiffs.entriesDiffering();
        List<DiffMatchPatch.Diff> diffs = Lists.newArrayListWithExpectedSize(mapDiffs.entriesInCommon().size() + deleted.size() + added.size() + modified.size() * 3);
        dealDiffs(needEncrypt, encryptKeys, row, diffs, DiffMatchPatch.Operation.INSERT, added.entrySet());
        dealDiffs(needEncrypt, encryptKeys, row, diffs, DiffMatchPatch.Operation.DELETE, deleted.entrySet());

        for (Map.Entry<String, MapDifference.ValueDifference<String>> entry : modified.entrySet()) {
            String key = entry.getKey();
            String leftValue = entry.getValue().leftValue();
            String rightValue = entry.getValue().rightValue();
            if (needEncrypt && (encryptKeyService.isEncryptedKey(encryptKeys, key) || encryptKeyService.isEncryptedKey(encryptKeys, row))) {
                leftValue = "加密数据";
                rightValue = "加密数据 (有修改)";
            }
            String prefix = key;
            if (!row.isEmpty()) {
                prefix = row + SEPARATOR + key;
            }
            diffs.add(new DiffMatchPatch.Diff(DiffMatchPatch.Operation.EQUAL, prefix + "="));
            diffs.add(new DiffMatchPatch.Diff(DiffMatchPatch.Operation.DELETE, leftValue));
            diffs.add(new DiffMatchPatch.Diff(DiffMatchPatch.Operation.INSERT, rightValue + "\n"));
        }

        dealDiffs(needEncrypt, encryptKeys, row, diffs, DiffMatchPatch.Operation.EQUAL, mapDiffs.entriesInCommon().entrySet());
        DiffCount count = new DiffCount(added.size(), deleted.size(), modified.size());
        return new DiffResult<>(count, diffs);
    }

    public DiffResult<List<PropertyDiffDto>> getPropertiesDiff(String lhs, String rhs) {
        MapDifference<String, String> mapDiffs = getMapDifference(lhs, rhs);
        return genDiffResultFromMapDifference(mapDiffs, false, ImmutableList.<EncryptKey>of());

    }

    public MapDifference<String, String> getMapDifference(String lhs, String rhs) {
        Map<String, String> lMap = toStringMap(readProperties(lhs));
        Map<String, String> rMap = toStringMap(readProperties(rhs));
        return Maps.difference(lMap, rMap);
    }

    private Properties readProperties(String text) {
        Properties p = new Properties();
        try {
            p.load(new StringReader(text));
        } catch (IOException e) {
            logger.error("load properties error, text{}", text, e);
            throw new RuntimeException("properties格式错误");
        }
        return p;
    }

    private DiffResult<List<PropertyDiffDto>> genDiffResultFromMapDifference(MapDifference<String, String> mapDiffs, boolean needEncrypt, List<EncryptKey> encryptKeys) {
        Map<String, String> added = mapDiffs.entriesOnlyOnRight();
        Map<String, String> deleted = mapDiffs.entriesOnlyOnLeft();
        Map<String, MapDifference.ValueDifference<String>> modified = mapDiffs.entriesDiffering();
        List<PropertyDiffDto> diffs = Lists.newArrayListWithExpectedSize(deleted.size() + added.size() + modified.size());
        for (Map.Entry<String, String> add : added.entrySet()) {
            if (needEncrypt && (encryptKeyService.isEncryptedKey(encryptKeys, add.getKey()))) {
                diffs.add(new PropertyDiffDto(DiffType.ADDED, add.getKey(), null, "加密数据"));
            } else {
                diffs.add(new PropertyDiffDto(DiffType.ADDED, add.getKey(), null, add.getValue()));
            }
        }
        for (Map.Entry<String, String> delete : deleted.entrySet()) {
            if (needEncrypt && (encryptKeyService.isEncryptedKey(encryptKeys, delete.getKey()))) {
                diffs.add(new PropertyDiffDto(DiffType.DELETED, delete.getKey(), "加密数据", null));
            } else {
                diffs.add(new PropertyDiffDto(DiffType.DELETED, delete.getKey(), delete.getValue(), null));
            }
        }
        for (Map.Entry<String, MapDifference.ValueDifference<String>> modify : modified.entrySet()) {
            if (needEncrypt && encryptKeyService.isEncryptedKey(encryptKeys, modify.getKey())) {
                diffs.add(new PropertyDiffDto(DiffType.MODIFIED, modify.getKey(), "加密数据", "加密数据 (有修改)"));
            } else {
                diffs.add(new PropertyDiffDto(DiffType.MODIFIED, modify.getKey(), modify.getValue().leftValue(), modify.getValue().rightValue()));
            }
        }
        DiffCount count = new DiffCount(added.size(), deleted.size(), modified.size());
        return new DiffResult<>(count, diffs);
    }



    private void dealDiffs(boolean needEncrypt, List<EncryptKey> encryptKeys, String row, List<DiffMatchPatch.Diff> diffs, DiffMatchPatch.Operation type, Set<Map.Entry<String, String>> entries) {
        for (Map.Entry<String, String> entry : entries) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (needEncrypt && (encryptKeyService.isEncryptedKey(encryptKeys, key) || encryptKeyService.isEncryptedKey(encryptKeys, row))) {
                value = "加密数据";
            }
            String prefix = key;
            if (!row.isEmpty()) {
                prefix = row + SEPARATOR + key;
            }
            diffs.add(new DiffMatchPatch.Diff(type, prefix + "=" + value + "\n"));
        }
    }

    public String toHtml(List<DiffMatchPatch.Diff> diffs) {
        StringBuilder html = new StringBuilder();
        for (DiffMatchPatch.Diff aDiff : diffs) {
            String text = aDiff.text.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\n", "<br/>").replace(" ", "&nbsp;");
            switch (aDiff.operation) {
                case INSERT:
                    html.append("<ins style=\"background:#63F541;\">").append(text)
                            .append("</ins>");
                    break;
                case DELETE:
                    html.append("<del style=\"background:#F85948;\">").append(text)
                            .append("</del>");
                    break;
                case EQUAL:
                    html.append("<span>").append(text).append("</span>");
                    break;
            }
        }
        return html.toString();
    }

    public DiffResult<String> diffToHtml(String lhs, String rhs, String filename) {
        DiffResult<List<DiffMatchPatch.Diff>> result = diff(lhs, rhs, filename);
        return wrap(result);
    }

    public DiffResult<String> diffToHtmlWithEncrypt(String lhs, String rhs, String group, String filename) {
        DiffResult<List<DiffMatchPatch.Diff>> result = diffWithEncrypt(lhs, rhs, group, filename);
        return wrap(result);
    }

    private DiffResult<String> wrap(DiffResult<List<DiffMatchPatch.Diff>> result) {
        String htmlFormatDiff = toHtml(result.getResult());
        return new DiffResult<String>(result.getDiffCount(), htmlFormatDiff);
    }

    public enum DiffType {
        ADDED, DELETED, MODIFIED
    }

    public class PropertyDiffDto {
        private DiffType type;
        private String key;
        private String lValue;
        private String rValue;

        public PropertyDiffDto(DiffType type, String key, String lValue, String rValue) {
            this.type = type;
            this.key = key;
            this.lValue = lValue;
            this.rValue = rValue;
        }

        public DiffType getType() {
            return type;
        }

        public String getKey() {
            return key;
        }

        public String getlValue() {
            return lValue;
        }

        public String getrValue() {
            return rValue;
        }
    }

    public static class MixedDiffResult<T1, T2> {
        DiffResult<T1> oldDiff;
        DiffResult<T2> uniDiff;

        public MixedDiffResult(DiffResult<T1> oldDiff, DiffResult<T2> uniDiff) {
            this.oldDiff = oldDiff;
            this.uniDiff = uniDiff;
        }

        public DiffResult<T1> getOldDiff() {
            return oldDiff;
        }

        public DiffResult<T2> getUniDiff() {
            return uniDiff;
        }
    }

}
