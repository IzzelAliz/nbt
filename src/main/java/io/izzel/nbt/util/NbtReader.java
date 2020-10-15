package io.izzel.nbt.util;

import io.izzel.nbt.CompoundTag;
import io.izzel.nbt.TagType;
import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagListVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

public class NbtReader implements Closeable {

    private final DataInputStream data;

    public NbtReader(InputStream stream) {
        this.data = stream instanceof DataInputStream ? (DataInputStream) stream : new DataInputStream(stream);
    }

    public NbtReader(InputStream stream, boolean gzip) throws IOException {
        this(gzip ? new GZIPInputStream(stream) : stream);
    }

    public void accept(TagValueVisitor visitor) throws IOException {
        this.read(new ValueContext(visitor, nextType(), nextString()));
    }

    @Override
    public void close() throws IOException {
        this.data.close();
    }

    public CompoundTag readAsTag() throws IOException {
        TagReader reader = new TagReader();
        this.accept(reader);
        return (CompoundTag) reader.getTag();
    }

    private void read(ValueContext initContext) throws IOException {
        Object[] stack = new Object[16];
        stack[0] = initContext;
        int pointer = 1;
        while (pointer > 0) {
            Object context = stack[--pointer];
            if (context instanceof ValueContext) {
                TagValueVisitor tagVisitor = ((ValueContext) context).tagVisitor;
                switch (((ValueContext) context).tagType) {
                    case END: {
                        tagVisitor.visitEnd();
                        break;
                    }
                    case BYTE: {
                        tagVisitor.visitByte(data.readByte());
                        break;
                    }
                    case SHORT: {
                        tagVisitor.visitShort(data.readShort());
                        break;
                    }
                    case INT: {
                        tagVisitor.visitInt(data.readInt());
                        break;
                    }
                    case LONG: {
                        tagVisitor.visitLong(data.readLong());
                        break;
                    }
                    case FLOAT: {
                        tagVisitor.visitFloat(data.readFloat());
                        break;
                    }
                    case DOUBLE: {
                        tagVisitor.visitDouble(data.readDouble());
                        break;
                    }
                    case BYTE_ARRAY: {
                        int len = data.readInt();
                        if (len >= 0 && len <= 0x7FFFFFF7) {
                            byte[] bytes = new byte[7];
                            int offset = 0;
                            do {
                                bytes = Arrays.copyOf(bytes, Math.min(bytes.length * 2 + 1, len));
                                data.readFully(bytes, offset, bytes.length - offset);
                                offset = bytes.length;
                            } while (offset < len);
                            tagVisitor.visitByteArray(bytes);
                            break;
                        }
                        throw new IOException("Size exceeds " + 0x7FFFFFF7 + ", got " + (len & 0xFFFFFFFFL));
                    }
                    case STRING: {
                        tagVisitor.visitString(this.nextString());
                        break;
                    }
                    case LIST: {
                        TagType tagType = nextType();
                        int len = data.readInt();
                        TagListVisitor visitor = tagVisitor.visitList();
                        stack[pointer++] = new ListContext(len, visitor, tagType);
                        visitor.visitType(tagType);
                        visitor.visitLength(len);
                        break;
                    }
                    case COMPOUND: {
                        String name = ((ValueContext) context).tagName;
                        if (name != null) {
                            stack[pointer++] = new CompoundContext(tagVisitor.visitNamedCompound(name));
                        } else {
                            stack[pointer++] = new CompoundContext(tagVisitor.visitCompound());
                        }
                        break;
                    }
                    case INT_ARRAY: {
                        int len = data.readInt();
                        if (len >= 0 && len <= 0x7FFFFFF7) {
                            int[] ints = new int[7];
                            int offset = 0;
                            do {
                                ints = Arrays.copyOf(ints, Math.min(ints.length * 2 + 1, len));
                                for (int i = offset, size = ints.length; i < size; ++i) {
                                    ints[i] = data.readInt();
                                }
                                offset = ints.length;
                            } while (offset < len);
                            tagVisitor.visitIntArray(ints);
                            break;
                        }
                        throw new IOException("Size exceeds " + 0x7FFFFFF7 + ", got " + (len & 0xFFFFFFFFL));
                    }
                    case LONG_ARRAY: {
                        int len = data.readInt();
                        if (len >= 0 && len <= 0x7FFFFFF7) {
                            long[] longs = new long[7];
                            int offset = 0;
                            do {
                                longs = Arrays.copyOf(longs, Math.min(longs.length * 2 + 1, len));
                                for (int i = offset, size = longs.length; i < size; ++i) {
                                    longs[i] = data.readLong();
                                }
                                offset = longs.length;
                            } while (offset < len);
                            tagVisitor.visitLongArray(longs);
                            break;
                        }
                        throw new IOException("Size exceeds " + 0x7FFFFFF7 + ", got " + (len & 0xFFFFFFFFL));
                    }
                }
            } else if (context instanceof ListContext) {
                TagListVisitor tagVisitor = ((ListContext) context).tagVisitor;
                if (((ListContext) context).leftTagCount.getAndDecrement() <= 0) {
                    tagVisitor.visitEnd();
                } else {
                    if (++pointer >= stack.length) {
                        stack = Arrays.copyOf(stack, stack.length * 2);
                    }
                    stack[pointer++] = new ValueContext(tagVisitor.visitValue(), ((ListContext) context).tagType, null);
                }
            } else if (context instanceof CompoundContext) {
                TagCompoundVisitor tagVisitor = ((CompoundContext) context).tagVisitor;
                TagType tagType = nextType();
                if (tagType == TagType.END) {
                    tagVisitor.visitEnd();
                } else {
                    if (++pointer >= stack.length) {
                        stack = Arrays.copyOf(stack, stack.length * 2);
                    }
                    stack[pointer++] = new ValueContext(tagVisitor.visit(nextString()), tagType, null);
                }
            }
        }
    }

    private TagType nextType() throws IOException {
        try {
            return TagType.getById(data.readByte());
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }
    }

    private String nextString() throws IOException {
        int len = data.readShort() & 0xFFFF;
        byte[] bytes = new byte[len];
        data.readFully(bytes, 0, len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static final class ValueContext {
        private final String tagName;
        private final TagType tagType;
        private final TagValueVisitor tagVisitor;

        private ValueContext(TagValueVisitor tagVisitor, TagType tagType, String tagName) {
            this.tagName = tagName;
            this.tagType = tagType;
            this.tagVisitor = tagVisitor;
        }
    }

    private static final class ListContext {
        private final TagType tagType;
        private final TagListVisitor tagVisitor;
        private final AtomicInteger leftTagCount;

        private ListContext(int tagCount, TagListVisitor tagVisitor, TagType tagType) {
            this.tagType = tagType;
            this.tagVisitor = tagVisitor;
            this.leftTagCount = new AtomicInteger(tagCount);
        }
    }

    private static final class CompoundContext {
        private final TagCompoundVisitor tagVisitor;

        private CompoundContext(TagCompoundVisitor tagVisitor) {
            this.tagVisitor = tagVisitor;
        }
    }
}
