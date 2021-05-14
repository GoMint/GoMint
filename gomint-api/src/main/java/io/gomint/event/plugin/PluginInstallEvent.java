package io.gomint.event.plugin;

import io.gomint.plugin.Plugin;

/**
 * @author theminecoder
 * @version 1.0
 * @stability 3
 */
public class PluginInstallEvent extends PluginEvent {

    public PluginInstallEvent(Plugin plugin) {
        super(plugin);
    }

    @Override
    public String toString() {
        return "PluginInstallEvent{" +
            "plugin=" + this.plugin() +
            '}';
    }

}
