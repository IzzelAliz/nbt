package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class DoubleTag extends Tag {

    private final double value;

    private DoubleTag(double value) {
        super(TagType.DOUBLE);
        this.value = value;
    }

    public double getDouble() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitDouble(this.value);
    }

    @Override
    public String toString() {
        return this.value + "d";
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof DoubleTag && Double.doubleToLongBits(this.value) == Double.doubleToLongBits(((DoubleTag) o).value);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(Double.doubleToLongBits(this.value));
    }

    public static DoubleTag of(double d) {
        return new DoubleTag(d);
    }
}
