package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.Arrays;
import java.util.StringJoiner;

public final class LongArrayTag extends Tag<long[]> {

    private final long[] value;

    public LongArrayTag(long[] value) {
        super(TagType.LONG_ARRAY);
        this.value = value;
    }

    @Override
    public long[] getValue() {
        return this.value;
    }

    @Override
    public Tag<long[]> copy() {
        return new LongArrayTag(Arrays.copyOf(this.value, this.value.length));
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitLongArray(this.value);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "[L:", "]");
        for (long l : value) {
            joiner.add(String.valueOf(l));
        }
        return joiner.toString();
    }
}
