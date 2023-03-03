package io.izzel.nbt.dfu;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import io.izzel.nbt.ByteArrayTag;
import io.izzel.nbt.ByteTag;
import io.izzel.nbt.CompoundTag;
import io.izzel.nbt.DoubleTag;
import io.izzel.nbt.EndTag;
import io.izzel.nbt.FloatTag;
import io.izzel.nbt.IntArrayTag;
import io.izzel.nbt.IntTag;
import io.izzel.nbt.ListTag;
import io.izzel.nbt.LongArrayTag;
import io.izzel.nbt.LongTag;
import io.izzel.nbt.ShortTag;
import io.izzel.nbt.StringTag;
import io.izzel.nbt.Tag;
import io.izzel.nbt.TagType;
import io.izzel.nbt.util.ImmutableBytes;
import io.izzel.nbt.util.ImmutableInts;
import io.izzel.nbt.util.ImmutableLongs;
import io.izzel.nbt.util.StringNbtWriter;

import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public enum NbtOps implements DynamicOps<Tag> {
    INSTANCE;

    @Override
    public Tag empty() {
        return EndTag.of();
    }

    @Override
    public Tag emptyMap() {
        return CompoundTag.empty();
    }

    @Override
    public Tag emptyList() {
        return ListTag.empty();
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, Tag input) {
        switch (input.getType()) {
            case END:
                return outOps.empty();
            case BYTE:
                return outOps.createByte(((ByteTag) input).getByte());
            case SHORT:
                return outOps.createShort(((ShortTag) input).getShort());
            case INT:
                return outOps.createInt(((IntTag) input).getInt());
            case LONG:
                return outOps.createLong(((LongTag) input).getLong());
            case FLOAT:
                return outOps.createFloat(((FloatTag) input).getFloat());
            case DOUBLE:
                return outOps.createDouble(((DoubleTag) input).getDouble());
            case BYTE_ARRAY:
                return outOps.createByteList(((ByteArrayTag) input).getBytes().toByteBuffer());
            case STRING:
                return outOps.createString(((StringTag) input).getString());
            case LIST:
                return this.convertList(outOps, input);
            case COMPOUND:
                return this.convertMap(outOps, input);
            case INT_ARRAY:
                return outOps.createIntList(((IntArrayTag) input).getInts().stream());
            case LONG_ARRAY:
                return outOps.createLongList(((LongArrayTag) input).getLongs().stream());
            default:
                throw new IllegalArgumentException("Unknown tag: " + input);
        }
    }

    @Override
    public DataResult<Number> getNumberValue(Tag input) {
        switch (input.getType()) {
            case BYTE:
                return DataResult.success(((ByteTag) input).getByte());
            case SHORT:
                return DataResult.success(((ShortTag) input).getShort());
            case INT:
                return DataResult.success(((IntTag) input).getInt());
            case LONG:
                return DataResult.success(((LongTag) input).getLong());
            case FLOAT:
                return DataResult.success(((FloatTag) input).getFloat());
            case DOUBLE:
                return DataResult.success(((DoubleTag) input).getDouble());
            default:
                return DataResult.error("Tag " + input + " is not number");
        }
    }

    @Override
    public Tag createNumeric(Number i) {
        if (i instanceof Byte) {
            return ByteTag.of(i.byteValue());
        } else if (i instanceof Short) {
            return ShortTag.of(i.shortValue());
        } else if (i instanceof Integer) {
            return IntTag.of(i.intValue());
        } else if (i instanceof Long) {
            return LongTag.of(i.longValue());
        } else if (i instanceof Float) {
            return FloatTag.of(i.floatValue());
        } else {
            return DoubleTag.of(i.doubleValue());
        }
    }

    @Override
    public Tag createByte(byte value) {
        return ByteTag.of(value);
    }

    @Override
    public Tag createShort(short value) {
        return ShortTag.of(value);
    }

    @Override
    public Tag createInt(int value) {
        return IntTag.of(value);
    }

    @Override
    public Tag createLong(long value) {
        return LongTag.of(value);
    }

    @Override
    public Tag createFloat(float value) {
        return FloatTag.of(value);
    }

    @Override
    public Tag createDouble(double value) {
        return DoubleTag.of(value);
    }

    @Override
    public DataResult<String> getStringValue(Tag input) {
        if (input instanceof StringTag) {
            return DataResult.success(((StringTag) input).getString());
        } else {
            return DataResult.error("Tag " + input + " is not string");
        }
    }

    @Override
    public Tag createString(String value) {
        return StringTag.of(value);
    }

    @Override
    public DataResult<Tag> mergeToList(Tag list, Tag value) {
        if (list.getType() == TagType.END) {
            return DataResult.success(ListTag.builder().add(value).build());
        } else if (list.getType() == TagType.LIST) {
            return DataResult.success(((ListTag) list).toBuilder().add(value).build());
        } else if (list.getType() == TagType.INT_ARRAY) {
            return DataResult.success(IntArrayTag.of(((IntArrayTag) list).getInts().toBuilder().add(((IntTag) value).getInt()).build()));
        } else if (list.getType() == TagType.LONG_ARRAY) {
            return DataResult.success(LongArrayTag.of(((LongArrayTag) list).getLongs().toBuilder().add(((LongTag) value).getLong()).build()));
        } else if (list.getType() == TagType.BYTE_ARRAY) {
            return DataResult.success(ByteArrayTag.of(((ByteArrayTag) list).getBytes().toBuilder().add(((ByteTag) value).getByte()).build()));
        }
        return DataResult.error("Tag is not a list");
    }

    @Override
    public DataResult<Tag> mergeToList(Tag list, List<Tag> values) {
        if (list.getType() == TagType.END) {
            ListTag.Builder builder = ListTag.builder();
            for (Tag value : values) {
                builder.add(value);
            }
            return DataResult.success(builder.build());
        } else if (list.getType() == TagType.LIST) {
            ListTag.Builder builder = ((ListTag) list).toBuilder();
            for (Tag value : values) {
                builder.add(value);
            }
            return DataResult.success(builder.build());
        } else if (list.getType() == TagType.INT_ARRAY) {
            ImmutableInts.Builder builder = ((IntArrayTag) list).getInts().toBuilder();
            for (Tag value : values) {
                builder.add(((IntTag) value).getInt());
            }
            return DataResult.success(IntArrayTag.of(builder.build()));
        } else if (list.getType() == TagType.LONG_ARRAY) {
            ImmutableLongs.Builder builder = ((LongArrayTag) list).getLongs().toBuilder();
            for (Tag value : values) {
                builder.add(((LongTag) value).getLong());
            }
            return DataResult.success(LongArrayTag.of(builder.build()));
        } else if (list.getType() == TagType.BYTE_ARRAY) {
            ImmutableBytes.Builder builder = ((ByteArrayTag) list).getBytes().toBuilder();
            for (Tag value : values) {
                builder.add(((ByteTag) value).getByte());
            }
            return DataResult.success(ByteArrayTag.of(builder.build()));
        }
        return DataResult.error("Tag is not a list");
    }

    @Override
    public DataResult<Tag> mergeToMap(Tag map, Tag key, Tag value) {
        if (map.getType() != TagType.COMPOUND && map.getType() != TagType.END) {
            return DataResult.error("Tag is not a map");
        }
        if (key.getType() != TagType.STRING) {
            return DataResult.error("Key is not a string");
        }
        CompoundTag.Builder builder = CompoundTag.builder(true);
        if (map instanceof CompoundTag) {
            for (CompoundTag.Entry<?> entry : ((CompoundTag) map).dump()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        builder.add(((StringTag) key).getString(), value);
        return DataResult.success(builder.build());
    }

    @Override
    public DataResult<Tag> mergeToMap(Tag map, MapLike<Tag> values) {
        if (map.getType() != TagType.COMPOUND && map.getType() != TagType.END) {
            return DataResult.error("Tag is not a map");
        }
        CompoundTag.Builder builder = CompoundTag.builder(true);
        if (map instanceof CompoundTag) {
            for (CompoundTag.Entry<?> entry : ((CompoundTag) map).dump()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        Iterator<Pair<Tag, Tag>> iterator = values.entries().iterator();
        while (iterator.hasNext()) {
            Pair<Tag, Tag> pair = iterator.next();
            Tag key = pair.getFirst();
            if (key.getType() != TagType.STRING) {
                return DataResult.error("Key is not a string");
            }
            builder.add(((StringTag) key).getString(), pair.getSecond());
        }
        return DataResult.success(builder.build());
    }

    @Override
    public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag map) {
        if (map.getType() != TagType.COMPOUND && map.getType() != TagType.END) {
            return DataResult.error("Tag is not a map");
        }
        if (map instanceof CompoundTag) {
            return DataResult.success(((CompoundTag) map).dump().stream().map(it -> Pair.of(createString(it.getKey()), it.getValue())));
        } else {
            return DataResult.success(Stream.empty());
        }
    }

    @Override
    public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag map) {
        if (map.getType() != TagType.COMPOUND && map.getType() != TagType.END) {
            return DataResult.error("Tag is not a map");
        }
        if (map instanceof CompoundTag) {
            return DataResult.success(consumer -> {
                for (CompoundTag.Entry<?> entry : ((CompoundTag) map).dump()) {
                    consumer.accept(createString(entry.getKey()), entry.getValue());
                }
            });
        } else {
            return DataResult.success(consumer -> {});
        }
    }

    private String getAsString(Tag tag) {
        if (tag instanceof StringTag) {
            return ((StringTag) tag).getString();
        }
        try {
            StringWriter writer = new StringWriter();
            tag.accept(new StringNbtWriter(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tag createMap(Stream<Pair<Tag, Tag>> map) {
        CompoundTag.Builder builder = CompoundTag.builder(true);
        Iterator<Pair<Tag, Tag>> iterator = map.iterator();
        while (iterator.hasNext()) {
            Pair<Tag, Tag> pair = iterator.next();
            builder.add(getAsString(pair.getFirst()), pair.getSecond());
        }
        return builder.build();
    }

    @Override
    public DataResult<MapLike<Tag>> getMap(Tag input) {
        if (input instanceof CompoundTag) {
            return DataResult.success(MapLike.forMap(((CompoundTag) input).dump().stream()
                    .collect(Collectors.toMap(it -> StringTag.of(it.getKey()), CompoundTag.Entry::getValue)), this));
        }
        return DataResult.error("Tag is not a map");
    }

    @Override
    public Tag createMap(Map<Tag, Tag> map) {
        CompoundTag.Builder builder = CompoundTag.builder(true);
        for (Map.Entry<Tag, Tag> entry : map.entrySet()) {
            builder.add(getAsString(entry.getKey()), entry.getValue());
        }
        return builder.build();
    }

    @Override
    public DataResult<Stream<Tag>> getStream(Tag input) {
        if (input instanceof ListTag) {
            return DataResult.success(((ListTag) input).stream());
        } else if (input instanceof IntArrayTag) {
            return DataResult.success(((IntArrayTag) input).getInts().stream().mapToObj(IntTag::of));
        } else if (input instanceof LongArrayTag) {
            return DataResult.success(((LongArrayTag) input).getLongs().stream().mapToObj(LongTag::of));
        } else if (input instanceof ByteArrayTag) {
            ImmutableBytes bytes = ((ByteArrayTag) input).getBytes();
            return DataResult.success(IntStream.range(0, bytes.size()).mapToObj(i -> ByteTag.of(bytes.get(i))));
        }
        return DataResult.error("Tag is not a list");
    }

    @Override
    public Tag createList(Stream<Tag> input) {
        List<Tag> tags = input.collect(Collectors.toList());
        if (tags.isEmpty()) {
            return ListTag.builder(TagType.BYTE).build();
        } else {
            TagType type = tags.get(0).getType();
            if (type == TagType.BYTE) {
                ImmutableBytes.Builder builder = ImmutableBytes.builder();
                for (Tag tag : tags) {
                    builder.add(((ByteTag) tag).getByte());
                }
                return ByteArrayTag.of(builder.build());
            } else if (type == TagType.INT) {
                return IntArrayTag.of(tags.stream().mapToInt(it -> ((IntTag) it).getInt()).toArray());
            } else if (type == TagType.LONG) {
                return LongArrayTag.of(tags.stream().mapToLong(it -> ((LongTag) it).getLong()).toArray());
            } else if (type == TagType.END) {
                return ListTag.empty();
            } else {
                ListTag.Builder builder = ListTag.builder(type);
                for (Tag tag : tags) {
                    builder.add(tag);
                }
                return builder.build();
            }
        }
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(Tag input) {
        if (input instanceof ByteArrayTag) {
            return DataResult.success(((ByteArrayTag) input).getBytes().toByteBuffer());
        }
        return DynamicOps.super.getByteBuffer(input);
    }

    @Override
    public Tag createByteList(ByteBuffer input) {
        return ByteArrayTag.of(ImmutableBytes.builder().add(input).build());
    }

    @Override
    public DataResult<IntStream> getIntStream(Tag input) {
        if (input instanceof IntArrayTag) {
            return DataResult.success(((IntArrayTag) input).getInts().stream());
        }
        return DynamicOps.super.getIntStream(input);
    }

    @Override
    public Tag createIntList(IntStream input) {
        return IntArrayTag.of(input.toArray());
    }

    @Override
    public DataResult<LongStream> getLongStream(Tag input) {
        if (input instanceof LongArrayTag) {
            return DataResult.success(((LongArrayTag) input).getLongs().stream());
        }
        return DynamicOps.super.getLongStream(input);
    }

    @Override
    public Tag createLongList(LongStream input) {
        return LongArrayTag.of(input.toArray());
    }

    @Override
    public Tag remove(Tag input, String key) {
        if (input instanceof CompoundTag) {
            return ((CompoundTag) input).toBuilder().remove(key).build();
        }
        return input;
    }

    @Override
    public DataResult<Tag> get(Tag input, String key) {
        return DynamicOps.super.get(input, key);
    }

    @Override
    public DataResult<Tag> getGeneric(Tag input, Tag key) {
        return DynamicOps.super.getGeneric(input, key);
    }

    @Override
    public Tag set(Tag input, String key, Tag value) {
        return DynamicOps.super.set(input, key, value);
    }

    @Override
    public Tag update(Tag input, String key, Function<Tag, Tag> function) {
        return DynamicOps.super.update(input, key, function);
    }

    @Override
    public Tag updateGeneric(Tag input, Tag key, Function<Tag, Tag> function) {
        return DynamicOps.super.updateGeneric(input, key, function);
    }

    @Override
    public ListBuilder<Tag> listBuilder() {
        return new ListBuilder<Tag>() {
            private TagType type = TagType.END;
            private ListTag.Builder listBuilder;
            private ImmutableInts.Builder intBuilder;
            private ImmutableLongs.Builder longBuilder;
            private ImmutableBytes.Builder byteBuilder;
            private DataResult<Tag> error = DataResult.success(EndTag.of());

            @Override
            public DynamicOps<Tag> ops() {
                return NbtOps.this;
            }

            @Override
            public DataResult<Tag> build(Tag prefix) {
                if (type == TagType.END) {
                    return DataResult.success(prefix);
                } else if (type == TagType.INT) {
                    return getIntStream(prefix).map(stream ->
                            IntArrayTag.of(ImmutableInts.concat(ImmutableInts.builder().add(stream.toArray()).build(), intBuilder.build()))
                    );
                } else if (type == TagType.LONG) {
                    return getLongStream(prefix).map(stream ->
                            LongArrayTag.of(ImmutableLongs.concat(ImmutableLongs.builder().add(stream.toArray()).build(), longBuilder.build()))
                    );
                } else if (type == TagType.BYTE) {
                    return getByteBuffer(prefix).map(buffer ->
                            ByteArrayTag.of(ImmutableBytes.concat(ImmutableBytes.builder().add(buffer).build(), byteBuilder.build()))
                    );
                } else {
                    return getList(prefix).map(it -> {
                        it.accept(listBuilder::add);
                        return listBuilder;
                    }).map(ListTag.Builder::build);
                }
            }

            @Override
            public ListBuilder<Tag> add(Tag value) {
                if (this.type != value.getType()) {
                    this.type = value.getType();
                    switch (value.getType()) {
                        case INT:
                            intBuilder = ImmutableInts.builder();
                            break;
                        case LONG:
                            longBuilder = ImmutableLongs.builder();
                            break;
                        case BYTE:
                            byteBuilder = ImmutableBytes.builder();
                            break;
                        default:
                            listBuilder = ListTag.builder();
                    }
                }
                switch (this.type) {
                    case INT:
                        if (value instanceof IntTag) {
                            intBuilder.add(((IntTag) value).getInt());
                        }
                        break;
                    case LONG:
                        if (value instanceof LongTag) {
                            longBuilder.add(((LongTag) value).getLong());
                        }
                        break;
                    case BYTE:
                        if (value instanceof ByteTag) {
                            byteBuilder.add(((ByteTag) value).getByte());
                        }
                        break;
                    default:
                        listBuilder.add(value);
                        break;
                }
                return this;
            }

            @Override
            public ListBuilder<Tag> add(DataResult<Tag> value) {
                value.get().ifLeft(this::add).ifRight(it -> this.withErrorsFrom(value));
                return this;
            }

            @Override
            public ListBuilder<Tag> withErrorsFrom(DataResult<?> result) {
                this.error = this.error.flatMap(v -> result.map(r -> v));
                return this;
            }

            @Override
            public ListBuilder<Tag> mapError(UnaryOperator<String> onError) {
                this.error = this.error.mapError(onError);
                return this;
            }
        };
    }

    @Override
    public RecordBuilder<Tag> mapBuilder() {
        return new RecordBuilder<Tag>() {
            private DataResult<CompoundTag.Builder> result = DataResult.success(CompoundTag.builder());

            @Override
            public DynamicOps<Tag> ops() {
                return NbtOps.this;
            }

            @Override
            public RecordBuilder<Tag> add(Tag key, Tag value) {
                this.result = result.map(it -> {
                    it.add(getAsString(key), value);
                    return it;
                });
                return this;
            }

            @Override
            public RecordBuilder<Tag> add(Tag key, DataResult<Tag> value) {
                this.result = this.result.flatMap(it -> value.map(tag -> {
                    it.add(getAsString(key), tag);
                    return it;
                }));
                return this;
            }

            @Override
            public RecordBuilder<Tag> add(DataResult<Tag> key, DataResult<Tag> value) {
                this.result = this.result.flatMap(it -> key.flatMap(str -> value.map(tag -> {
                    it.add(getAsString(str), tag);
                    return it;
                })));
                return this;
            }

            @Override
            public RecordBuilder<Tag> withErrorsFrom(DataResult<?> result) {
                this.result = this.result.flatMap(v -> result.map(r -> v));
                return this;
            }

            @Override
            public RecordBuilder<Tag> setLifecycle(Lifecycle lifecycle) {
                this.result = this.result.setLifecycle(lifecycle);
                return this;
            }

            @Override
            public RecordBuilder<Tag> mapError(UnaryOperator<String> onError) {
                this.result = this.result.mapError(onError);
                return this;
            }

            @Override
            public DataResult<Tag> build(Tag prefix) {
                return this.result.flatMap(it -> getMapValues(prefix).map(stream -> {
                    stream.forEach(pair -> it.add(getAsString(pair.getFirst()), pair.getSecond()));
                    return it;
                })).map(CompoundTag.Builder::build);
            }
        };
    }
}
