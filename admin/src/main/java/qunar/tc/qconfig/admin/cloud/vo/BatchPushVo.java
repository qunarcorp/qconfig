package qunar.tc.qconfig.admin.cloud.vo;

import qunar.tc.qconfig.admin.dto.PushItemDto;

import java.util.List;

public class BatchPushVo {

    private List<PushSimpleVo> pushSimpleVos;

    private List<PushItemDto> pushItems;

    public List<PushSimpleVo> getPushSimpleVos() {
        return pushSimpleVos;
    }

    public void setPushSimpleVos(List<PushSimpleVo> pushSimpleVos) {
        this.pushSimpleVos = pushSimpleVos;
    }

    public List<PushItemDto> getPushItems() {
        return pushItems;
    }

    public void setPushItems(List<PushItemDto> pushItems) {
        this.pushItems = pushItems;
    }

    @Override
    public String toString() {
        return "BatchPushVo{" + "pushSimpleVos=" + pushSimpleVos + ", pushItems=" + pushItems + '}';
    }
}
