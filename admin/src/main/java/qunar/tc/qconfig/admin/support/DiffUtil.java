package qunar.tc.qconfig.admin.support;

import com.sksamuel.diffpatch.DiffMatchPatch;
import qunar.tc.qconfig.admin.cloud.vo.DiffResultVo;
import qunar.tc.qconfig.admin.model.DiffCount;
import qunar.tc.qconfig.admin.model.DiffResult;
import qunar.tc.qconfig.admin.web.bean.DiffResultView;
import qunar.tc.qconfig.common.util.FileChecker;

import java.util.List;

/**
 * @author zhenyu.nie created on 2015 2015/7/7 11:08
 */
public class DiffUtil {

    public static String diffText(String filename, DiffCount diffCount, boolean isTemplateFile) {
        if (isTemplateFile || FileChecker.isPropertiesFile(filename)) {
            return "增加" + diffCount.getAdd() + "处，删除" + diffCount.getDelete() + "处，修改" + diffCount.getModify() + "处";
        } else {
            return "增加" + diffCount.getAdd() + "处，删除" + diffCount.getDelete() + "处";
        }
    }

    public static DiffResultView wrapDiffVo(String name, DiffResult<String> value) {
        if (value == null) {
            return null;
        }
        return new DiffResultView(value.getResult(), "文件变更：" + DiffUtil.diffText(name, value.getDiffCount(), FileChecker.isTemplateFile(name)), value.getDiffCount());
    }

    public static DiffResultView wrapDiffVo(String name, Differ.MixedDiffResult<String, String> diffResult) {
        if (diffResult == null) {
            return null;
        }
        boolean isTemplateFile = FileChecker.isTemplateFile(name);
        DiffCount diffCount = isTemplateFile ? diffResult.getOldDiff().getDiffCount() : diffResult.getUniDiff().getDiffCount();
        String diffText = "文件变更：" + diffText(name, diffCount, isTemplateFile);
        return new DiffResultView(diffResult.getOldDiff().getResult(), diffResult.getUniDiff().getResult(), diffText, diffResult.getOldDiff().getDiffCount());
    }

    public static DiffResultVo generateDiffVo(String name, DiffResult<List<DiffMatchPatch.Diff>> value) {
        return new DiffResultVo(value.getResult(), diffText(name, value.getDiffCount(), FileChecker.isTemplateFile(name)), value.getDiffCount());
    }


    public enum DiffType {
        PLAIN,
        HTML
    }

}
