package qunar.tc.qconfig.server.config.longpolling.impl;

import qunar.tc.qconfig.server.config.longpolling.Listener;
import qunar.tc.qconfig.server.domain.Changed;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author zhenyu.nie created on 2018 2018/4/11 18:26
 */
public class PushItem {

    private final Queue<Listener> listeners;
    private final String type;
    private final Changed change;

    public PushItem(List<Listener> listeners, String type, Changed change) {
        this.listeners = new LinkedList<>(listeners);
        this.type = type;
        this.change = change;
    }

    public Queue<Listener> getListeners() {
        return listeners;
    }

    public String getType() {
        return type;
    }

    public Changed getChange() {
        return change;
    }

    @Override
    public String toString() {
        return "PushItem{" +
                "type='" + type + '\'' +
                ", change=" + change +
                '}';
    }
}
