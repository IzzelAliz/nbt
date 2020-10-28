package io.izzel.nbt.util;

import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringJoiner;

public class ImmutableLongs implements Iterable<Long> {
    private static final ImmutableLongs EMPTY = new ImmutableLongs(new long[0], 0, 0);

    private final long[] value;

    private final int longCount;
    private final int offset;

    private int hash;

    private ImmutableLongs(long[] value, int offset, int length) {
        this.longCount = length;
        this.offset = offset;
        this.value = value;
    }

    public int size() {
        return this.longCount;
    }

    public long get(int index) {
        if (index >= 0 && index < this.longCount) {
            return this.value[index + this.offset];
        }
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public Iterator<Long> iterator() {
        return new Iterator<Long>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < ImmutableLongs.this.longCount;
            }

            @Override
            public Long next() {
                return ImmutableLongs.this.get(this.index++);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof ImmutableLongs) {
            int thatLength = ((ImmutableLongs) o).longCount;
            if (thatLength == this.longCount) {
                long[] thatValue = ((ImmutableLongs) o).value;
                if (thatLength == this.value.length && thatLength == thatValue.length) {
                    return Arrays.equals(thatValue, this.value);
                }
                int end = this.offset + this.longCount;
                for (int i = this.offset, j = ((ImmutableLongs) o).offset; i < end; ++i, ++j) {
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
            int hash = 1, end = this.offset + this.longCount;
            for (int i = this.offset; i < end; ++i) {
                hash = hash * 31 + Long.hashCode(this.value[i]);
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
        for (int i = offset, end = offset + longCount; i < end; ++i) {
            joiner.add(Long.toString(value[i]));
        }
        return joiner.toString();
    }

    public long[] toLongArray() {
        return Arrays.copyOfRange(value, offset, offset + longCount);
    }

    public LongBuffer toLongBuffer() {
        return LongBuffer.wrap(value, offset, longCount).slice().asReadOnlyBuffer();
    }

    public static ImmutableLongs empty() {
        return EMPTY;
    }

    public static ImmutableLongs slice(ImmutableLongs longs, int offset, int length) {
        if (longs.longCount < offset + length) {
            throw new IndexOutOfBoundsException("Index: " + (offset + length) + ", Size: " + longs.longCount);
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Index: " + length + ", Size: " + longs.longCount);
        }
        if (offset < 0) {
            throw new IndexOutOfBoundsException("Index: " + offset + ", Size: " + longs.longCount);
        }
        return new ImmutableLongs(longs.value, longs.offset + offset, length);
    }

    public static ImmutableLongs concat(ImmutableLongs first, ImmutableLongs second) {
        return ImmutableLongs.builder()
                .add(first.value, first.offset, first.longCount)
                .add(second.value, second.offset, second.longCount).build();
    }

    public static Builder builder() {
        return new Builder(8);
    }

    public static Builder builder(int initCapacity) {
        return new Builder(initCapacity);
    }

    public static final class Builder {
        private long[] value;
        private int length;

        private Builder(int initCapacity) {
            this.value = new long[initCapacity];
        }

        private long[] growIfNecessary(int size) {
            long[] oldValue = this.value;
            if (oldValue == null) {
                throw new IllegalStateException("this builder has been frozen since build method was called");
            }
            int old = oldValue.length;
            if (this.length > old - size) {
                int diff = Math.max(Math.min(old / 2, 0x7FFFFFF7 - old), Math.min(size, 0x7FFFFFF7 - old));
                long[] newValue = Arrays.copyOf(oldValue, old + diff);
                this.value = newValue;
                return newValue;
            }
            return this.value;
        }

        public ImmutableLongs.Builder add(long b) {
            this.growIfNecessary(1)[this.length++] = b;
            return this;
        }

        public ImmutableLongs.Builder add(long[] longs) {
            return this.add(longs, 0, longs.length);
        }

        public ImmutableLongs.Builder add(long[] longs, int offset, int length) {
            System.arraycopy(longs, offset, this.growIfNecessary(length), this.length, length);
            this.length += length;
            return this;
        }

        public ImmutableLongs.Builder add(LongBuffer buffer) {
            int length = buffer.remaining();
            buffer.duplicate().get(this.growIfNecessary(length), this.length, length);
            this.length += length;
            return this;
        }

        public ImmutableLongs build() {
            if (this.length == 0) {
                return ImmutableLongs.EMPTY;
            }
            long[] value = this.value;
            this.value = null; // make the builder frozen
            return new ImmutableLongs(value, 0, this.length);
        }
    }
}
