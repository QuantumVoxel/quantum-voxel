package dev.ultreon.quantum.teavm;

import com.badlogic.gdx.utils.IntArray;
import dev.ultreon.quantum.TimerInstance;
import dev.ultreon.quantum.TimerTask;
import org.teavm.jso.browser.Window;

public class TeaVMTimer implements TimerInstance {
    private final IntArray timers = new IntArray();

    @Override
    public void cancel() {
        for (int i = 0; i < this.timers.size; i++) {
            Window.clearTimeout(this.timers.get(i));
        }
        this.timers.clear();
    }

    @Override
    public void schedule(TimerTask timerTask, long millis) {
        var ref = new Object() {
            int value = -1;
        };
        timerTask.onCancelled = () -> {
            this.timers.removeValue(ref.value);
            Window.clearTimeout(ref.value);
        };
        ref.value = Window.setTimeout(timerTask::run, millis / 1000.0);
        this.timers.add(ref.value);
    }

    @Override
    public void schedule(TimerTask timerTask, long millis, long interval) {
        var ref = new Object() {
            int value = -1;
        };
        timerTask.onCancelled = () -> {
            this.timers.removeValue(ref.value);
            Window.clearInterval(ref.value);
        };
        ref.value = Window.setInterval(timerTask::run, interval / 1000.0);
        this.timers.add(ref.value);
    }
}
