package io.izzel.nbt;

import io.izzel.nbt.visitor.TagListVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class ListTag extends Tag<List<Tag<?>>> implements Iterable<Tag<?>> {

    private static final ListTag EMPTY = new ListTag(TagType.END, Collections.emptyList());

    private final List<Tag<?>> value;
    private final TagType tagType;

    private ListTag(TagType type, List<Tag<?>> value) {
        super(TagType.LIST);
        this.tagType = type;
        this.value = value;
    }

    public int size() {
        return this.value.size();
    }

    public Tag<?> get(int index) {
        return this.value.get(index);
    }

    public TagType getTagType() {
        return tagType;
    }

    @Override
    public List<Tag<?>> getValue() {
        return this.value;
    }

    @Override
    public Tag<List<Tag<?>>> copy() {
        return this;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        TagListVisitor listVisitor = visitor.visitList();
        listVisitor.visitType(this.tagType);
        listVisitor.visitLength(this.value.size());
        for (Tag<?> tag : this.value) {
            tag.accept(listVisitor.visitValue());
        }
        listVisitor.visitEnd();
    }

    @Override
    public Iterator<Tag<?>> iterator() {
        return this.value.iterator();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (Tag<?> tag : this.value) {
            joiner.add(tag.toString());
        }
        return joiner.toString();
    }

    public static ListTag empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder(new ArrayList<>(), null);
    }

    public static Builder builder(TagType type) {
        return new Builder(new ArrayList<>(), Objects.requireNonNull(type));
    }

    public static final class Builder {
        private final List<Tag<?>> values;
        private TagType tagType;

        private Builder(List<Tag<?>> values, TagType type) {
            this.values = values;
            this.tagType = type;
        }

        public Builder add(Tag<?> tag) {
            if (tagType == null) {
                tagType = tag.getType();
            } else if (tag.getType() != tagType) {
                throw new IllegalArgumentException("Unmatched tag type (required " + tagType + ")");
            }
            values.add(tag);
            return this;
        }

        public ListTag build() {
            if (values.isEmpty() && (tagType == null || tagType == TagType.END)) {
                return ListTag.EMPTY;
            }
            return new ListTag(tagType, Collections.unmodifiableList(values));
        }
    }
}
