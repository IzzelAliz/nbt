package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class LongTag extends Tag<Long> {

    private final long value;

    public LongTag(long value) {
        super(TagType.LONG);
        this.value = value;
    }

    public long getLong() {
        return this.value;
    }

    @Override
    public Long getValue() {
        return this.value;
    }

    @Override
    public Tag<Long> copy() {
        return new LongTag(this.value);
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitLong(this.value);
    }

    @Override
    public String toString() {
        return value + "l";
    }
}
