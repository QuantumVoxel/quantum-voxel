package dev.ultreon.logging.compat;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.util.WeakHashMap;

public class SLF4JCompat implements SLF4JServiceProvider {
    public static String REQUESTED_API_VERSION = "2.0.99";

    private final WeakHashMap<String, ULogger> loggers = new WeakHashMap<>();
    private final WeakHashMap<String, ULoggerMarker> marker = new WeakHashMap<>();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return name -> loggers.computeIfAbsent(name, ULogger::new);
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return new IMarkerFactory() {

            @Override
            public Marker getMarker(String name) {
                return marker.computeIfAbsent(name, ULoggerMarker::new);
            }

            @Override
            public boolean exists(String name) {
                return marker.containsKey(name);
            }

            @Override
            public boolean detachMarker(String name) {
                ULoggerMarker remove = marker.remove(name);
                if (remove != null) {
                    // Welp fuck off then :3
                }
                return remove != null;
            }

            @Override
            public Marker getDetachedMarker(String name) {
                return null;
            }
        };
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return null;
    }

    @Override
    public String getRequestedApiVersion() {
        return REQUESTED_API_VERSION;
    }

    @Override
    public void initialize() {
        // Meow :3
    }
}
