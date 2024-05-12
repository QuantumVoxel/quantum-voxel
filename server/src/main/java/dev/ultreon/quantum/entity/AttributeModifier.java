package dev.ultreon.quantum.entity;

import dev.ultreon.libs.functions.v0.BiDouble2DoubleFunction;

import java.util.Objects;
import java.util.UUID;

public final class AttributeModifier {
    private final UUID id;
    private final Operation operation;
    private final double value;

    public AttributeModifier(UUID id, Operation operation, double value) {
        this.id = id;
        this.operation = operation;
        this.value = value;
    }

    public UUID id() {
        return id;
    }

    public Operation operation() {
        return operation;
    }

    public double value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AttributeModifier) obj;
        return Objects.equals(this.id, that.id) &&
               Objects.equals(this.operation, that.operation) &&
               Double.doubleToLongBits(this.value) == Double.doubleToLongBits(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, operation, value);
    }

    @Override
    public String toString() {
        return "AttributeModifier[" +
               "id=" + id + ", " +
               "operation=" + operation + ", " +
               "value=" + value + ']';
    }


    public enum Operation {
        PLUS((a, b) -> a + b),
        MULTIPLY((a, b) -> a * b);

        private final BiDouble2DoubleFunction function;

        Operation(BiDouble2DoubleFunction function) {
            this.function = function;
        }

        public double calculate(double a, double b) {
            return this.function.apply(a, b);
        }
    }
}
