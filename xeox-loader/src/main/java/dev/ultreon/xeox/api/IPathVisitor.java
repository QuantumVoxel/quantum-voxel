package dev.ultreon.xeox.api;

import java.io.IOException;

@FunctionalInterface
public interface IPathVisitor {
    void visit(IPath path) throws IOException;
}
