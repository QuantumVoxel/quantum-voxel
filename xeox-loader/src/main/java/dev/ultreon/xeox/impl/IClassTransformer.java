package dev.ultreon.xeox.impl;

public interface IClassTransformer {
    byte[] transform(String name, String transformedName, byte[] basicClass);
}
