package io.izzel.nbt.util;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringJoiner;

public class ImmutableInts implements Iterable<Integer> {
    private static final ImmutableInts EMPTY = new ImmutableInts(new int[0], 0, 0);

    private final int[] value;

    private final int intCount;
    private final int offset;

    private int hash;

    private ImmutableInts(int[] value, int offset, int length) {
        this.intCount = length;
        this.offset = offset;
        this.value = value;
    }

    public int size() {
        return this.intCount;
    }

    public int get(int index) {
        if (index >= 0 && index < this.intCount) {
            return this.value[index + this.offset];
        }
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < ImmutableInts.this.intCount;
            }

            @Override
            public Integer next() {
                return ImmutableInts.this.get(this.index++);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof ImmutableInts) {
            int thatLength = ((ImmutableInts) o).intCount;
            if (thatLength == this.intCount) {
                int[] thatValue = ((ImmutableInts) o).value;
                if (thatLength == this.value.length && thatLength == thatValue.length) {
                    return Arrays.equals(thatValue, this.value);
                }
                int end = this.offset + this.intCount;
                for (int i = this.offset, j = ((ImmutableInts) o).offset; i < end; ++i, ++j) {
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
            int hash = 1, end = this.offset + this.intCount;
            for (int i = this.offset; i < end; ++i) {
                hash = hash * 31 + Integer.hashCode(this.value[i]);
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
        for (int i = offset, end = offset + intCount; i < end; ++i) {
            joiner.add(Integer.toString(value[i]));
        }
        return joiner.toString();
    }

    public int[] toIntArray() {
        return Arrays.copyOfRange(value, offset, offset + intCount);
    }

    public IntBuffer toIntBuffer() {
        return IntBuffer.wrap(value, offset, intCount).slice().asReadOnlyBuffer();
    }

    public static ImmutableInts empty() {
        return EMPTY;
    }

    public static ImmutableInts slice(ImmutableInts ints, int offset, int length) {
        if (ints.intCount < offset + length) {
            throw new IndexOutOfBoundsException("Index: " + (offset + length) + ", Size: " + ints.intCount);
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Index: " + length + ", Size: " + ints.intCount);
        }
        if (offset < 0) {
            throw new IndexOutOfBoundsException("Index: " + offset + ", Size: " + ints.intCount);
        }
        return new ImmutableInts(ints.value, ints.offset + offset, length);
    }

    public static ImmutableInts concat(ImmutableInts first, ImmutableInts second) {
        return ImmutableInts.builder()
                .add(first.value, first.offset, first.intCount)
                .add(second.value, second.offset, second.intCount).build();
    }

    public static Builder builder() {
        return new Builder(8);
    }

    public static Builder builder(int initCapacity) {
        return new Builder(initCapacity);
    }

    public static final class Builder {
        private int[] value;
        private int length;

        private Builder(int initCapacity) {
            this.value = new int[initCapacity];
        }

        private int[] growIfNecessary(int size) {
            int old = this.value.length;
            if (this.length > old - size) {
                int diff = Math.max(Math.min(old / 2, 0x7FFFFFF7 - old), Math.min(size, 0x7FFFFFF7 - old));
                int[] newValue = Arrays.copyOf(this.value, old + diff);
                this.value = newValue;
                return newValue;
            }
            return this.value;
        }

        public ImmutableInts.Builder add(int b) {
            this.growIfNecessary(1)[this.length++] = b;
            return this;
        }

        public ImmutableInts.Builder add(int[] ints) {
            return this.add(ints, 0, ints.length);
        }

        public ImmutableInts.Builder add(int[] ints, int offset, int length) {
            System.arraycopy(ints, offset, this.growIfNecessary(length), this.length, length);
            this.length += length;
            return this;
        }

        public ImmutableInts.Builder add(IntBuffer buffer) {
            int length = buffer.remaining();
            buffer.duplicate().get(this.growIfNecessary(length), this.length, length);
            this.length += length;
            return this;
        }

        public ImmutableInts build() {
            if (this.length == 0) {
                return ImmutableInts.EMPTY;
            }
            return new ImmutableInts(this.value, 0, this.length);
        }
    }
}
