package dev.ultreon.quantum.api.commands;

import java.lang.reflect.Method;

@Deprecated
public class InvalidCommandMethodError extends Error {
    public InvalidCommandMethodError(Method method) {
        super(String.format("Invalid command method: %s in class: %s", method.getName(), method.getDeclaringClass().getName()));
    }

    public InvalidCommandMethodError(Method method, Throwable cause) {
        super(String.format("Invalid command method: %s in class: %s", method.getName(), method.getDeclaringClass().getName()), cause);
    }

    public InvalidCommandMethodError(String message) {
        super(message);
    }

    public InvalidCommandMethodError(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCommandMethodError(Throwable cause) {
        super(cause);
    }
}
