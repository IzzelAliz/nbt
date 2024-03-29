package io.izzel.nbt;

import io.izzel.nbt.util.ImmutableBytes;
import io.izzel.nbt.util.ImmutableInts;
import io.izzel.nbt.util.ImmutableLongs;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.SortedSet;
import java.util.stream.Stream;

public final class ListTag extends Tag {

    private static final ListTag[] CACHE = Arrays.stream(TagType.values())
            .map(type -> new ListTag(type, Collections.emptyList())).toArray(ListTag[]::new);

    private final List<Tag> values;
    private final TagType elemType;

    private ListTag(TagType type, List<Tag> values) {
        super(TagType.LIST);
        this.elemType = type;
        this.values = Collections.unmodifiableList(values);
    }

    public int size() {
        return this.values.size();
    }

    public Tag get(int index, Tag fallback) {
        return index >= 0 && index < this.size() ? this.values.get(index) : fallback;
    }

    public Tag getOrDefault(int index) {
        return index >= 0 && index < this.size() ? this.values.get(index) : this.elemType.getDefault();
    }

    public Tag get(int index, TagType type, Tag fallback) {
        if (fallback.getType() != type) {
            String msg = "Expected " + type.getTagName() + " but got " + fallback.getType().getTagName();
            throw new IllegalArgumentException(msg);
        }
        return index >= 0 && index < this.size() && this.elemType == type ? this.values.get(index) : fallback;
    }

    public Tag getOrDefault(int index, TagType type) {
        return index >= 0 && index < this.size() && this.elemType == type ? this.values.get(index) : type.getDefault();
    }

    public Number getNumber(int index, Number fallback) {
        if (index >= 0 && index < this.size()) {
            switch (this.elemType) {
                case BYTE:
                    return ((ByteTag) this.values.get(index)).getByte();
                case SHORT:
                    return ((ShortTag) this.values.get(index)).getShort();
                case INT:
                    return ((IntTag) this.values.get(index)).getInt();
                case LONG:
                    return ((LongTag) this.values.get(index)).getLong();
                case FLOAT:
                    return ((FloatTag) this.values.get(index)).getFloat();
                case DOUBLE:
                    return ((DoubleTag) this.values.get(index)).getDouble();
            }
        }
        return fallback;
    }

    public Number getNumberOrDefault(int index) {
        if (index >= 0 && index < this.size()) {
            switch (this.elemType) {
                case DOUBLE:
                    return ((DoubleTag) this.values.get(index)).getDouble();
                case FLOAT:
                    return ((FloatTag) this.values.get(index)).getFloat();
                case LONG:
                    return ((LongTag) this.values.get(index)).getLong();
                case INT:
                    return ((IntTag) this.values.get(index)).getInt();
                case SHORT:
                    return ((ShortTag) this.values.get(index)).getShort();
                case BYTE:
                    return ((ByteTag) this.values.get(index)).getByte();
            }
        }
        return 0;
    }

    public boolean getBoolean(int index, boolean fallback) {
        return this.getNumber(index, fallback ? (byte) 1 : (byte) 0).byteValue() != 0;
    }

    public boolean getBooleanOrDefault(int index) {
        return this.getNumberOrDefault(index).byteValue() != 0;
    }

    public byte getByte(int index, byte fallback) {
        return this.getNumber(index, fallback).byteValue();
    }

    public byte getByteOrDefault(int index) {
        return this.getNumberOrDefault(index).byteValue();
    }

    public short getShort(int index, short fallback) {
        return this.getNumber(index, fallback).shortValue();
    }

    public short getShortOrDefault(int index) {
        return this.getNumberOrDefault(index).shortValue();
    }

    public int getInt(int index, int fallback) {
        return this.getNumber(index, fallback).intValue();
    }

    public int getIntOrDefault(int index) {
        return this.getNumberOrDefault(index).intValue();
    }

    public long getLong(int index, long fallback) {
        return this.getNumber(index, fallback).longValue();
    }

