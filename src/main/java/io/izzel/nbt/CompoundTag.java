package io.izzel.nbt;

import io.izzel.nbt.util.ImmutableBytes;
import io.izzel.nbt.util.ImmutableInts;
import io.izzel.nbt.util.ImmutableLongs;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.regex.Pattern;

public final class CompoundTag extends Tag {

    private static final CompoundTag EMPTY = new CompoundTag(Collections.emptyNavigableMap(), Collections.emptyList());

    private static final Pattern SIMPLE_KEY = Pattern.compile("[A-Za-z0-9._+-]+");

    private final NavigableMap<String, Entry<?>> valueMap;
    private final List<Entry<?>> values;

    private CompoundTag(NavigableMap<String, Entry<?>> entryMap, List<Entry<?>> entries) {
        super(TagType.COMPOUND);
        this.values = Collections.unmodifiableList(entries);
        this.valueMap = Collections.unmodifiableNavigableMap(entryMap);
    }

    public SortedSet<String> names() {
        return this.valueMap.navigableKeySet();
    }

    public Tag get(String name, Tag fallback) {
        return this.valueMap.containsKey(name) ? this.valueMap.get(name).getValue() : fallback;
    }

    public Tag getOrDefault(String name) {
        return this.valueMap.containsKey(name) ? this.valueMap.get(name).getValue() : EndTag.of();
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
        return this.valueMap.getOrDefault(name, fallback);
    }

    public Entry<?> getEntryOrDefault(String name) {
        return this.valueMap.getOrDefault(name, new Entry<>("", EndTag.of()));
    }

