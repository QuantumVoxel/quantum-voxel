package dev.ultreon.quantum.server.player;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Tracker<T> {
    private final Set<T> tracked = new LinkedHashSet<>();

    public void startTracking(T t) {
        tracked.add(t);
    }

    public void stopTracking(T t) {
        tracked.remove(t);
    }

    public boolean isTracking(T t) {
        return tracked.contains(t);
    }

    public boolean isTrackingAnything() {
        return !tracked.isEmpty();
    }

    public boolean isTrackingNothing() {
        return tracked.isEmpty();
    }

    public Collection<? extends T> getTracked() {
        return Collections.unmodifiableCollection(tracked);
    }
}
