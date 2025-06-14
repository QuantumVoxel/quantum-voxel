package dev.ultreon.quantum.entity.player;

import dev.ultreon.quantum.ubo.types.MapType;

public class PlayerAbilities {
    public boolean flying = false;
    public boolean allowFlight = false;
    public boolean instaMine = false;
    public boolean invincible = false;
    public boolean blockBreak = true;

    public void load(MapType data) {
        this.flying = data.getBoolean("flying");
        this.allowFlight = data.getBoolean("allowFlight");
    }

    public MapType save(MapType data) {
        data.putBoolean("flying", this.flying);
        data.putBoolean("allowFlight", this.allowFlight);
        return data;
    }
}
