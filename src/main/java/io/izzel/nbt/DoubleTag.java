package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class DoubleTag extends Tag<Double> {

    private final double value;

    public DoubleTag(double value) {
        super(TagType.DOUBLE);
        this.value = value;
    }

    public double getDouble() {
        return this.value;
    }

    @Override
    public Double getValue() {
        return this.value;
    }

    @Override
    public Tag<Double> copy() {
        return new DoubleTag(this.value);
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitDouble(this.value);
    }

    @Override
    public String toString() {
        return value + "d";
    }
}
