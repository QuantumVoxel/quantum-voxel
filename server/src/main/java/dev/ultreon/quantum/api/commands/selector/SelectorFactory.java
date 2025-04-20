package dev.ultreon.quantum.api.commands.selector;

import dev.ultreon.quantum.api.commands.CommandSender;

@FunctionalInterface
@Deprecated
public interface SelectorFactory<T extends BaseSelector<?>> {
    T createSelector(CommandSender sender, String text);
}
