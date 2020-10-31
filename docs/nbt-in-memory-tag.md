# In-memory Tag Object

The library provides in-memory object representation of NBT tags. The common base class of tag objects is `io.izzel.nbt.Tag`.

## Types of tag objects

Different subclasses of `io.izzel.nbt.Tag` represent different tag types (`io.izzel.nbt.ByteTag` for bytes, `io.izzel.nbt.ShortTag` for shorts, etc.).

The return value of `getType` method can also indicate the tag type. The return type is `io.izzel.nbt.TagType`, an enum class.

Here is the correlation between these subclasses and tag type objects:

| Name             | Number Id | `io.izzel.nbt.Tag` Subclass  | `io.izzel.nbt.TagType` Instance   |
| ---------------- | --------- | ---------------------------- | --------------------------------- |
| `TAG_End`        | 0         | `io.izzel.nbt.EndTag`        | `io.izzel.nbt.TagType.END`        |
| `TAG_Byte`       | 1         | `io.izzel.nbt.ByteTag`       | `io.izzel.nbt.TagType.BYTE`       |
| `TAG_Short`      | 2         | `io.izzel.nbt.ShortTag`      | `io.izzel.nbt.TagType.SHORT`      |
| `TAG_Int`        | 3         | `io.izzel.nbt.IntTag`        | `io.izzel.nbt.TagType.INT`        |
| `TAG_Long`       | 4         | `io.izzel.nbt.LongTag`       | `io.izzel.nbt.TagType.LONG`       |
| `TAG_Float`      | 5         | `io.izzel.nbt.FloatTag`      | `io.izzel.nbt.TagType.FLOAT`      |
| `TAG_Double`     | 6         | `io.izzel.nbt.DoubleTag`     | `io.izzel.nbt.TagType.DOUBLE`     |
| `TAG_Byte_Array` | 7         | `io.izzel.nbt.ByteArrayTag`  | `io.izzel.nbt.TagType.BYTE_ARRAY` |
| `TAG_String`     | 8         | `io.izzel.nbt.StringTag`     | `io.izzel.nbt.TagType.STRING`     |
| `TAG_List`       | 9         | `io.izzel.nbt.ListTag`       | `io.izzel.nbt.TagType.LIST`       |
| `TAG_Compound`   | 10        | `io.izzel.nbt.CompoundTag`   | `io.izzel.nbt.TagType.COMPOUND`   |
| `TAG_Int_Array`  | 11        | `io.izzel.nbt.IntArrayTag`   | `io.izzel.nbt.TagType.INT_ARRAY`  |
| `TAG_Long_Array` | 12        | `io.izzel.nbt.LongArrayTag`  | `io.izzel.nbt.TagType.LONG_ARRAY` |

## Check and get values

Here is a simple example for checking tag type and get the value:

```java
import io.izzel.nbt.*;

public void handleIntTag(Tag tag) {
    if (tag.getType() == TagType.INT) {
        IntTag intTag = (IntTag) tag;
        int i = intTag.getInt();
        // do other things ...
    }
}
```

`getByte` is a method of byte tags. For other types except end tags, lists, and compounds, there are similar methods:

| `io.izzel.nbt.Tag` Subclass  | Method Name  | Return Type                        |
| ---------------------------- | ------------ | ---------------------------------- |
| `io.izzel.nbt.ByteTag`       | `getByte`    | `byte`                             |
| `io.izzel.nbt.ShortTag`      | `getShort`   | `short`                            |
| `io.izzel.nbt.IntTag`        | `getInt`     | `int`                              |
| `io.izzel.nbt.LongTag`       | `getLong`    | `long`                             |
| `io.izzel.nbt.FloatTag`      | `getFloat`   | `float`                            |
| `io.izzel.nbt.DoubleTag`     | `getDouble`  | `double`                           |
| `io.izzel.nbt.ByteArrayTag`  | `getBytes`   | `io.izzel.nbt.util.ImmutableBytes` |
| `io.izzel.nbt.StringTag`     | `getString`  | `java.lang.String`                 |
| `io.izzel.nbt.IntArrayTag`   | `getInts`    | `io.izzel.nbt.util.ImmutableInts`  |
| `io.izzel.nbt.LongArrayTag`  | `getLongs`   | `io.izzel.nbt.util.ImmutableLongs` |

End tag is singleton so such method is unnecessary. For lists and compounds, please refer [here](#list-and-compound-tags).

## Construct tag objects

Except for lists and compounds, each subclass of `io.izzel.nbt.Tag` has its factory method:

```java
import io.izzel.nbt.*;

public EndTag constructEndTag() {
    return EndTag.of();
}

public IntTag constructIntTag() {
    return IntTag.of(0);
}

// etc.
```

For lists and compounds, please refer [here](#list-and-compound-tags).

You can also construct default instance by tag type:

```java
import io.izzel.nbt.*;

public EndTag constructEndTag() {
    return TagType.END.getDefault() // same as EndTag.of()
}

public IntTag constructIntTag() {
    return TagType.INT.getDefault() // same as IntTag.of(0)
}

// etc.
```

## Immutable arrays

```java
// TODO
```

## List and compound tags

```java
// TODO
```
