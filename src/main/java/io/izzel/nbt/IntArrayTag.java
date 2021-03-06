package io.izzel.nbt;

import io.izzel.nbt.util.ImmutableInts;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.nio.IntBuffer;

public final class IntArrayTag extends Tag {

    private final ImmutableInts value;

    private IntArrayTag(ImmutableInts value) {
        super(TagType.INT_ARRAY);
        this.value = value;
    }

    public ImmutableInts getInts() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitIntArray(this.value);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof IntArrayTag && this.value.equals(((IntArrayTag) o).value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    public static IntArrayTag of(int[] ints) {
        return new IntArrayTag(ImmutableInts.builder(ints.length).add(ints).build());
    }

    public static IntArrayTag of(IntBuffer buffer) {
        return new IntArrayTag(ImmutableInts.builder(buffer.remaining()).add(buffer).build());
    }

    public static IntArrayTag of(ImmutableInts ints) {
        return new IntArrayTag(ints);
    }
}
