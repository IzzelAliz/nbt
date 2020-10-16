package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.Arrays;
import java.util.StringJoiner;

public final class LongArrayTag extends Tag<long[]> {

    private final int hash;

    private final long[] value;

    public LongArrayTag(long[] value) {
        super(TagType.LONG_ARRAY);
        this.hash = Arrays.hashCode(value);
        this.value = Arrays.copyOf(value, value.length);
    }

    @Override
    public long[] getValue() {
        return Arrays.copyOf(value, value.length);
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

    @Override
    public boolean equals(Object o) {
        return o instanceof LongArrayTag && Arrays.equals(((LongArrayTag) o).value, value);
    }

    @Override
    public int hashCode() {
        return this.hash;
    }
}