    public List<? extends Entry<?>> dump() {
        return this.values;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof CompoundTag) {
            Queue<Tag> thisTags = new ArrayDeque<>(Collections.singleton(this));
            Queue<Tag> thatTags = new ArrayDeque<>(Collections.singleton((CompoundTag) o));
            while (!thisTags.isEmpty()) {
                Tag thisTag = thisTags.remove(), thatTag = thatTags.remove();
                if (thisTag != thatTag) {
                    TagType type = thisTag.getType();
                    if (type != thatTag.getType()) return false;
                    if (type == TagType.COMPOUND) {
                        SortedSet<String> thisCompoundNames = ((CompoundTag) thisTag).names();
                        if (((CompoundTag) thatTag).names().size() != thisCompoundNames.size()) return false;
                        for (String name : thisCompoundNames) {
                            thisTags.add(((CompoundTag) thisTag).getOrDefault(name));
                            thatTags.add(((CompoundTag) thatTag).getOrDefault(name));
                        }
                        continue;
                    }
                    if (type == TagType.LIST) {
                        if (((ListTag) thatTag).size() != ((ListTag) thisTag).size()) return false;
                        thisTags.addAll(((ListTag) thisTag).dump());
                        thatTags.addAll(((ListTag) thatTag).dump());
                        continue;
                    }
                    if (!thatTag.equals(thisTag)) return false;
                }
            }
            return true;
        }
        return false;
    }

    public CompoundTag.Builder toBuilder() {
        return toBuilder(false);
    }

    public CompoundTag.Builder toBuilder(boolean allowDuplicate) {
        return new Builder(this, allowDuplicate);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (String name : this.names()) {
            hash = hash * 31 + this.getEntryOrDefault(name).hashCode();
        }
        return hash;
    }

    public static <T extends Tag> Entry<T> entry(String name, T tag) {
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

    public static final class Entry<T extends Tag> implements Map.Entry<String, T> {

        private final String name;
        private final T value;

        private Entry(String name, T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.name;
        }

        @Override
        public T getValue() {
            return this.value;
        }

        @Override
        public T setValue(T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Entry && this.name.equals(((Entry<?>) o).name) && this.value.equals(((Entry<?>) o).value);
        }

        @Override
        public int hashCode() {
            return this.name.hashCode() ^ this.value.hashCode();
        }
    }

    public static final class Builder {
        private final NavigableMap<String, Entry<?>> entryMap;
        private List<Entry<?>> entries;

        private final boolean allowDuplicate;

        private Builder(boolean allowDuplicate) {
            this.entryMap = new TreeMap<>();
            this.entries = new ArrayList<>();
            this.allowDuplicate = allowDuplicate;
        }

        private Builder(CompoundTag compoundTag, boolean allowDuplicate) {
            this.entryMap = new TreeMap<>(compoundTag.valueMap);
            this.entries = new ArrayList<>(compoundTag.values);
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
            if (this.entries == null) {
                throw new IllegalStateException("this builder has been frozen since build method was called");
            }
            if (!this.entryMap.containsKey(entryName) || this.allowDuplicate) {
                this.entryMap.put(entryName, entry);
                this.entries.add(entry);
                return this;
            }
            String escapedName = SIMPLE_KEY.matcher(entryName).matches() ? entryName : StringTag.escape(entryName);
            throw new IllegalArgumentException("Duplicate tag names: " + escapedName);
        }

        public Builder remove(String key) {
            Entry<?> entry = this.entryMap.remove(key);
            this.entries.remove(entry);
            return this;
        }

        public Builder set(String name, Tag tag) {
            return this.set(new Entry<>(name, tag));
        }

        public Builder set(String name, boolean b) {
            return this.set(name, ByteTag.of(b));
        }

        public Builder set(String name, byte b) {
            return this.set(name, ByteTag.of(b));
        }

        public Builder set(String name, short s) {
            return this.set(name, ShortTag.of(s));
        }

        public Builder set(String name, int i) {
            return this.set(name, IntTag.of(i));
        }

        public Builder set(String name, long l) {
            return this.set(name, LongTag.of(l));
        }

        public Builder set(String name, float f) {
            return this.set(name, FloatTag.of(f));
        }

        public Builder set(String name, double d) {
            return this.set(name, DoubleTag.of(d));
        }

        public Builder set(String name, byte[] bytes) {
            return this.set(name, ByteArrayTag.of(bytes));
        }

        public Builder set(String name, ByteBuffer bytes) {
            return this.set(name, ByteArrayTag.of(bytes));
        }

        public Builder set(String name, ImmutableBytes bytes) {
            return this.set(name, ByteArrayTag.of(bytes));
        }

        public Builder set(String name, int[] ints) {
            return this.set(name, IntArrayTag.of(ints));
        }

        public Builder set(String name, IntBuffer ints) {
            return this.set(name, IntArrayTag.of(ints));
        }

        public Builder set(String name, ImmutableInts ints) {
            return this.set(name, IntArrayTag.of(ints));
        }

        public Builder set(String name, long[] longs) {
            return this.set(name, LongArrayTag.of(longs));
        }

        public Builder set(String name, LongBuffer longs) {
            return this.set(name, LongArrayTag.of(longs));
        }

        public Builder set(String name, ImmutableLongs longs) {
            return this.set(name, LongArrayTag.of(longs));
        }

        public Builder set(String name, String s) {
            return this.set(name, StringTag.of(s));
        }

        public Builder set(Entry<?> entry) {
            String entryName = entry.getKey();
            if (entry.getValue().getType() == TagType.END) {
                String escapedName = SIMPLE_KEY.matcher(entryName).matches() ? entryName : StringTag.escape(entryName);
                throw new IllegalArgumentException("Compound tags do not allow end tag values, name: " + escapedName);
            }
            if (this.entries == null) {
                throw new IllegalStateException("this builder has been frozen since build method was called");
            }
            Entry<?> old = this.entryMap.put(entryName, entry);
            if (old != null) {
                this.entries.replaceAll(it -> it == old ? entry : it);
            } else {
                this.entries.add(entry);
            }
            return this;
        }

        public CompoundTag build() {
            if (this.entryMap.isEmpty()) {
                return CompoundTag.EMPTY;
            }
            final List<Entry<?>> entries = this.entries;
            this.entries = null; // make the builder frozen
            return new CompoundTag(this.entryMap, entries);
        }
    }
}