    public long getLongOrDefault(int index) {
        return this.getNumberOrDefault(index).longValue();
    }

    public float getFloat(int index, float fallback) {
        return this.getNumber(index, fallback).floatValue();
    }

    public float getFloatOrDefault(int index) {
        return this.getNumberOrDefault(index).floatValue();
    }

    public double getDouble(int index, double fallback) {
        return this.getNumber(index, fallback).doubleValue();
    }

    public double getDoubleOrDefault(int index) {
        return this.getNumberOrDefault(index).doubleValue();
    }

    public ImmutableBytes getBytes(int index, ImmutableBytes fallback) {
        return ((ByteArrayTag) this.get(index, TagType.BYTE_ARRAY, ByteArrayTag.of(fallback))).getBytes();
    }

    public ImmutableBytes getBytesOrDefault(int index) {
        return ((ByteArrayTag) this.getOrDefault(index, TagType.BYTE_ARRAY)).getBytes();
    }

    public ImmutableInts getInts(int index, ImmutableInts fallback) {
        return ((IntArrayTag) this.get(index, TagType.INT_ARRAY, IntArrayTag.of(fallback))).getInts();
    }

    public ImmutableInts getIntsOrDefault(int index) {
        return ((IntArrayTag) this.getOrDefault(index, TagType.INT_ARRAY)).getInts();
    }

    public ImmutableLongs getLongs(int index, ImmutableLongs fallback) {
        return ((LongArrayTag) this.get(index, TagType.LONG_ARRAY, LongArrayTag.of(fallback))).getLongs();
    }

    public ImmutableLongs getLongsOrDefault(int index) {
        return ((LongArrayTag) this.getOrDefault(index, TagType.LONG_ARRAY)).getLongs();
    }

    public String getString(int index, String fallback) {
        return ((StringTag) this.get(index, TagType.STRING, StringTag.of(fallback))).getString();
    }

    public String getStringOrDefault(int index) {
        return ((StringTag) this.getOrDefault(index, TagType.STRING)).getString();
    }

    public ListTag getList(int index, ListTag fallback) {
        return (ListTag) this.get(index, TagType.LIST, fallback);
    }

    public ListTag getListOrDefault(int index) {
        return (ListTag) this.getOrDefault(index, TagType.LIST);
    }

    public ListTag getList(int index, TagType elemType, ListTag fallback) {
        if (fallback.getElemType() != elemType) {
            String msg = "Expected " + elemType.getTagName() + " but got " + fallback.getElemType().getTagName();
            throw new IllegalArgumentException(msg);
        }
        ListTag tag = (ListTag) this.get(index, TagType.LIST, fallback);
        return tag.getElemType() == elemType ? tag : fallback;
    }

    public ListTag getListOrDefault(int index, TagType elemType) {
        ListTag tag = (ListTag) this.getOrDefault(index, TagType.LIST);
        return tag.getElemType() == elemType ? tag : builder(elemType).build();
    }

    public CompoundTag getCompound(int index, CompoundTag fallback) {
        return (CompoundTag) this.get(index, TagType.COMPOUND, fallback);
    }

    public CompoundTag getCompoundOrDefault(int index) {
        return (CompoundTag) this.getOrDefault(index, TagType.COMPOUND);
    }

    public TagType getElemType() {
        return this.elemType;
    }

    public List<? extends Tag> dump() {
        return this.values;
    }

