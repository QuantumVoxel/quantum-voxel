package dev.ultreon.quantum.client;

import com.github.tommyettinger.textra.utils.CaseInsensitiveIntMap;

public class TranslatableNameLookup extends CaseInsensitiveIntMap {
    @Override
    public int get(String key, int defaultValue) {
        return super.get(key, defaultValue);
    }
}
