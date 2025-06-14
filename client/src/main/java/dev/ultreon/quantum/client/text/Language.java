package dev.ultreon.quantum.client.text;

import dev.ultreon.quantum.util.NamespaceID;

import java.util.Locale;
import java.util.Map;

public class Language {
    private final Locale locale;
    private final Map<String, String> languageMap;
    private final NamespaceID id;

    public Language(Locale locale, Map<String, String> languageMap, NamespaceID id) {
        this.locale = locale;
        this.languageMap = languageMap;
        this.id = id;
    }

    public String get(String path, Object... args) {
        String s = this.languageMap.get(path);
        return s == null ? null : String.format(s, args);
    }

    public Locale getLocale() {
        return this.locale;
    }

    public NamespaceID getId() {
        return this.id;
    }

    public static String translate(String path, Object... args) {
        Language language = dev.ultreon.quantum.client.text.LanguageManager.INSTANCE.get(dev.ultreon.quantum.client.text.LanguageManager.getCurrentLanguage());
        if (language == null) return translateFallback(path, args);
        String s = language.get(path, args);
        return s == null ? translateFallback(path, args) : s;
    }

    private static String translateFallback(String path, Object[] args) {
        Language english = LanguageManager.INSTANCE.get(new Locale("en", "us"));
        if (english == null) {
            return path;
        }
        String s = english.get(path, args);
        return s == null ? path : s;
    }
}
