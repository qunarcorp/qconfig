package qunar.tc.qconfig.servercommon.service;

import qunar.tc.qconfig.servercommon.bean.AlarmType;

import java.util.Set;

/**
 * Created by pingyang.yang on 2019-05-13
 */
public interface IAlarmService {

    void sendMailAlarm(String title, String content, Set<String> names);

    void sendRtxAlarm(String titleWithContent, Set<String> rtxId);
}

