package io.izzel.nbt.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringJoiner;

public class ImmutableBytes implements Iterable<Byte> {
    private static final ImmutableBytes EMPTY = new ImmutableBytes(new byte[0], 0, 0);

    private final byte[] value;

    private final int byteCount;
    private final int offset;

    private int hash;

    private ImmutableBytes(byte[] value, int offset, int length) {
        this.byteCount = length;
        this.offset = offset;
        this.value = value;
    }

    public int size() {
        return this.byteCount;
    }

    public byte get(int index) {
        if (index >= 0 && index < this.byteCount) {
            return this.value[index + this.offset];
        }
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < ImmutableBytes.this.byteCount;
            }

            @Override
            public Byte next() {
                return ImmutableBytes.this.get(this.index++);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof ImmutableBytes) {
            int thatLength = ((ImmutableBytes) o).byteCount;
            if (thatLength == this.byteCount) {
                byte[] thatValue = ((ImmutableBytes) o).value;
                if (thatLength == this.value.length && thatLength == thatValue.length) {
                    return Arrays.equals(thatValue, this.value);
                }
                int end = this.offset + this.byteCount;
                for (int i = this.offset, j = ((ImmutableBytes) o).offset; i < end; ++i, ++j) {
                    if (this.value[i] != thatValue[j]) return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.hash == 0) {
            int hash = 1, end = this.offset + this.byteCount;
            for (int i = this.offset; i < end; ++i) {
                hash = hash * 31 + Byte.hashCode(this.value[i]);
            }
            this.hash = hash;
        }
        return this.hash;
    }

    @Override
    public String toString() {
        return this.toString("[", ", ", "]");
    }

    public String toString(String prefix, String delimiter, String suffix) {
        StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
        for (int i = offset, end = offset + byteCount; i < end; ++i) {
            joiner.add(Byte.toString(value[i]));
        }
        return joiner.toString();
    }

    public byte[] toByteArray() {
        return Arrays.copyOfRange(value, offset, offset + byteCount);
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(value, offset, byteCount).slice().asReadOnlyBuffer();
    }

    public static ImmutableBytes empty() {
        return EMPTY;
    }

    public static ImmutableBytes slice(ImmutableBytes bytes, int offset, int length) {
        if (bytes.byteCount < offset + length) {
            throw new IndexOutOfBoundsException("Index: " + (offset + length) + ", Size: " + bytes.byteCount);
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Index: " + length + ", Size: " + bytes.byteCount);
        }
        if (offset < 0) {
            throw new IndexOutOfBoundsException("Index: " + offset + ", Size: " + bytes.byteCount);
        }
        return new ImmutableBytes(bytes.value, bytes.offset + offset, length);
    }

    public static ImmutableBytes concat(ImmutableBytes first, ImmutableBytes second) {
        return ImmutableBytes.builder()
                .add(first.value, first.offset, first.byteCount)
                .add(second.value, second.offset, second.byteCount).build();
    }

    public static Builder builder() {
        return new Builder(8);
    }

    public static Builder builder(int initCapacity) {
        return new Builder(initCapacity);
    }

    public static final class Builder {
        private byte[] value;
        private int length;

        private Builder(int initCapacity) {
            this.value = new byte[initCapacity];
        }

        private byte[] growIfNecessary(int size) {
            int old = this.value.length;
            if (this.length > old - size) {
                int diff = Math.max(Math.min(old / 2, 0x7FFFFFF7 - old), Math.min(size, 0x7FFFFFF7 - old));
                byte[] newValue = Arrays.copyOf(this.value, old + diff);
                this.value = newValue;
                return newValue;
            }
            return this.value;
        }

        public ImmutableBytes.Builder add(byte b) {
            this.growIfNecessary(1)[this.length++] = b;
            return this;
        }

        public ImmutableBytes.Builder add(byte[] bytes) {
            return this.add(bytes, 0, bytes.length);
        }

        public ImmutableBytes.Builder add(byte[] bytes, int offset, int length) {
            System.arraycopy(bytes, offset, this.growIfNecessary(length), this.length, length);
            this.length += length;
            return this;
        }

        public ImmutableBytes.Builder add(ByteBuffer buffer) {
            int length = buffer.remaining();
            buffer.duplicate().get(this.growIfNecessary(length), this.length, length);
            this.length += length;
            return this;
        }

        public ImmutableBytes build() {
            if (this.length == 0) {
                return ImmutableBytes.EMPTY;
            }
            return new ImmutableBytes(this.value, 0, this.length);
        }
    }
}
