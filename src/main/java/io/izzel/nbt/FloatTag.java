package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class FloatTag extends Tag<Float> {

    private final float value;

    public FloatTag(float value) {
        super(TagType.FLOAT);
        this.value = value;
    }

    @Override
    public Float getValue() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitFloat(this.value);
    }

    @Override
    public String toString() {
        return value + "f";
    }
}
