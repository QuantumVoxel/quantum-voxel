package dev.ultreon.xeox.impl.main;

import dev.ultreon.xeox.impl.XeoxLoader;
import dev.ultreon.xeox.impl.games.quantum.QuantumGameProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static final Logger LOGGER = LoggerFactory.getLogger("XeoxLoader");

    public static void main(String[] args) {
        XeoxLoader.create(args, new QuantumGameProvider());
    }
}