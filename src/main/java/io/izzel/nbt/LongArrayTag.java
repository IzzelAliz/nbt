package io.izzel.nbt;

import io.izzel.nbt.util.ImmutableLongs;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.nio.LongBuffer;

public final class LongArrayTag extends Tag {

    private final ImmutableLongs value;

    private LongArrayTag(ImmutableLongs value) {
        super(TagType.LONG_ARRAY);
        this.value = value;
    }

    public ImmutableLongs getLongs() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitLongArray(this.value);
    }

    @Override
    public String toString() {
        return this.value.toString("[L;", ",", "]");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LongArrayTag && this.value.equals(((LongArrayTag) o).value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    public static LongArrayTag of(long[] longs) {
        return new LongArrayTag(ImmutableLongs.builder(longs.length).add(longs).build());
    }

    public static LongArrayTag of(LongBuffer buffer) {
        return new LongArrayTag(ImmutableLongs.builder(buffer.remaining()).add(buffer).build());
    }

    public static LongArrayTag of(ImmutableLongs longs) {
        return new LongArrayTag(longs);
    }
}
