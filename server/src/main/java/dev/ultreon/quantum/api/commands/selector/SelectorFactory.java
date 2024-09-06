package dev.ultreon.quantum.api.commands.selector;

import dev.ultreon.quantum.api.commands.CommandSender;

@FunctionalInterface
public interface SelectorFactory<T extends BaseSelector<?>> {
    T createSelector(CommandSender sender, String text);
}
