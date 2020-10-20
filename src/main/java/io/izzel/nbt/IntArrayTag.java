package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.Arrays;
import java.util.StringJoiner;

public final class IntArrayTag extends Tag<int[]> {

    private final int hash;

    private final int[] value;

    public IntArrayTag(int[] value) {
        super(TagType.INT_ARRAY);
        this.hash = Arrays.hashCode(value);
        this.value = Arrays.copyOf(value, value.length);
    }

    @Override
    public int[] getValue() {
        return Arrays.copyOf(value, value.length);
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitIntArray(this.value);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "[I:", "]");
        for (int i : value) {
            joiner.add(String.valueOf(i));
        }
        return joiner.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof IntArrayTag && Arrays.equals(((IntArrayTag) o).value, value);
    }

    @Override
    public int hashCode() {
        return this.hash;
    }
}
