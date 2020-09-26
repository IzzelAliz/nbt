package io.izzel.nbt;

import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class CompoundTag extends Tag<Map<String, Tag<?>>> {

    private final Map<String, Tag<?>> value;
    private String name;

    public CompoundTag() {
        this(Collections.emptyMap());
    }

    public CompoundTag(Map<String, Tag<?>> value) {
        this(value, null);
    }

    public CompoundTag(String name) {
        this(Collections.emptyMap(), name);
    }

    public CompoundTag(Map<String, Tag<?>> value, String name) {
        super(TagType.COMPOUND);
        this.value = new LinkedHashMap<>(value);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void put(String key, Tag<?> tag) {
        this.value.put(key, tag);
    }

    public Tag<?> get(String key) {
        return this.value.get(key);
    }

    public boolean containsKey(String key) {
        return this.value.containsKey(key);
    }

    public void remove(String key) {
        this.value.remove(key);
    }

    @Override
    public Map<String, Tag<?>> getValue() {
        return this.value;
    }

    @Override
    public Tag<Map<String, Tag<?>>> copy() {
        CompoundTag tag = new CompoundTag(this.name);
        for (Map.Entry<String, Tag<?>> entry : this.value.entrySet()) {
            tag.put(entry.getKey(), entry.getValue().copy());
        }
        return tag;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        TagCompoundVisitor compoundVisitor;
        if (this.name != null) {
            compoundVisitor = visitor.visitNamedCompound(this.name);
        } else {
            compoundVisitor = visitor.visitCompound();
        }
        for (Map.Entry<String, Tag<?>> entry : this.value.entrySet()) {
            String key = entry.getKey();
            Tag<?> value = entry.getValue();
            value.accept(compoundVisitor.visit(key));
        }
        compoundVisitor.visitEnd();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (Map.Entry<String, Tag<?>> entry : this.value.entrySet()) {
            joiner.add(entry.getKey() + ":" + entry.getValue());
        }
        return joiner.toString();
    }
}
