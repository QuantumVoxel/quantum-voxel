package dev.ultreon.quantum.android.log;

import android.util.Log;
import dev.ultreon.quantum.log.Logger;

public class AndroidLogger implements Logger {
    private final String name;

    public AndroidLogger(String name) {
        this.name = name;
    }

    @Override
    public void debug(String s) {
        Log.d(this.name, s);
    }

    @Override
    public void debug(String s, Object o) {
        if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            Log.d(this.name, s, throwable);
            return;
        }
        Log.d(this.name, String.format(s, o));
    }

    @Override
    public void debug(String s, Throwable t) {
        Log.d(this.name, s, t);
    }

    @Override
    public void debug(String s, Object p, Object o) {
        if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            Log.d(this.name, String.format(s, p), throwable);
            return;
        }
        Log.d(this.name, String.format(s, p, o));
    }

    @Override
    public void debug(String s, Object p, Throwable t) {
        Log.d(this.name, String.format(s, p), t);
    }

    @Override
    public void info(String s) {
        Log.i(this.name, s);
    }

    @Override
    public void info(String s, Object o) {
        if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            Log.i(this.name, s, throwable);
            return;
        }
        Log.i(this.name, String.format(s, o));
    }

    @Override
    public void info(String s, Object p, Object o) {
        if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            Log.i(this.name, String.format(s, p), throwable);
            return;
        }
        Log.i(this.name, String.format(s, p, o));
    }

    @Override
    public void info(String s, Object p, Throwable t) {
        Log.i(this.name, String.format(s, p), t);
    }

    @Override
    public void info(String s, Throwable t) {
        Log.i(this.name, s, t);
    }

    @Override
    public void warn(String s) {
        Log.w(this.name, s);
    }

    @Override
    public void warn(String s, Object o) {
        if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            Log.w(this.name, s, throwable);
            return;
        }
        Log.w(this.name, String.format(s, o));
    }

    @Override
    public void warn(String s, Object p, Object o) {
        if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            Log.w(this.name, String.format(s, p), throwable);
            return;
        }
        Log.w(this.name, String.format(s, p, o));
    }

    @Override
    public void warn(String s, Object p, Throwable t) {
        Log.w(this.name, String.format(s, p), t);
    }

    @Override
    public void warn(String s, Throwable t) {
        Log.w(this.name, s, t);
    }

    @Override
    public void error(String s) {
        Log.e(this.name, s);
    }

    @Override
    public void error(String s, Object o) {
        if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            Log.e(this.name, s, throwable);
            return;
        }
        Log.e(this.name, String.format(s, o));
    }

    @Override
    public void error(String s, Throwable t) {
        Log.e(this.name, s, t);
    }

    @Override
    public void error(String s, Object p, Object o) {
        if (o instanceof Throwable) {
            Throwable throwable = (Throwable) o;
            Log.e(this.name, String.format(s, p), throwable);
            return;
        }
        Log.e(this.name, String.format(s, p, o));
    }

    @Override
    public void error(String s, Object p, Throwable t) {
        Log.e(this.name, String.format(s, p), t);
    }
}
