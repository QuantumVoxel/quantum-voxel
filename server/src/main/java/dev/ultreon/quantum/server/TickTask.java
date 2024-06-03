package dev.ultreon.quantum.server;

public class TickTask implements Runnable {
    private int countdown;
    private final Runnable func;

    public TickTask(int delay, Runnable func) {
        this.countdown = delay;
        this.func = func;
    }

    @Override
    public void run() {
        if (countdown <= 0) {
            func.run();
        } else {
            countdown--;
            QuantumServer.get().submit(this);
        }
    }
}
