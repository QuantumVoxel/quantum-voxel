package dev.ultreon.quantum.api.commands.variables;

import com.ultreon.libs.commons.v0.Either;
import dev.ultreon.quantum.api.commands.CommandParseException;
import dev.ultreon.quantum.api.commands.CommandReader;
import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.api.commands.selector.BaseSelector;
import dev.ultreon.quantum.api.commands.selector.SelectorFactory;
import dev.ultreon.quantum.server.player.ServerPlayer;

import java.util.List;

public class SelectorObjectSource<T> implements ObjectSource<T> {
    private final String name;
    private final SelectorFactory<? extends BaseSelector<T>> selectorFactory;
    private final ObjectType<T> objectType;

    public SelectorObjectSource(String name, Class<T> type, SelectorFactory<? extends BaseSelector<T>> selectorFactory) {
        this.name = name;
        this.objectType = ObjectType.get(type);
        this.selectorFactory = selectorFactory;
    }

    public String getName() {
        return name;
    }

    public SelectorFactory<? extends BaseSelector<T>> getSelectorFactory() {
        return selectorFactory;
    }

    @Override
    public ObjectType<T> getObjectType() {
        return this.objectType;
    }

    public Object get(CommandSender sender, CommandReader ctx) throws CommandParseException {
        BaseSelector<T> selector = this.selectorFactory.createSelector(sender, ctx.readString());
        return selector.getValue();
    }

    @Override
    public Either<Object, List<String>> tabComplete(ServerPlayer serverPlayer, CommandReader ctx, StringBuilder code) {
        return Either.right(List.of());
    }
}
