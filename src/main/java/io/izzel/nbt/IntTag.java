package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class IntTag extends Tag<Integer> {

    private final int value;

    public IntTag(int value) {
        super(TagType.INT);
        this.value = value;
    }

    public int getInt() {
        return this.value;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitInt(this.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
