package io.izzel.nbt;

import io.izzel.nbt.util.ImmutableBytes;
import io.izzel.nbt.util.ImmutableInts;
import io.izzel.nbt.util.ImmutableLongs;
import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.regex.Pattern;

public final class CompoundTag extends Tag {

    private static final CompoundTag EMPTY = new CompoundTag(Collections.emptyNavigableMap(), Collections.emptyList());

    private static final Pattern SIMPLE_KEY = Pattern.compile("[A-Za-z0-9._+-]+");

    private final NavigableMap<String, List<Entry<?>>> valueMap;
    private final List<Entry<?>> values;

    private CompoundTag(NavigableMap<String, List<Entry<?>>> entryMap, List<Entry<?>> entries) {
        super(TagType.COMPOUND);
        this.values = Collections.unmodifiableList(entries);
        this.valueMap = Collections.unmodifiableNavigableMap(entryMap);
    }

    public SortedSet<String> keys() {
        return this.valueMap.navigableKeySet();
    }

    public Tag get(String name, Tag fallback) {
        List<? extends Entry<?>> entries = this.getEntries(name);
        return entries.isEmpty() ? fallback : entries.get(entries.size() - 1).getValue();
    }

    public Tag getOrDefault(String name) {
        List<? extends Entry<?>> entries = this.getEntries(name);
        return entries.isEmpty() ? EndTag.of() : entries.get(entries.size() - 1).getValue();
    }

    public Tag get(String name, TagType type, Tag fallback) {
        if (fallback.getType() != type) {
            String msg = "Expected " + type.getTagName() + " but got " + fallback.getType().getTagName();
            throw new IllegalArgumentException(msg);
        }
        Tag tag = this.get(name, fallback);
        return tag.getType() != type ? fallback : tag;
    }

    public Tag getOrDefault(String name, TagType type) {
        Tag tag = this.getOrDefault(name);
        return tag.getType() != type ? type.getDefault() : tag;
    }

    public Number getNumber(String name, Number fallback) {
        Tag tag = this.get(name, EndTag.of());
        switch (tag.getType()) {
            case BYTE:
                return ((ByteTag) tag).getByte();
            case SHORT:
                return ((ShortTag) tag).getShort();
            case INT:
                return ((IntTag) tag).getInt();
            case LONG:
                return ((LongTag) tag).getLong();
            case FLOAT:
                return ((FloatTag) tag).getFloat();
            case DOUBLE:
                return ((DoubleTag) tag).getDouble();
            default:
                return fallback;
        }
    }

    public Number getNumberOrDefault(String name) {
        Tag tag = this.getOrDefault(name);
        switch (tag.getType()) {
            case DOUBLE:
                return ((DoubleTag) tag).getDouble();
            case FLOAT:
                return ((FloatTag) tag).getFloat();
            case LONG:
                return ((LongTag) tag).getLong();
            case INT:
                return ((IntTag) tag).getInt();
            case SHORT:
                return ((ShortTag) tag).getShort();
            case BYTE:
                return ((ByteTag) tag).getByte();
            default:
                return 0;
        }
    }

    public boolean getBoolean(String name, boolean fallback) {
        return this.getNumber(name, fallback ? (byte) 1 : (byte) 0).byteValue() != 0;
    }

    public boolean getBooleanOrDefault(String name) {
        return this.getNumberOrDefault(name).byteValue() != 0;
    }

    public byte getByte(String name, byte fallback) {
        return this.getNumber(name, fallback).byteValue();
    }

    public byte getByteOrDefault(String name) {
        return this.getNumberOrDefault(name).byteValue();
    }

    public short getShort(String name, short fallback) {
        return this.getNumber(name, fallback).shortValue();
    }

    public short getShortOrDefault(String name) {
        return this.getNumberOrDefault(name).shortValue();
    }

    public int getInt(String name, int fallback) {
        return this.getNumber(name, fallback).intValue();
    }

    public int getIntOrDefault(String name) {
        return this.getNumberOrDefault(name).intValue();
    }

    public long getLong(String name, long fallback) {
        return this.getNumber(name, fallback).longValue();
    }

    public long getLongOrDefault(String name) {
        return this.getNumberOrDefault(name).longValue();
    }

    public float getFloat(String name, float fallback) {
        return this.getNumber(name, fallback).floatValue();
    }

