package dev.ultreon.xeox.impl;

public record EntryPoint(String type, String name) {
    public static final String TYPE_CLIENT = "client";
    public static final String TYPE_SERVER = "server";
    public static final String TYPE_COMMON = "common";
    public static final String TYPE_PREINIT = "preinit";
}
