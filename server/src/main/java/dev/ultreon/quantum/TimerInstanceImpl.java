package dev.ultreon.quantum;

import java.util.Timer;

public class TimerInstanceImpl implements TimerInstance {
    private final Timer timer = new Timer("Quantum Timer");

    @Override
    public void cancel() {
        timer.cancel();
    }

    @Override
    public void schedule(TimerTask timerTask, long millis) {
        timer.schedule(new TimerTaskTimerTask(timerTask), millis);
    }

    @Override
    public void schedule(TimerTask timerTask, long millis, long interval) {
        timer.schedule(new TimerTaskTimerTask(timerTask), millis, interval);
    }

    private static class TimerTaskTimerTask extends java.util.TimerTask {

        private final TimerTask timerTask;

        public TimerTaskTimerTask(TimerTask timerTask) {
            this.timerTask = timerTask;
            timerTask.onCancelled = this::cancel;
        }

        @Override
        public void run() {
            timerTask.run();
        }
    }
}
