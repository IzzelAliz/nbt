package io.izzel.nbt;

import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.regex.Pattern;

public final class CompoundTag extends Tag<List<CompoundTag.Entry<?>>> {

    private static final CompoundTag EMPTY = new CompoundTag(Collections.emptyNavigableMap(), Collections.emptyList());

    private static final Pattern SIMPLE_KEY = Pattern.compile("[A-Za-z0-9._+-]+");

    private final NavigableMap<String, List<Entry<?>>> entryMap;
    private final List<Entry<?>> entries;

    private CompoundTag(NavigableMap<String, List<Entry<?>>> entryMap, List<Entry<?>> entries) {
        super(TagType.COMPOUND);
        this.entries = Collections.unmodifiableList(entries);
        this.entryMap = Collections.unmodifiableNavigableMap(entryMap);
    }

    public SortedSet<String> keys() {
        return this.entryMap.navigableKeySet();
    }

    public boolean contains(String key) {
        return this.entryMap.containsKey(key);
    }

    public Tag<?> get(String key) {
        List<? extends Entry<?>> entries = this.getEntries(key);
        return entries.get(entries.size() - 1).getValue();
    }

    public Entry<?> getEntry(String key) {
        List<? extends Entry<?>> entries = this.getEntries(key);
        return entries.get(entries.size() - 1);
    }

    public List<? extends Entry<?>> getEntries(String key) {
        return this.entryMap.getOrDefault(key, Collections.emptyList());
    }

    @Override
    public List<Entry<?>> getValue() {
        return this.entries;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        TagCompoundVisitor compoundVisitor = visitor.visitCompound();
        for (Entry<?> entry : this.entries) {
            entry.getValue().accept(compoundVisitor.visit(entry.getKey()));
        }
        compoundVisitor.visitEnd();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (Entry<?> entry : this.entries) {
            String key = entry.getKey();
            joiner.add((SIMPLE_KEY.matcher(key).matches() ? key : StringTag.escape(key)) + ":" + entry.getValue());
        }
        return joiner.toString();
    }

    public static <T> Entry<T> entry(String name, Tag<T> tag) {
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

    public static final class Entry<T> implements Map.Entry<String, Tag<T>> {

        private final String key;
        private final Tag<T> value;

        private Entry(String key, Tag<T> value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public Tag<T> getValue() {
            return this.value;
        }

        @Override
        public Tag<T> setValue(Tag<T> value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Entry && this.key.equals(((Entry<?>) o).key) && this.value.equals(((Entry<?>) o).value);
        }

        @Override
        public int hashCode() {
            return 31 * this.key.hashCode() + this.value.hashCode();
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

        public Builder add(String name, Tag<?> tag) {
            Entry<?> entry = new Entry<>(name, tag);
            if (!this.entryMap.containsKey(name)) {
                List<Entry<?>> entries = Collections.singletonList(entry);
                this.entryMap.put(name, entries);
                this.entries.add(entry);
                return this;
            }
            if (this.allowDuplicate) {
                List<Entry<?>> entries = new ArrayList<>(this.entryMap.get(name));
                this.entryMap.put(name, Collections.unmodifiableList(entries));
                this.entries.add(entry);
                entries.add(entry);
                return this;
            }
            throw new IllegalArgumentException("Duplicate tag name: " + name);
        }

        public CompoundTag build() {
            if (this.entryMap.isEmpty()) {
                return CompoundTag.EMPTY;
            }
            return new CompoundTag(this.entryMap, this.entries);
        }
    }
}
