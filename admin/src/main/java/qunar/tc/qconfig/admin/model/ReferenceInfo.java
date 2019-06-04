package qunar.tc.qconfig.admin.model;

import java.util.List;

/**
 * Date: 14-6-27
 * Time: 下午3:05
 *
 * @author: xiao.liang
 * @description:
 */
public class ReferenceInfo {

    private boolean edit;

    private int totalCount;

    private List<ConfigInfoWithoutPublicStatus> referenceDatas;

    public ReferenceInfo(boolean edit, int totalCount, List<ConfigInfoWithoutPublicStatus> referenceDatas) {
        this.edit = edit;
        this.totalCount = totalCount;
        this.referenceDatas = referenceDatas;
    }

    public boolean isEdit() {
        return edit;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<ConfigInfoWithoutPublicStatus> getReferenceDatas() {
        return referenceDatas;
    }
}
