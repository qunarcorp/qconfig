package qunar.tc.qconfig.client.impl;

import qunar.tc.qconfig.client.Switcher;

class MockSwitcher implements Switcher {
    @Override
    public boolean status() {
        return true;
    }
}
