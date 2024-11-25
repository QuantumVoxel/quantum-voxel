package dev.ultreon.quantum.client.gui.screens;

import dev.ultreon.quantum.client.ServerInfo;
import org.jetbrains.annotations.ApiStatus;

public class ServerEntry {
    public final ServerInfo info;

    @ApiStatus.Experimental
    public String motd = "";

    public ServerEntry(ServerInfo info) {
        this.info = info;
    }

    @ApiStatus.Experimental
    public ServerEntry(ServerInfo info, String motd) {
        this.info = info;
        this.motd = motd;
    }
}
