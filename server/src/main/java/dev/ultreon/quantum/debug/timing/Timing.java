package dev.ultreon.quantum.debug.timing;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectLongMap;
import org.jetbrains.annotations.Nullable;

public class Timing {
    public static boolean enabled = false;
    private static final ObjectLongMap<String> runningTimings = new ObjectLongMap<>();
    private static final ObjectLongMap<String> timings = new ObjectLongMap<>();

    public static void end(String name) {
        if (!enabled) return;

        long end = System.nanoTime();
        long start = runningTimings.remove(name, end);
        long prev = timings.get(name, 0);
        timings.put(name, prev + end - start);
    }

    public static void start(String name) {
        if (!enabled) return;

        runningTimings.put(name, System.nanoTime());
    }

    public static @Nullable Array<Timed> getTimings() {
        if (!enabled) return null;

        Array<Timed> timed = new Array<>();
        long total = 0;

        for (String name : timings.keys()) {
            long time = timings.get(name, -1);
            if (time == -1) continue;
            total += time;
            timed.add(new Timed(name, time));
        }

        timed.add(new Timed("Total", total));
        timed.sort((o1, o2) -> Long.compare(o2.time, o1.time));

        timings.clear();

        return timed;
    }

    public static long getTiming(String name) {
        if (!enabled) return 0;

        return timings.get(name, -1L) / 1_000_000;
    }
}
