package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.menu.ItemSlot;
import dev.ultreon.quantum.menu.Menu;
import dev.ultreon.quantum.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface MenuEvent extends Event {
    Menu getMenu();

    default <T> @Nullable T getMenu(Class<T> clazz) {
        if(clazz.isInstance(getMenu())) {
            return clazz.cast(getMenu());
        }
        return null;
    }

    default <T extends ContainerMenu> @Nullable T getContainerMenu(Class<T> clazz) {
        if(clazz.isInstance(getMenu())) {
            return clazz.cast(getMenu());
        }
        return null;
    }

    class Open implements MenuEvent, ServerPlayerEvent, Cancelable {
        private final Menu menu;
        private final ServerPlayer entity;
        private boolean canceled;

        public Open(Menu menu, ServerPlayer entity) {
            this.menu = menu;
            this.entity = entity;
        }

        @Override
        public Menu getMenu() {
            return menu;
        }

        @Override
        public ServerPlayer getEntity() {
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

    class Close implements MenuEvent, ServerPlayerEvent {
        private final Menu menu;
        private final ServerPlayer entity;

        public Close(Menu menu, ServerPlayer entity) {
            this.menu = menu;
            this.entity = entity;
        }

        @Override
        public Menu getMenu() {
            return menu;
        }

        @Override
        public @Nullable ServerPlayer getEntity() {
            return entity;
        }
    }

    class Click implements MenuEvent, ServerPlayerEvent, ItemStackEvent, Cancelable {
        private final Menu menu;
        private final ServerPlayer entity;
        private final ItemSlot slot;
        private final boolean rightClick;
        private boolean canceled;

        public Click(Menu menu, ServerPlayer entity, ItemSlot slot, boolean rightClick) {
            this.menu = menu;
            this.entity = entity;
            this.slot = slot;
            this.rightClick = rightClick;
        }

        @Override
        public Menu getMenu() {
            return menu;
        }

        public boolean isRightClick() {
            return rightClick;
        }

        public ItemSlot getSlot() {
            return slot;
        }

        @Override
        public ItemStack getItemStack() {
            return slot.getItem();
        }

        public void setItemStack(ItemStack itemStack) {
            slot.setItem(itemStack);
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
