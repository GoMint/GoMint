package io.gomint.event.plugin;

import io.gomint.event.Event;
import io.gomint.plugin.Plugin;

/**
 * @author theminecoder
 * @version 1.0
 * @stability 3
 */
public class PluginEvent extends Event {

    private final Plugin plugin;

    public PluginEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin plugin() {
        return this.plugin;
    }

    @Override
    public String toString() {
        return "PluginEvent{" +
            "plugin=" + this.plugin +
            '}';
    }

}
