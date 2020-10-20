package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class ByteTag extends Tag<Byte> {

    private final byte value;

    public ByteTag(byte value) {
        super(TagType.BYTE);
        this.value = value;
    }

    public byte getByte() {
        return this.value;
    }

    @Override
    public Byte getValue() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitByte(this.value);
    }

    @Override
    public String toString() {
        return value + "b";
    }
}