    public Stream<Tag> stream() {
        return this.values.stream();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof ListTag) {
            Queue<Tag> thisTags = new ArrayDeque<>(Collections.singleton(this));
            Queue<Tag> thatTags = new ArrayDeque<>(Collections.singleton((ListTag) o));
            while (!thisTags.isEmpty()) {
                Tag thisTag = thisTags.remove(), thatTag = thatTags.remove();
                if (thisTag != thatTag) {
                    TagType type = thisTag.getType();
                    if (type != thatTag.getType()) return false;
                    if (type == TagType.LIST) {
                        if (((ListTag) thatTag).size() != ((ListTag) thisTag).size()) return false;
                        thisTags.addAll(((ListTag) thisTag).dump());
                        thatTags.addAll(((ListTag) thatTag).dump());
                        continue;
                    }
                    if (type == TagType.COMPOUND) {
                        SortedSet<String> thisCompoundNames = ((CompoundTag) thisTag).names();
                        if (((CompoundTag) thatTag).names().size() != thisCompoundNames.size()) return false;
                        for (String name : thisCompoundNames) {
                            thisTags.add(((CompoundTag) thisTag).getOrDefault(name));
                            thatTags.add(((CompoundTag) thatTag).getOrDefault(name));
                        }
                        continue;
                    }
                    if (!thatTag.equals(thisTag)) return false;
                }
            }
            return true;
        }
        return false;
    }

    public Builder toBuilder() {
        return new Builder(new ArrayList<>(this.values), this.elemType);
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    public static ListTag empty() {
        return CACHE[TagType.END.ordinal()];
    }

    public static Builder builder() {
        return new Builder(new ArrayList<>(), TagType.END);
    }

    public static Builder builder(TagType type) {
        return new Builder(new ArrayList<>(), Objects.requireNonNull(type));
    }

    public static final class Builder {
        private List<Tag> values;
        private TagType tagType;

        private Builder(List<Tag> values, TagType type) {
            this.values = values;
            this.tagType = type;
        }

        public Builder add(Tag tag) {
            if (tagType == TagType.END) {
                tagType = tag.getType();
            }
            if (tagType == TagType.END) {
                throw new IllegalArgumentException("List tags do not allow end tag values");
            }
            if (tagType != tag.getType()) {
                throw new IllegalArgumentException("Unmatched tag type (required " + tagType + ")");
            }
            if (values == null) {
                throw new IllegalStateException("this builder has been frozen since build method was called");
            }
            values.add(tag);
            return this;
        }

        public Builder add(boolean b) {
            return this.add(ByteTag.of(b));
        }

        public Builder add(byte b) {
            return this.add(ByteTag.of(b));
        }

        public Builder add(short s) {
            return this.add(ShortTag.of(s));
        }

        public Builder add(int i) {
            return this.add(IntTag.of(i));
        }

        public Builder add(long l) {
            return this.add(LongTag.of(l));
        }

        public Builder add(float f) {
            return this.add(FloatTag.of(f));
        }

        public Builder add(double d) {
            return this.add(DoubleTag.of(d));
        }

        public Builder add(byte[] bytes) {
            return this.add(ByteArrayTag.of(bytes));
        }

        public Builder add(ByteBuffer bytes) {
            return this.add(ByteArrayTag.of(bytes));
        }

        public Builder add(ImmutableBytes bytes) {
            return this.add(ByteArrayTag.of(bytes));
        }

        public Builder add(int[] ints) {
            return this.add(IntArrayTag.of(ints));
        }

        public Builder add(IntBuffer ints) {
            return this.add(IntArrayTag.of(ints));
        }

        public Builder add(ImmutableInts ints) {
            return this.add(IntArrayTag.of(ints));
        }

        public Builder add(long[] longs) {
            return this.add(LongArrayTag.of(longs));
        }

        public Builder add(LongBuffer longs) {
            return this.add(LongArrayTag.of(longs));
        }

        public Builder add(ImmutableLongs longs) {
            return this.add(LongArrayTag.of(longs));
        }

        public Builder add(String s) {
            return this.add(StringTag.of(s));
        }

        public Builder remove(int index) {
            if (index < 0) {
                index = this.values.size() - index;
            }
            this.values.remove(index);
            return this;
        }

        public ListTag build() {
            List<Tag> values = this.values;
            if (values.isEmpty()) {
                return ListTag.CACHE[this.tagType.ordinal()];
            }
            this.values = null; // make the builder frozen
            return new ListTag(this.tagType, values);
        }
    }
}
