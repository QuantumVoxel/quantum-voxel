package dev.ultreon.quantum.api.events.entity;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.entity.LivingEntity;
import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface LivingEntityEvent extends EntityEvent {
    @Override
    @Nullable
    LivingEntity getEntity();

    class Death implements LivingEntityEvent, Cancelable {
        private final LivingEntity entity;
        @Nullable
        private DamageSource source;
        private boolean canceled;

        public Death(LivingEntity entity, @Nullable DamageSource source) {
            this.entity = entity;
            this.source = source;
        }

        public @Nullable DamageSource getSource() {
            return this.source;
        }

        public void setSource(@Nullable DamageSource source) {
            this.source = source;
        }

        @Override
        public @Nullable LivingEntity getEntity() {
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

    class Hurt implements Cancelable, LivingEntityEvent {
        private final LivingEntity entity;
        private DamageSource source;
        private float damage;
        private boolean canceled;

        public Hurt(LivingEntity entity, DamageSource source, float damage) {
            this.entity = entity;
            this.source = source;
            this.damage = damage;
        }

        public DamageSource getSource() {
            return this.source;
        }

        public void setSource(DamageSource source) {
            this.source = source;
        }

        public float getDamage() {
            return this.damage;
        }

        public void setDamage(float damage) {
            this.damage = damage;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        @Override
        public @Nullable LivingEntity getEntity() {
            return entity;
        }
    }

    class DropItems implements LivingEntityEvent, Cancelable {
        private final LivingEntity livingEntity;
        private final List<ItemStack> drops;
        private final DamageSource source;
        private boolean canceled;

        public DropItems(LivingEntity livingEntity, List<ItemStack> drops, DamageSource source) {
            this.livingEntity = livingEntity;
            this.drops = drops;
            this.source = source;
        }

        @Override
        public @Nullable LivingEntity getEntity() {
            return livingEntity;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        public DamageSource getSource() {
            return source;
        }

        public List<ItemStack> getDrops() {
            return drops;
        }
    }
}
