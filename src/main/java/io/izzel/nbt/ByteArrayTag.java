package io.izzel.nbt;

import io.izzel.nbt.util.ImmutableBytes;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.nio.ByteBuffer;

public final class ByteArrayTag extends Tag<ImmutableBytes> {

    private final ImmutableBytes value;

    private ByteArrayTag(ImmutableBytes bytes) {
        super(TagType.BYTE_ARRAY);
        this.value = bytes;
    }

    @Override
    public ImmutableBytes getValue() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitByteArray(this.value.toByteArray());
    }

    @Override
    public String toString() {
        return this.value.toString("[B:", ",", "]");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ByteArrayTag && this.value.equals(((ByteArrayTag) o).value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    public static ByteArrayTag of(byte[] bytes) {
        return new ByteArrayTag(ImmutableBytes.builder(bytes.length).add(bytes).build());
    }

    public static ByteArrayTag of(ByteBuffer buffer) {
        return new ByteArrayTag(ImmutableBytes.builder(buffer.remaining()).add(buffer).build());
    }

    public static ByteArrayTag of(ImmutableBytes bytes) {
        return new ByteArrayTag(bytes);
    }
}
