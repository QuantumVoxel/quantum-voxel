package dev.ultreon.quantum.api.events.player;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.text.TextObject;

public class PlayerAttemptJoinEvent extends PlayerEvent {
    private TextObject denied;

    public PlayerAttemptJoinEvent(Player entity) {
        super(entity);
    }

    public void setDenied(TextObject reason) {
        this.denied = reason;
    }

    public void setDenied(String reason) {
        setDenied(TextObject.literal(reason));
    }

    public void setDenied(boolean denied) {
        setDenied(denied ? TextObject.translation("quantum.multiplayer.auth.denied") : null);
    }

    public TextObject getDenyReason() {
        return denied;
    }

    public boolean isDenied() {
        return denied != null;
    }

    public boolean isAllowed() {
        return !isDenied();
    }
}
