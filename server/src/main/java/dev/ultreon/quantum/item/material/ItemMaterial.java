package dev.ultreon.quantum.item.material;

public interface ItemMaterial {
    static Builder builder() {
        return new Builder();
    }

    float getEfficiency();

    float getAttackDamage();

    class Builder {
        private float efficiency;
        private float attackDamage;

        public Builder efficiency(float efficiency) {
            this.efficiency = efficiency;
            return this;
        }

        public Builder attackDamage(float attackDamage) {
            this.attackDamage = attackDamage;
            return this;
        }

        public ItemMaterial build() {
            return new ItemMaterial() {
                @Override
                public float getEfficiency() {
                    return efficiency;
                }

                @Override
                public float getAttackDamage() {
                    return attackDamage;
                }
            };
        }
    }
}
