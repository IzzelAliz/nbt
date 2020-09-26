package io.izzel.nbt;

import java.util.HashMap;
import java.util.Map;

public enum TagType {
    END(0, "TAG_End"),
    BYTE(1, "TAG_Byte"),
    SHORT(2, "TAG_Short"),
    INT(3, "TAG_Int"),
    LONG(4, "TAG_Long"),
    FLOAT(5, "TAG_Float"),
    DOUBLE(6, "TAG_Double"),
    BYTE_ARRAY(7, "TAG_Byte_Array"),
    STRING(8, "TAG_String"),
    LIST(9, "TAG_List"),
    COMPOUND(10, "TAG_Compound"),
    INT_ARRAY(11, "TAG_Int_Array"),
    LONG_ARRAY(12, "TAG_Long_Array");

    private static final Map<String, TagType> BY_NAME = new HashMap<>();

    static {
        for (TagType tagType : TagType.values()) {
            BY_NAME.put(tagType.getTagName(), tagType);
        }
    }

    private final int id;
    private final String tagName;

    TagType(int id, String tagName) {
        this.tagName = tagName;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTagName() {
        return tagName;
    }

    public static TagType getByName(String tagName) {
        TagType ret = BY_NAME.get(tagName);
        if (ret == null) {
            throw new IllegalArgumentException("Unknown tag type " + tagName);
        }
        return ret;
    }
}
