package io.izzel.nbt;

import io.izzel.nbt.visitor.TagListVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class ListTag extends Tag<List<Tag<?>>> implements Iterable<Tag<?>> {

    private final List<Tag<?>> value;
    private TagType tagType;

    public ListTag(TagType tagType, List<Tag<?>> value) {
        super(TagType.LIST);
        Objects.requireNonNull(tagType, "tagType");
        Objects.requireNonNull(value, "value");
        this.tagType = tagType;
        this.value = new ArrayList<>(value);
    }

    public ListTag(TagType tagType) {
        this(tagType, Collections.emptyList());
    }

    public ListTag(List<Tag<?>> value) {
        super(TagType.LIST);
        Objects.requireNonNull(value, "value");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("empty list");
        } else {
            this.tagType = value.get(0).getType();
            this.value = new ArrayList<>(value);
        }
    }

    public boolean add(Tag<?> tag) {
        if (this.tagType.equals(tag.getType())) {
            return this.value.add(tag);
        } else {
            return false;
        }
    }

    public Tag<?> get(int index) {
        return this.value.get(index);
    }

    public void clear() {
        this.value.clear();
    }

    public TagType getTagType() {
        return tagType;
    }

    public void setTagType(TagType tagType) {
        this.tagType = tagType;
        this.value.clear();
    }

    @Override
    public List<Tag<?>> getValue() {
        return this.value;
    }

    @Override
    public Tag<List<Tag<?>>> copy() {
        ListTag tags = new ListTag(this.tagType);
        for (Tag<?> tag : this.value) {
            tags.add(tag.copy());
        }
        return tags;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        TagListVisitor listVisitor = visitor.visitList();
        listVisitor.visitType(this.tagType);
        listVisitor.visitLength(this.value.size());
        int i = 0;
        for (Tag<?> tag : this.value) {
            tag.accept(listVisitor.visitValue(i++));
        }
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
}
