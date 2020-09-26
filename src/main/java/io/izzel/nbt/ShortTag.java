package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public class ShortTag extends Tag<Short> {

    private final short value;

    public ShortTag(short value) {
        super(TagType.SHORT);
        this.value = value;
    }

    public short getShort() {
        return this.value;
    }

    @Override
    public Short getValue() {
        return this.value;
    }

    @Override
    public Tag<Short> copy() {
        return new ShortTag(this.value);
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitShort(this.value);
    }

    @Override
    public String toString() {
        return value + "s";
    }
}
