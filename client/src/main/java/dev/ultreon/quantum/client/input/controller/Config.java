package dev.ultreon.quantum.client.input.controller;


import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.widget.TextButton;
import dev.ultreon.quantum.client.gui.widget.Widget;
import dev.ultreon.quantum.client.input.controller.entries.ControllerBindingEntry;
import dev.ultreon.quantum.client.input.controller.gui.BindingsConfigScreen;
import dev.ultreon.quantum.client.input.controller.gui.ConfigEntry;
import dev.ultreon.quantum.client.input.dyn.ControllerInterDynamic;
import dev.ultreon.quantum.collection.OrderedMap;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import org.checkerframework.common.value.qual.IntRange;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private static final Map<ControllerContext, Config> CONFIGS = new OrderedMap<>();

    private final Map<ControllerMapping<?>, ConfigEntry<?>> mappings = new HashMap<>();
    private final Map<String, ConfigEntry<?>> entryMap = new HashMap<>();
    private final List<ConfigEntry<?>> entries = new ArrayList<>();
    private final NamespaceID key;
    private final ControllerContext context;
    private final Path file;

    public Config(NamespaceID key, ControllerContext context) {
        this.key = key;
        this.context = context;

        Path bindingsDir = Paths.get("config/quantum");
        if (key.getDomain().equals(CommonConstants.NAMESPACE))
            file = bindingsDir.resolve(key.getPath() + ".txt");
        else
            file = bindingsDir.resolve(key.getDomain() + "/" + key.getPath() + ".txt");

        for (ControllerMapping<?> mapping : context.mappings.getAllMappings()) {
            ConfigEntry<?> entry = mapping.createEntry(this);
            mappings.put(mapping, entry);
        }
    }

    public ConfigEntry<?> byMapping(ControllerMapping<?> mapping) {
        return this.mappings.get(mapping);
    }

    public static void register(Config config) {
        CONFIGS.put(config.context, config);
    }

    public static Config[] getConfigs() {
        return CONFIGS.values().toArray(new Config[0]);
    }

    public static void saveAll() {
        for (Config config : CONFIGS.values()) {
            config.save();
        }
    }

    public <T extends Enum<T> & ControllerInterDynamic<?>> ConfigEntry<T> add(String key, ControllerMapping<T> defaultValue, TextObject description) {
        ConfigEntry<T> entry = new ControllerBindingEntry<>(key, defaultValue, defaultValue, description).comment(description.getText());
        entryMap.put(entry.getKey(), entry);
        entries.add(entry);

        return entry;
    }

    public void load() {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String s;
            while ((s = reader.readLine()) != null) {
                if (s.startsWith("#")) {
                    continue;
                }
                String[] entryArr = s.split("=", 2);
                if (entryArr.length <= 1) {
                    continue;
                }

                ConfigEntry<?> entry = entryMap.get(entryArr[0]);
                entry.readAndSet(entryArr[1]);
            }
        } catch (FileNotFoundException | NoSuchFileException ignored) {

        } catch (Exception e) {
            QuantumClient.LOGGER.error("Failed to load config", e);
        }
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            for (ConfigEntry<?> e : entryMap.values()) {
                String key = e.getKey();
                String value = e.write();

                String comment = e.getComment();
                if (comment != null && !comment.isBlank()) {
                    writer.write("# ");
                    writer.write(comment.trim().replaceAll("\r\n", " ").replaceAll("\r", " ").replaceAll("\n", " "));
                    writer.newLine();
                }
                writer.write(key);
                writer.write("=");
                writer.write(value);
                writer.newLine();
            }
        } catch (Exception e) {
            QuantumClient.LOGGER.error("Failed to save config", e);
        }
    }

    public ConfigEntry<?>[] values() {
        return entries.toArray(new ConfigEntry[0]);
    }

    public Widget createButton(Config config, int w) {
        return new ConfigButton(w, config);
    }

    public ControllerContext getContext() {
        return context;
    }

    private static class ConfigButton extends TextButton {
        public ConfigButton(@IntRange(from = 21) int w, Config config) {
            super(w, 21);
            this.text().set(TextObject.translation("quantum.open_config"));
            this.getCallback(button -> new BindingsConfigScreen(QuantumClient.get().screen, config).open());
        }
    }

    public TextObject getTitle() {
        return TextObject.translation("quantum.config." + this.key.toString().replace(":", "."));
    }
}
