package dev.ultreon.quantum.client.gui.debug;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.debug.timing.Timed;
import dev.ultreon.quantum.debug.timing.Timing;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class TimingsDebugPage implements DebugPage {
    private @Nullable Array<Timed> timings = null;
    private long lastUpdated;

    @Override
    public void render(DebugPageContext context) {
        context.left("Timings");

        if (timings == null || lastUpdated + 1000 < System.currentTimeMillis()) {
            timings = Timing.getTimings();
            lastUpdated = System.currentTimeMillis();
        }
        Array<Timed> timings = this.timings;
        if (timings == null) return;
        for (Timed timing : timings.toArray(Timed.class)) {
            context.left(timing.name, String.format(Locale.ROOT, "%.3f%%", timing.time / 10000000.0));
        }
    }

    @Override
    public void update(boolean selected) {
        Timing.enabled = selected;
    }
}
