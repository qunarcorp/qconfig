package qunar.tc.qconfig.client.impl;

import qunar.tc.qconfig.client.Switcher;

/**
 * Created by zhaohui.yu
 * 1/29/18
 */
public class QConfigServerClientFactory {

    public static QConfigServerClient create() {
        return new QueueQConfigServerClient(new QConfigHttpServerClient());
    }
}