    public float getFloatOrDefault(String name) {
        return this.getNumberOrDefault(name).floatValue();
    }

    public double getDouble(String name, double fallback) {
        return this.getNumber(name, fallback).doubleValue();
    }

    public double getDoubleOrDefault(String name) {
        return this.getNumberOrDefault(name).doubleValue();
    }

    public ImmutableBytes getBytes(String name, ImmutableBytes fallback) {
        return ((ByteArrayTag) this.get(name, TagType.BYTE_ARRAY, ByteArrayTag.of(fallback))).getBytes();
    }

    public ImmutableBytes getBytesOrDefault(String name) {
        return ((ByteArrayTag) this.getOrDefault(name, TagType.BYTE_ARRAY)).getBytes();
    }

    public ImmutableInts getInts(String name, ImmutableInts fallback) {
        return ((IntArrayTag) this.get(name, TagType.INT_ARRAY, IntArrayTag.of(fallback))).getInts();
    }

    public ImmutableInts getIntsOrDefault(String name) {
        return ((IntArrayTag) this.getOrDefault(name, TagType.INT_ARRAY)).getInts();
    }

    public ImmutableLongs getLongs(String name, ImmutableLongs fallback) {
        return ((LongArrayTag) this.get(name, TagType.LONG_ARRAY, LongArrayTag.of(fallback))).getLongs();
    }

    public ImmutableLongs getLongsOrDefault(String name) {
        return ((LongArrayTag) this.getOrDefault(name, TagType.LONG_ARRAY)).getLongs();
    }

    public String getString(String name, String fallback) {
        return ((StringTag) this.get(name, TagType.STRING, StringTag.of(fallback))).getString();
    }

    public String getStringOrDefault(String name) {
        return ((StringTag) this.getOrDefault(name, TagType.STRING)).getString();
    }

    public ListTag getList(String name, ListTag fallback) {
        return (ListTag) this.get(name, TagType.LIST, fallback);
    }

    public ListTag getListOrDefault(String name) {
        return (ListTag) this.getOrDefault(name, TagType.LIST);
    }

    public ListTag getList(String name, TagType elemType, ListTag fallback) {
        if (fallback.getElemType() != elemType) {
            String msg = "Expected " + elemType.getTagName() + " but got " + fallback.getElemType().getTagName();
            throw new IllegalArgumentException(msg);
        }
        ListTag tag = (ListTag) this.get(name, TagType.LIST, fallback);
        return tag.getElemType() != elemType ? fallback : tag;
    }

    public ListTag getListOrDefault(String name, TagType elemType) {
        ListTag tag = (ListTag) this.getOrDefault(name, TagType.LIST);
        return tag.getElemType() != elemType ? ListTag.builder(elemType).build() : tag;
    }

    public CompoundTag getCompound(String name, CompoundTag fallback) {
        return (CompoundTag) this.get(name, TagType.COMPOUND, fallback);
    }

    public CompoundTag getCompoundOrDefault(String name) {
        return (CompoundTag) this.getOrDefault(name, TagType.COMPOUND);
    }

    public Entry<?> getEntry(String name, Entry<?> fallback) {
        List<? extends Entry<?>> entries = this.getEntries(name);
        return entries.isEmpty() ? fallback : entries.get(entries.size() - 1);
    }

    public Entry<?> getEntryOrDefault(String name) {
        List<? extends Entry<?>> entries = this.getEntries(name);
        return entries.isEmpty() ? new Entry<>("", EndTag.of()) : entries.get(entries.size() - 1);
    }

    public List<? extends Entry<?>> getEntries(String name) {
        return this.valueMap.getOrDefault(name, Collections.emptyList());
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        TagCompoundVisitor compoundVisitor = visitor.visitCompound();
        for (Entry<?> entry : this.values) {
            entry.getValue().accept(compoundVisitor.visit(entry.getKey()));
        }
        compoundVisitor.visitEnd();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (Entry<?> entry : this.values) {
            String name = entry.getKey();
            joiner.add((SIMPLE_KEY.matcher(name).matches() ? name : StringTag.escape(name)) + ":" + entry.getValue());
        }
        return joiner.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof CompoundTag && this.values.equals(((CompoundTag) o).values);
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    public static <T> Entry<T> entry(String name, Tag tag) {
        return new Entry<>(name, tag);
    }

    public static CompoundTag empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder(false);
    }

