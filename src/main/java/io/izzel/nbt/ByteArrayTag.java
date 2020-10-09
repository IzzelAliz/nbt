package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.Arrays;
import java.util.StringJoiner;

public final class ByteArrayTag extends Tag<byte[]> {

    private final byte[] value;

    public ByteArrayTag(byte[] value) {
        super(TagType.BYTE_ARRAY);
        this.value = value;
    }

    @Override
    public byte[] getValue() {
        return this.value;
    }

    @Override
    public Tag<byte[]> copy() {
        return new ByteArrayTag(Arrays.copyOf(this.value, this.value.length));
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitByteArray(this.value);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "[B:", "]");
        for (byte b : value) {
            joiner.add(String.valueOf(b));
        }
        return joiner.toString();
    }
}
