package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.Inventory;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.text.TextObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ServerPlayerEvent extends PlayerEvent {
    @Nullable
    ServerPlayer getEntity();

    class Join implements ServerPlayerEvent {
        private final @NotNull ServerPlayer entity;

        public Join(@NotNull ServerPlayer entity) {
            this.entity = entity;
        }

        @Override
        public @NotNull ServerPlayer getEntity() {
            return entity;
        }
    }

    class AttemptJoin implements ServerPlayerEvent {
        private final @NotNull ServerPlayer entity;

        private @Nullable TextObject denied;

        public AttemptJoin(@NotNull ServerPlayer entity) {
            this.entity = entity;
        }

        public void setDenied(@Nullable TextObject reason) {
            this.denied = reason;
        }

        public void setDenied(@Nullable String reason) {
            if (reason == null) {
                this.denied = null;
                return;
            }
            setDenied(TextObject.literal(reason));
        }

        public void setDenied(boolean denied) {
            setDenied(denied ? TextObject.translation("quantum.multiserverPlayer.auth.denied") : null);
        }

        public @Nullable TextObject getDenyReason() {
            return denied;
        }

        public boolean isDenied() {
            return denied != null;
        }

        public boolean isAllowed() {
            return !isDenied();
        }

        @Override
        public @NotNull ServerPlayer getEntity() {
            return entity;
        }
    }

    class Left implements ServerPlayerEvent {
        private final @NotNull ServerPlayer entity;

        private final String message;

        public Left(@NotNull ServerPlayer entity, String message) {
            this.entity = entity;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public @NotNull ServerPlayer getEntity() {
            return entity;
        }

    }

    class Spawned implements ServerPlayerEvent {
        private final @NotNull ServerPlayer entity;

        public Spawned(@NotNull ServerPlayer entity) {
            this.entity = entity;
        }

        @Override
        public @NotNull ServerPlayer getEntity() {
            return entity;
        }
    }

    class InitialItems implements ServerPlayerEvent, Cancelable {
        private final ServerPlayer entity;
        private final Inventory inventory;
        private boolean canceled;

        public InitialItems(ServerPlayer entity, Inventory inventory) {
            this.entity = entity;
            this.inventory = inventory;
        }

        public Inventory getInventory() {
            return inventory;
        }

        @Override
        public @Nullable ServerPlayer getEntity() {
            return entity;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }

    class ItemDropped implements ServerPlayerEvent, ItemStackEvent, Cancelable {
        private final ServerPlayer entity;
        private final ItemStack itemStack;
        private boolean canceled;

        public ItemDropped(ServerPlayer entity, ItemStack itemStack) {
            this.entity = entity;
            this.itemStack = itemStack;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        @Override
        public @Nullable ServerPlayer getEntity() {
            return entity;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }
}
