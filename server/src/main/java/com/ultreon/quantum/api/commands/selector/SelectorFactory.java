package com.ultreon.quantum.api.commands.selector;

import com.ultreon.quantum.api.commands.CommandSender;

@FunctionalInterface
public interface SelectorFactory<T extends BaseSelector<?>> {
    T createSelector(CommandSender sender, String text);
}
