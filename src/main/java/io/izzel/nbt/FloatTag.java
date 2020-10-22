package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class FloatTag extends Tag {

    private final float value;

    private FloatTag(float value) {
        super(TagType.FLOAT);
        this.value = value;
    }

    public float getFloat() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitFloat(this.value);
    }

    @Override
    public String toString() {
        return this.value + "f";
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof FloatTag && Float.floatToIntBits(this.value) == Float.floatToIntBits(((FloatTag) o).value);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(Float.floatToIntBits(this.value));
    }

    public static FloatTag of(float f) {
        return new FloatTag(f);
    }
}
