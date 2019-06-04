package qunar.tc.qconfig.admin.model;

import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.common.util.PublicType;

/**
 * @author zhenyu.nie created on 2014 2014/10/15 14:15
 */
public class InterData {

    private boolean isPublic = false;

    private final CandidateDTO applyDto;

    private final ApplyResult applyResult;

    private final CandidateDTO approveDto;

    private final CandidateDTO publishDto;

    public InterData(CandidateDTO applyDto, ApplyResult applyResult, CandidateDTO approveDto, CandidateDTO publishDto) {
        this.applyDto = applyDto;
        this.applyResult = applyResult;
        this.approveDto = approveDto;
        this.publishDto = publishDto;
    }

    public CandidateDTO getApplyDto() {
        return applyDto;
    }

    public ApplyResult getApplyResult() {
        return applyResult;
    }

    public CandidateDTO getApproveDto() {
        return approveDto;
    }

    public CandidateDTO getPublishDto() {
        return publishDto;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public String toString() {
        return "InterData{" +
                "applyDto=" + applyDto +
                ", applyResult=" + applyResult +
                ", approveDto=" + approveDto +
                ", publishDto=" + publishDto +
                '}';
    }
}
