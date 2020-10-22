package io.izzel.nbt;

import io.izzel.nbt.util.ImmutableBytes;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.nio.ByteBuffer;

public final class ByteArrayTag extends Tag {

    private final ImmutableBytes value;

    private ByteArrayTag(ImmutableBytes bytes) {
        super(TagType.BYTE_ARRAY);
        this.value = bytes;
    }

    public ImmutableBytes getBytes() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitByteArray(this.value);
    }

    @Override
    public String toString() {
        return this.value.toString("[B:", ",", "]");
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof ByteArrayTag && this.value.equals(((ByteArrayTag) o).value);
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
