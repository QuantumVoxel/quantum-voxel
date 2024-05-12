package dev.ultreon.quantum;

import java.util.Objects;

public final class LoadingContext {
    private static final ThreadLocal<LoadingContext> currentContext = new ThreadLocal<>();
    private final String namespace;

    public LoadingContext(String namespace) {
        this.namespace = namespace;
    }

    public static LoadingContext get() {
        if (currentContext.get() == null) throw new IllegalStateException("Not in a loading context!");
        return LoadingContext.currentContext.get();
    }

    public static void withinContext(LoadingContext context, Runnable runnable) {
        LoadingContext.currentContext.set(context);
        runnable.run();
        LoadingContext.currentContext.remove();
    }

    public String namespace() {
        return namespace;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LoadingContext) obj;
        return Objects.equals(this.namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace);
    }

    @Override
    public String toString() {
        return "LoadingContext[" +
               "namespace=" + namespace + ']';
    }

}