    public static Builder builder(boolean allowDuplicate) {
        return new Builder(allowDuplicate);
    }

    public static final class Entry<T> implements Map.Entry<String, Tag> {

        private final String name;
        private final Tag value;

        private Entry(String name, Tag value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.name;
        }

        @Override
        public Tag getValue() {
            return this.value;
        }

        @Override
        public Tag setValue(Tag value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Entry && this.name.equals(((Entry<?>) o).name) && this.value.equals(((Entry<?>) o).value);
        }

        @Override
        public int hashCode() {
            return 31 * this.name.hashCode() + this.value.hashCode();
        }
    }

    public static final class Builder {
        private final NavigableMap<String, List<Entry<?>>> entryMap;
        private final List<Entry<?>> entries;

        private final boolean allowDuplicate;

        private Builder(boolean allowDuplicate) {
            this.entryMap = new TreeMap<>();
            this.entries = new ArrayList<>();
            this.allowDuplicate = allowDuplicate;
        }

        public Builder add(String name, Tag tag) {
            return this.add(new Entry<>(name, tag));
        }

        public Builder add(String name, boolean b) {
            return this.add(name, ByteTag.of(b));
        }

        public Builder add(String name, byte b) {
            return this.add(name, ByteTag.of(b));
        }

        public Builder add(String name, short s) {
            return this.add(name, ShortTag.of(s));
        }

        public Builder add(String name, int i) {
            return this.add(name, IntTag.of(i));
        }

        public Builder add(String name, long l) {
            return this.add(name, LongTag.of(l));
        }

        public Builder add(String name, float f) {
            return this.add(name, FloatTag.of(f));
        }

        public Builder add(String name, double d) {
            return this.add(name, DoubleTag.of(d));
        }

        public Builder add(String name, byte[] bytes) {
            return this.add(name, ByteArrayTag.of(bytes));
        }

        public Builder add(String name, ByteBuffer bytes) {
            return this.add(name, ByteArrayTag.of(bytes));
        }

        public Builder add(String name, ImmutableBytes bytes) {
            return this.add(name, ByteArrayTag.of(bytes));
        }

        public Builder add(String name, int[] ints) {
            return this.add(name, IntArrayTag.of(ints));
        }

        public Builder add(String name, IntBuffer ints) {
            return this.add(name, IntArrayTag.of(ints));
        }

        public Builder add(String name, ImmutableInts ints) {
            return this.add(name, IntArrayTag.of(ints));
        }

        public Builder add(String name, long[] longs) {
            return this.add(name, LongArrayTag.of(longs));
        }

        public Builder add(String name, LongBuffer longs) {
            return this.add(name, LongArrayTag.of(longs));
        }

        public Builder add(String name, ImmutableLongs longs) {
            return this.add(name, LongArrayTag.of(longs));
        }

        public Builder add(String name, String s) {
            return this.add(name, StringTag.of(s));
        }

        public Builder add(Entry<?> entry) {
            String entryName = entry.getKey();
            if (entry.getValue().getType() == TagType.END) {
                String escapedName = SIMPLE_KEY.matcher(entryName).matches() ? entryName : StringTag.escape(entryName);
                throw new IllegalArgumentException("Compound tags do not allow end tag values, name: " + escapedName);
            }
            if (!this.entryMap.containsKey(entryName)) {
                List<Entry<?>> entries = Collections.singletonList(entry);
                this.entryMap.put(entryName, entries);
                this.entries.add(entry);
                return this;
            }
            if (this.allowDuplicate) {
                List<Entry<?>> entries = new ArrayList<>(this.entryMap.get(entryName));
                this.entryMap.put(entryName, Collections.unmodifiableList(entries));
                this.entries.add(entry);
                entries.add(entry);
                return this;
            }
            String escapedName = SIMPLE_KEY.matcher(entryName).matches() ? entryName : StringTag.escape(entryName);
            throw new IllegalArgumentException("Duplicate tag names: " + escapedName);
        }

        public CompoundTag build() {
            if (this.entryMap.isEmpty()) {
                return CompoundTag.EMPTY;
            }
            return new CompoundTag(this.entryMap, this.entries);
        }
    }
}
