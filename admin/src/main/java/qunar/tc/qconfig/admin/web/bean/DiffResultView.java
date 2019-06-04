package qunar.tc.qconfig.admin.web.bean;

import qunar.tc.qconfig.admin.model.DiffCount;

/**
 * @author zhenyu.nie created on 2016 2016/9/2 18:02
 */
public class DiffResultView {

    private String result;

    private String uniDiffResult;

    private String diffResultText;

    private DiffCount diffCount;

    public DiffResultView(String result, String diffResultText, DiffCount diffCount) {
        this(result, null, diffResultText, diffCount);
    }

    public DiffResultView(String result, String uniDiffResult, String diffResultText, DiffCount diffCount) {
        this.result = result;
        this.uniDiffResult = uniDiffResult;
        this.diffResultText = diffResultText;
        this.diffCount = diffCount;
    }

    public String getResult() {
        return result;
    }

    public String getUniDiffResult() {
        return uniDiffResult;
    }

    public String getDiffResultText() {
        return diffResultText;
    }

    public DiffCount getDiffCount() {
        return diffCount;
    }

    @Override
    public String toString() {
        return "DiffResultView{" +
                "result='" + result + '\'' +
                ", uniDiffResult='" + uniDiffResult + '\'' +
                ", diffResultText='" + diffResultText + '\'' +
                ", diffCount=" + diffCount +
                '}';
    }
}
