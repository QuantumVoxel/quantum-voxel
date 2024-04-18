package com.ultreon.quantum.world;

import com.ultreon.quantum.block.state.BlockProperties;

/**
 * Block flags for block updates.
 *
 * @see World#set(int, int, int, BlockProperties, int)
 */
public class BlockFlags {
    public static final int NONE = 0b00000000;

    /**
     * Neighboring blocks will be updated causing block physics or other handling of block update events.
     */
    public static final int UPDATE = 0b00000001;

    /**
     * The block will be synced to clients or the server
     */
    public static final int SYNC = 0b00000010;
}
