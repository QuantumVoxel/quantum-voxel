package dev.ultreon.quantum.util;

import dev.ultreon.quantum.entity.player.PlayerAbilities;

public enum GameMode {
    SURVIVAL(false, false, false, true),
    ADVENTUROUS(false, false, false, false),
    BUILDER(true, true, true, true),
    BUILDER_PLUS(true, true, true, true),
    SPECTATOR(true, false, true, false);

    private final boolean allowFlight;
    private final boolean instaMine;
    private final boolean invincible;
    private final boolean blockBreak;

    GameMode(boolean allowFlight, boolean instaMine, boolean invincible, boolean blockBreak) {
        this.allowFlight = allowFlight;
        this.instaMine = instaMine;
        this.invincible = invincible;
        this.blockBreak = blockBreak;
    }

    public PlayerAbilities setAbilities(PlayerAbilities abilities) {
        abilities.allowFlight = this.allowFlight;
        abilities.instaMine = this.instaMine;
        abilities.invincible = this.invincible;
        abilities.blockBreak = this.blockBreak;
        return abilities;
    }

    public static GameMode byOrdinal(int ordinal) {
        GameMode[] values = GameMode.values();
        if (ordinal >= values.length || ordinal < 0) {
            return null;
        }
        return values[ordinal];
    }
}
