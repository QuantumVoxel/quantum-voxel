package dev.ultreon.quantum.client.gui.debug;

import com.badlogic.gdx.Gdx;
import dev.ultreon.quantum.network.client.ClientPacketHandler;
import dev.ultreon.quantum.network.server.ServerPacketHandler;
import dev.ultreon.quantum.network.system.IConnection;

public class SimpleDebugPage implements DebugPage {
    @Override
    public void render(DebugPageContext context) {
        var client = context.client();
        var player = client.player;

        IConnection<ClientPacketHandler, ServerPacketHandler> connection = client.connection;
        context.left("FPS", Gdx.graphics.getFramesPerSecond())
                .left("TPS", client.getCurrentTps())
                .left("Ping", (connection != null ? connection.getPing() : "N/A") + "ms");

        if (player != null) {
            context.left("Position", player.getBlockPos());
        }
    }
}
