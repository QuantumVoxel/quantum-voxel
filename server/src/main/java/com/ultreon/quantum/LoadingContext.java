package com.ultreon.quantum;

public record LoadingContext(String namespace) {
    private static final ThreadLocal<LoadingContext> currentContext = new ThreadLocal<>();

    public static LoadingContext get() {
        if (currentContext.get() == null) throw new IllegalStateException("Not in a loading context!");
        return LoadingContext.currentContext.get();
    }

    public static void withinContext(LoadingContext context, Runnable runnable) {
        LoadingContext.currentContext.set(context);
        runnable.run();
        LoadingContext.currentContext.remove();
    }
}
