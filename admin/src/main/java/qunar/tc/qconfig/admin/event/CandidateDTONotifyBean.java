package qunar.tc.qconfig.admin.event;

import qunar.tc.qconfig.admin.dto.CandidateDTO;
import qunar.tc.qconfig.common.support.Application;

/**
 * Created by IntelliJ IDEA.
 * User: liuzz
 * Date: 14-5-26
 * Time: 下午2:26
 */
public class CandidateDTONotifyBean {

    public CandidateDTONotifyBean(ConfigOperationEvent event, String operator, String ip, CandidateDTO candidateDTO, String remarks, Application application) {
        this.event = event;
        this.operator = operator;
        this.candidateDTO = candidateDTO;
        this.remarks = remarks;
        this.ip = ip;
        this.application = application;
    }

    public CandidateDTONotifyBean copy() {
        return new CandidateDTONotifyBean(event, operator, ip, candidateDTO.copy(), remarks, application);
    }

    public final ConfigOperationEvent event;
    public final CandidateDTO candidateDTO;
    public final String remarks;
    public final String operator;
    public final String ip;
    public final Application application;

    @Override
    public String toString() {
        return "CandidateDTONotifyBean{" +
                "event=" + event +
                ", candidateDTO=" + candidateDTO +
                ", remarks='" + remarks + '\'' +
                ", operator='" + operator + '\'' +
                ", ip='" + ip + '\'' +
                ", application=" + application +
                '}';
    }
}
