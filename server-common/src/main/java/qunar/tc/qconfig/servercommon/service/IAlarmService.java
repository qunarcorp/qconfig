package qunar.tc.qconfig.servercommon.service;

import qunar.tc.qconfig.servercommon.bean.AlarmType;

import java.util.Set;

/**
 * Created by pingyang.yang on 2019-05-13
 */
public interface IAlarmService {

    void sendAlarm(String var1, String var2, Set<String> var3, Set<AlarmType> var4);

    void sendAllTypeAlarm(String var1, String var2, Set<String> var3);

    void sendRtxAlarm(String var1, Set<String> var2);

    void sendPhoneAlarm(String var1, Set<String> var2);

    void sendMailAlarm(String var1, String var2, Set<String> var3);
}

