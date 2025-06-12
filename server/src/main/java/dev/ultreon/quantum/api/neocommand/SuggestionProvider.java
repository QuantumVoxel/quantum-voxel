package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.UUID;

public interface SuggestionProvider {
    void suggest(String value);

    default void suggest(UUID uuid) {
        String current = getCurrent();
        if (current.startsWith(uuid.toString().replace("-", "")) || current.startsWith(uuid.toString())) {
            this.suggest(uuid.toString());
        }
    }

    default void suggest(String... values) {
        for (String value : values) {
            if (getCurrent().startsWith(value)) {
                this.suggest(value);
            }
        }
    }

    default void suggest(Iterable<String> values) {
        for (String value : values) {
            if (getCurrent().startsWith(value)) {
                this.suggest(value);
            }
        }
    }

    default void suggest(Object... values) {
        for (Object value : values) {
            if (getCurrent().startsWith(value.toString())) {
                this.suggest(value.toString());
            }
        }
    }

    default void suggestNamespaceIDs(NamespaceID... values) {
        for (NamespaceID value : values) {
            if (getCurrent().startsWith(value.toString())) {
                this.suggest(value.toString());
            } else if (getCurrent().startsWith(value.getPath())) {
                this.suggest(value.getPath());
            }
        }
    }

    default void suggestModIDs(QuantumServer server) {
        for (Mod value : GamePlatform.get().getMods()) {
            if (getCurrent().startsWith(value.getId()) || getCurrent().startsWith(value.getName())) {
                this.suggest(value.getId());
            }
        }
    }

    default void suggestInts(int... values) {
        String current = getCurrent();
        for (int value : values) {
            try {
                String value1 = String.valueOf(value);
                Integer intValue = Integer.parseInt(value1);
                if (current.startsWith(value1)) {
                    this.suggest(intValue);
                }
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
    }

    default void suggestLongs(long... values) {
        for (long value : values) {
            try {
                String value1 = String.valueOf(value);
                Long longValue = Long.parseLong(value1);
                if (getCurrent().startsWith(value1)) {
                    this.suggest(longValue);
                }
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
    }

    default void suggestFloats(float... values) {
        for (float value : values) {
            try {
                String value1 = String.valueOf(value);
                Float floatValue = Float.parseFloat(value1);
                if (getCurrent().startsWith(value1)) {
                    this.suggest(floatValue);
                }
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
    }

    default void suggestDoubles(double... values) {
        for (double value : values) {
            try {
                String value1 = String.valueOf(value);
                Double doubleValue = Double.parseDouble(value1);
                if (getCurrent().startsWith(value1)) {
                    this.suggest(doubleValue);
                }
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
    }

    default void suggestBooleans(boolean... values) {
        for (boolean value : values) {
            if (getCurrent().startsWith(value ? "true" : "false")) {
                this.suggest(value);
            }
        }
    }

    String getCurrent();

    QuantumServer getServer();

    CommandSender getSender();
}
