package com.ultreon.quantum.entity;

public record Attribute(String key) {
    public static final Attribute SPEED = new Attribute("quantum.generic.speed");
    public static final Attribute BLOCK_REACH = new Attribute("quantum.generic.block_reach");
    public static final Attribute ENTITY_REACH = new Attribute("quantum.generic.entity_reach");
}
