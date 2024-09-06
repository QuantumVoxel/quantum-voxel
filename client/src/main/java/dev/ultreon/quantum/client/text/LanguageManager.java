package dev.ultreon.quantum.client.text;

import de.marhali.json5.Json5;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ultreon.libs.commons.v0.Logger;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.registry.ClientRegistry;
import dev.ultreon.quantum.registry.Registry;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LanguageManager {
    public static final LanguageManager INSTANCE = new LanguageManager();
    public static final Registry<Language> REGISTRY = ClientRegistry.<Language>builder(new NamespaceID("language")).build();
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
    }

    public Language load(Locale locale, NamespaceID id, ResourceManager resourceManager) {
        try {
            Json5 gson = CommonConstants.JSON5;
            String newPath = "lang/" + id.getPath() + ".json";
            List<byte[]> assets = resourceManager.getAllDataById(id.withPath(newPath));
            Map<String, String> languageMap = new HashMap<>();
            for (byte[] asset : assets) {
                String s = new String(asset, StandardCharsets.UTF_8);
                System.out.println("s = " + s);
                Json5Object object = gson.parse(new StringReader(s)).getAsJson5Object();
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
        this.loadFile(languageMap, CommonConstants.JSON5.parse(reader).getAsJson5Object());
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

    private void loadFile(Map<String, String> languageMap, Json5Object object) {
        for (Map.Entry<String, Json5Element> entry : object.entrySet()) {
            Json5Element value = entry.getValue();
            String key = entry.getKey();
            if (value.isJson5Primitive() && value.getAsJson5Primitive().isString()) {
                languageMap.put(key, value.getAsString());
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
