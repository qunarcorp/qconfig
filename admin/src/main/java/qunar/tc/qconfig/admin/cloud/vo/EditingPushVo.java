package qunar.tc.qconfig.admin.cloud.vo;

import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.admin.dto.PushItemDto;

import java.util.List;

public class EditingPushVo {

    private CandidateDTO candidateDTO;

    private List<PushItemDto> pushItems;

    public CandidateDTO getCandidateDTO() {
        return candidateDTO;
    }

    public void setCandidateDTO(CandidateDTO candidateDTO) {
        this.candidateDTO = candidateDTO;
    }

    public List<PushItemDto> getPushItems() {
        return pushItems;
    }

    public void setPushItems(List<PushItemDto> pushItems) {
        this.pushItems = pushItems;
    }

    @Override
    public String toString() {
        return "EditingPushVo{" + "candidateDTO=" + candidateDTO + ", pushItems=" + pushItems + '}';
    }
}
