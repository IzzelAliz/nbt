package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.Arrays;
import java.util.StringJoiner;

public final class IntArrayTag extends Tag<int[]> {

    private final int[] value;

    public IntArrayTag(int[] value) {
        super(TagType.INT_ARRAY);
        this.value = value;
    }

    @Override
    public int[] getValue() {
        return this.value;
    }

    @Override
    public Tag<int[]> copy() {
        return new IntArrayTag(Arrays.copyOf(this.value, this.value.length));
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
}
