package dev.ultreon.quantum;

import dev.ultreon.quantum.log.Logger;

public class StdoutLogger implements Logger {
    @Override
    public void debug(String s) {
        System.out.printf("[DEBUG] %s%n", s);
    }

    @Override
    public void debug(String s, Object o) {
        if (o instanceof Throwable throwable) {
            System.out.printf("[DEBUG] %s %s%n", s, o);
            throwable.printStackTrace();
            return;
        }
        System.out.printf("[DEBUG] %s %s%n", s, o);
    }

    @Override
    public void debug(String s, Throwable t) {
        System.out.printf("[DEBUG] %s %s%n", s);
        t.printStackTrace();
    }

    @Override
    public void debug(String s, Object p, Object o) {
        if (o instanceof Throwable throwable) {
            System.out.printf("[DEBUG] %s %s%n", s, o);
            throwable.printStackTrace();
            return;
        }
        System.out.printf("[DEBUG] %s %s%n", s, o);
    }

    @Override
    public void debug(String s, Object p, Throwable t) {
        System.out.printf("[DEBUG] %s %s%n", s, p);
        t.printStackTrace();
    }

    @Override
    public void info(String s) {
        System.out.printf("[INFO] %s%n", s);
    }

    @Override
    public void info(String s, Object o) {
        if (o instanceof Throwable throwable) {
            System.out.printf("[INFO] %s %s%n", s, o);
            throwable.printStackTrace();
            return;
        }
        System.out.printf("[INFO] %s %s%n", s, o);
    }

    @Override
    public void info(String s, Object p, Object o) {
        if (o instanceof Throwable throwable) {
            System.out.printf("[INFO] %s %s%n", s, o);
            throwable.printStackTrace();
            return;
        }
        System.out.printf("[INFO] %s %s%n", s, o);
    }

    @Override
    public void info(String s, Object p, Throwable t) {
        System.out.printf("[INFO] %s %s%n", s, p);
        t.printStackTrace();
    }

    @Override
    public void info(String s, Throwable t) {
        System.out.printf("[INFO] %s%n", s);
        t.printStackTrace();
    }

    @Override
    public void warn(String s) {
        System.err.printf("[WARN] %s%n", s);
    }

    @Override
    public void warn(String s, Object o) {
        if (o instanceof Throwable throwable) {
            System.err.printf("[WARN] %s %s%n", s, o);
            throwable.printStackTrace();
            return;
        }
        System.err.printf("[WARN] %s %s%n", s, o);
    }

    @Override
    public void warn(String s, Object p, Object o) {
        if (o instanceof Throwable throwable) {
            System.err.printf("[WARN] %s %s%n", s, o);
            throwable.printStackTrace();
            return;
        }
        System.err.printf("[WARN] %s %s%n", s, o);
    }

    @Override
    public void warn(String s, Object p, Throwable t) {
        System.err.printf("[WARN] %s %s%n", s, p);
        t.printStackTrace();
    }

    @Override
    public void warn(String s, Throwable t) {
        System.err.printf("[WARN] %s%n", s);
        t.printStackTrace();
    }

    @Override
    public void error(String s) {
        System.err.println("[ERROR] " + s);
    }

    @Override
    public void error(String s, Object o) {
        if (o instanceof Throwable throwable) {
            System.err.printf("[ERROR] %s %s%n", s, o);
            throwable.printStackTrace();
            return;
        }
        System.err.printf("[ERROR] %s %s%n", s, o);
    }

    @Override
    public void error(String s, Throwable t) {
        System.err.printf("[ERROR] %s%n", s);
        t.printStackTrace();
    }

    @Override
    public void error(String s, Object p, Object o) {
        if (o instanceof Throwable throwable) {
            System.err.printf("[ERROR] %s %s%n", s, o);
            throwable.printStackTrace();
            return;
        }
        System.err.printf("[ERROR] %s %s%n", s, o);
    }

    @Override
    public void error(String s, Object p, Throwable t) {
        System.err.printf("[ERROR] %s %s%n", s, p);
        t.printStackTrace();
    }
}
