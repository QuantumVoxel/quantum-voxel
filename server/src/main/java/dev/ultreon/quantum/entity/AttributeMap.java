package dev.ultreon.quantum.entity;

import java.util.*;

public class AttributeMap {
    private final Map<Attribute, Double> bases = new HashMap<>();
    private final Map<Attribute, Map<UUID, AttributeModifier>> modifiers = new HashMap<>();

    public void setBase(Attribute attribute, double base) {
        this.bases.put(attribute, base);
    }

    public double getBase(Attribute attribute) {
        return this.bases.getOrDefault(attribute, 0.0);
    }

    public void addModifier(Attribute attribute, AttributeModifier modifier) {
        this.modifiers.computeIfAbsent(attribute, key -> new HashMap<>()).put(modifier.id(), modifier);
    }

    public AttributeModifier removeModifier(Attribute attribute, UUID uuid) {
        return this.modifiers.get(attribute).remove(uuid);
    }

    public double get(Attribute attribute) {
        double value = this.bases.getOrDefault(attribute, 0.0);
        List<AttributeModifier> list = new ArrayList<>();
        list.addAll(this.modifiers.getOrDefault(attribute, Collections.emptyMap()).values());
        list.sort(Comparator.comparing(modifier -> modifier.operation().ordinal()));
        for (AttributeModifier modifier : list) {
            AttributeModifier.Operation operation = modifier.operation();
            value = operation.calculate(value, modifier.value());
        }

        return value;
    }
}
