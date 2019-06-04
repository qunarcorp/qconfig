package qunar.tc.qconfig.admin.cloud.vo;

import com.sksamuel.diffpatch.DiffMatchPatch;
import qunar.tc.qconfig.admin.model.DiffCount;

import java.util.List;

public class DiffResultVo {

    private List<DiffMatchPatch.Diff> diff;

    private String diffText;

    private DiffCount diffCount;

    public DiffResultVo() {
    }

    public DiffResultVo(List<DiffMatchPatch.Diff> diff, String diffText, DiffCount diffCount) {
        this.diff = diff;
        this.diffText = diffText;
        this.diffCount = diffCount;
    }

    public List<DiffMatchPatch.Diff> getDiff() {
        return diff;
    }

    public void setDiff(List<DiffMatchPatch.Diff> diff) {
        this.diff = diff;
    }

    public String getDiffText() {
        return diffText;
    }

    public void setDiffText(String diffText) {
        this.diffText = diffText;
    }

    public DiffCount getDiffCount() {
        return diffCount;
    }

    public void setDiffCount(DiffCount diffCount) {
        this.diffCount = diffCount;
    }

    @Override
    public String toString() {
        return "DiffResuleVo{" +
                "diff=" + diff +
                ", diffText='" + diffText + '\'' +
                ", diffCount=" + diffCount +
                '}';
    }
}
