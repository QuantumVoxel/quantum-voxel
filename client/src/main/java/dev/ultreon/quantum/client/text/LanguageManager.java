package dev.ultreon.quantum.client.text;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.libs.commons.v0.Logger;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.GameFont;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.registry.SimpleRegistry;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LanguageManager {
    public static final LanguageManager INSTANCE = new LanguageManager();
    public static final Registry<Language> REGISTRY = SimpleRegistry.<Language>builder(new NamespaceID("language")).build();
    private static Locale currentLanguage;
    private final Map<Locale, Language> languages = new HashMap<>();
    private final Set<Locale> locales = new HashSet<>();
    private final Set<NamespaceID> ids = new HashSet<>();
    private final Map<Locale, NamespaceID> locale2id = new HashMap<>();
    private final Map<NamespaceID, Locale> id2locale = new HashMap<>();
    private Logger logger = (level, message, t) -> {};

    private LanguageManager() {

    }

    public static Locale getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setCurrentLanguage(Locale currentLanguage) {
        LanguageManager.currentLanguage = currentLanguage;
        GameFont.update();
    }

    public static boolean isUpsideDown() {
        if (currentLanguage == null) {
            return false;
        }
        return currentLanguage.getCountry().equals("en") && currentLanguage.getLanguage().equals("ud");
    }

    public Language load(Locale locale, NamespaceID id, ResourceManager resourceManager) {
        try {
            JsonReader gson = CommonConstants.JSON_READ;
            String newPath = "lang/" + id.getPath() + ".json";
            List<byte[]> assets = resourceManager.getAllDataById(id.withPath(newPath));
            if (assets.isEmpty()) {
                try {
                    assets = new ArrayList<>();
                    assets.add(QuantumClient.resource(id.withPath(newPath)).readBytes());
                } catch (Exception e) {
                    CommonConstants.LOGGER.warn("Language not found: " + id);
                    return null;
                }
            }
            Map<String, String> languageMap = new HashMap<>();
            for (byte[] asset : assets) {
                String s = new String(asset, StandardCharsets.UTF_8);
                JsonValue object = gson.parse(new StringReader(s));
                this.loadFile(languageMap, object);
            }

            Language language = new Language(locale, languageMap, id);
            this.languages.put(locale, language);
            REGISTRY.register(id, language);

            return language;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load language " + id, e);
        }
    }

    public Language load(Locale locale, NamespaceID id, Reader reader) {
        Map<String, String> languageMap = new HashMap<>();
        this.loadFile(languageMap, CommonConstants.JSON_READ.parse(reader));
        Language language = new Language(locale, languageMap, id);
        this.languages.put(locale, language);
        REGISTRY.register(id, language);
        return language;
    }

    public Language get(Locale locale) {
        return this.languages.get(locale);
    }

    public void register(Locale locale, NamespaceID id) {
        if (this.locales.contains(locale)) {
            this.getLogger().warn("Locale overridden: " + locale.getLanguage());
        }
        if (this.ids.contains(id)) {
            this.getLogger().warn("LanguageID overridden: " + id);
        }

        this.locales.add(locale);
        this.ids.add(id);
        this.locale2id.put(locale, id);
        this.id2locale.put(id, locale);
    }

    public Locale getLocale(NamespaceID id) {
        return this.id2locale.get(id);
    }

    public NamespaceID getLanguageID(Locale locale) {
        return this.locale2id.get(locale);
    }

    private void loadFile(Map<String, String> languageMap, JsonValue object) {
        for (JsonValue entry : object) {
            String key = entry.name;
            if (entry.isString()) {
                languageMap.put(key, entry.asString());
            }
        }
    }

    public Set<Locale> getLocales() {
        return new HashSet<>(this.locales);
    }

    public Set<NamespaceID> getLanguageIDs() {
        return new HashSet<>(this.ids);
    }

    public List<Language> getLanguages() {
        return new ArrayList<>(this.languages.values());
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
