package dev.ultreon.quantum.android.log;

import android.util.Log;
import dev.ultreon.quantum.Logger;

import java.text.MessageFormat;

public class AndroidLogger implements Logger {
    private final String name;

    public AndroidLogger(String name) {
        this.name = name;
    }

    @Override
    public void log(Level level, String message, Throwable t) {
        if (level == null) return;
        
        if (t == null) {
            switch (level) {
                case TRACE:
                    Log.v(name, message);
                    break;
                case DEBUG:
                    Log.d(name, message);
                    break;
                case INFO:
                    Log.i(name, message);
                    break;
                case WARN:
                    Log.w(name, message);
                    break;
                case ERROR:
                    Log.e(name, message);
                    break;
            }
            return;
        }
        
        switch (level) {
            case TRACE:
                Log.v(name, message, t);
                break;
            case DEBUG:
                Log.d(name, message, t);
                break;
            case INFO:
                Log.i(name, message, t);
                break;
            case WARN:
                Log.w(name, message, t);
                break;
            case ERROR:
                Log.e(name, message, t);
                break;
        }
    }
}
