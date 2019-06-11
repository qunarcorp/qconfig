package qunar.tc.qconfig.servercommon.service;

import org.springframework.stereotype.Component;
import qunar.tc.qconfig.servercommon.bean.AlarmType;

import java.util.Set;

/**
 * Created by pingyang.yang on 2019-05-13
 */
@Component
public class MockAlarmService implements IAlarmService {

    @Override
    public void sendMailAlarm(String var1, String var2, Set<String> var3) {

    }

    @Override
    public void sendRtxAlarm(String titleWithContent, Set<String> rtxId) {

    }
}
