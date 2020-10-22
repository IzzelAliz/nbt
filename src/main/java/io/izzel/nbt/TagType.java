package io.izzel.nbt;

import io.izzel.nbt.util.ImmutableBytes;
import io.izzel.nbt.util.ImmutableInts;
import io.izzel.nbt.util.ImmutableLongs;

import java.util.HashMap;
import java.util.Map;

public enum TagType {
    END(0, "TAG_End", EndTag.of()),
    BYTE(1, "TAG_Byte", ByteTag.of((byte) 0)),
    SHORT(2, "TAG_Short", ShortTag.of((short) 0)),
    INT(3, "TAG_Int", IntTag.of(0)),
    LONG(4, "TAG_Long", LongTag.of(0L)),
    FLOAT(5, "TAG_Float", FloatTag.of(0F)),
    DOUBLE(6, "TAG_Double", DoubleTag.of(0D)),
    BYTE_ARRAY(7, "TAG_Byte_Array", ByteArrayTag.of(ImmutableBytes.empty())),
    STRING(8, "TAG_String", StringTag.of("")),
    LIST(9, "TAG_List", ListTag.empty()),
    COMPOUND(10, "TAG_Compound", CompoundTag.empty()),
    INT_ARRAY(11, "TAG_Int_Array", IntArrayTag.of(ImmutableInts.empty())),
    LONG_ARRAY(12, "TAG_Long_Array", LongArrayTag.of(ImmutableLongs.empty()));

    private static final Map<String, TagType> BY_NAME = new HashMap<>();

    static {
        for (TagType tagType : TagType.values()) {
            BY_NAME.put(tagType.getTagName(), tagType);
        }
    }

    private final int id;
    private final String tagName;
    private final Tag defaultTag;

    TagType(int id, String tagName, Tag defaultTag) {
        this.defaultTag = defaultTag;
        this.tagName = tagName;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTagName() {
        return tagName;
    }

    public Tag getDefault() {
        return defaultTag;
    }

    public static TagType getByName(String tagName) {
        TagType ret = BY_NAME.get(tagName);
        if (ret == null) {
            throw new IllegalArgumentException("Unknown tag type " + tagName);
        }
        return ret;
    }

    public static TagType getById(int tagId) {
        if (tagId < 0 || tagId > 12) {
            throw new IllegalArgumentException("Unknown tag type " + tagId);
        }
        return TagType.values()[tagId];
    }
}
