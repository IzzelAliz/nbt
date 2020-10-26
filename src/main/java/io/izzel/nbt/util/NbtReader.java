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
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

public class NbtReader implements Closeable {

    private final DataInputStream data;
    private final TagType tagType;
    private final String name;

    public NbtReader(InputStream stream) throws IOException {
        this.data = stream instanceof DataInputStream ? (DataInputStream) stream : new DataInputStream(stream);
        this.tagType = nextType();
        this.name = nextString();
    }

    public NbtReader(InputStream stream, boolean gzip) throws IOException {
        this(gzip ? new GZIPInputStream(stream) : stream);
    }

    public void accept(TagValueVisitor visitor) throws IOException {
        this.read(new ValueContext(visitor, this.tagType));
    }

    @Override
    public void close() throws IOException {
        this.data.close();
    }

    public CompoundTag.Entry<?> toCompoundTagEntry() throws IOException {
        TagWriter reader = new TagWriter();
        this.accept(reader);
        return CompoundTag.entry(this.name, reader.getTag());
    }

    public CompoundTag toCompoundTag() throws IOException {
        return (CompoundTag) this.toCompoundTagEntry().getValue();
    }

    public String getName() {
        return this.name;
    }

    private void read(ValueContext initContext) throws IOException {
        Object[] stack = new Object[]{initContext};
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
                        tagVisitor.visitByte(this.data.readByte());
                        break;
                    }
                    case SHORT: {
                        tagVisitor.visitShort(this.data.readShort());
                        break;
                    }
                    case INT: {
                        tagVisitor.visitInt(this.data.readInt());
                        break;
                    }
                    case LONG: {
                        tagVisitor.visitLong(this.data.readLong());
                        break;
                    }
                    case FLOAT: {
                        tagVisitor.visitFloat(this.data.readFloat());
                        break;
                    }
                    case DOUBLE: {
                        tagVisitor.visitDouble(this.data.readDouble());
                        break;
                    }
                    case BYTE_ARRAY: {
                        int len = this.data.readInt();
                        if (len >= 0 && len <= 0x7FFFFFF7) {
                            byte[] bufferArray = new byte[8192];
                            ByteBuffer buffer = ByteBuffer.wrap(bufferArray);
                            ImmutableBytes.Builder builder = ImmutableBytes.builder();
                            for (int bufferLimit = buffer.limit(), offset = 0, bufferStep;
                                 (bufferStep = Math.min(bufferLimit, len - offset)) > 0; offset += bufferStep) {
                                this.data.readFully(bufferArray, 0, 8192 * bufferStep / bufferLimit);
                                buffer.limit(bufferStep);
                                builder.add(buffer);
                                buffer.clear();
                            }
                            tagVisitor.visitByteArray(builder.build());
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
                        int len = this.data.readInt();
                        if (tagType != TagType.END || len <= 0) {
                            TagListVisitor visitor = tagVisitor.visitList();
                            stack[pointer++] = new ListContext(len, visitor, tagType);
                            visitor.visitType(tagType);
                            visitor.visitLength(len);
                            break;
                        }
                        throw new IOException("List tags do not allow end tag values");
                    }
                    case COMPOUND: {
                        stack[pointer++] = new CompoundContext(tagVisitor.visitCompound());
                        break;
                    }
                    case INT_ARRAY: {
                        int len = this.data.readInt();
                        if (len >= 0 && len <= 0x7FFFFFF7) {
                            byte[] bufferArray = new byte[8192];
                            ImmutableInts.Builder builder = ImmutableInts.builder();
                            IntBuffer buffer = ByteBuffer.wrap(bufferArray).asIntBuffer();
                            for (int bufferLimit = buffer.limit(), offset = 0, bufferStep;
                                 (bufferStep = Math.min(bufferLimit, len - offset)) > 0; offset += bufferStep) {
                                this.data.readFully(bufferArray, 0, 8192 * bufferStep / bufferLimit);
                                buffer.limit(bufferStep);
                                builder.add(buffer);
                                buffer.clear();
                            }
                            tagVisitor.visitIntArray(builder.build());
                            break;
                        }
                        throw new IOException("Size exceeds " + 0x7FFFFFF7 + ", got " + (len & 0xFFFFFFFFL));
                    }
                    case LONG_ARRAY: {
                        int len = this.data.readInt();
                        if (len >= 0 && len <= 0x7FFFFFF7) {
                            byte[] bufferArray = new byte[8192];
                            ImmutableLongs.Builder builder = ImmutableLongs.builder();
                            LongBuffer buffer = ByteBuffer.wrap(bufferArray).asLongBuffer();
                            for (int bufferLimit = buffer.limit(), offset = 0, bufferStep;
                                 (bufferStep = Math.min(bufferLimit, len - offset)) > 0; offset += bufferStep) {
                                this.data.readFully(bufferArray, 0, 8192 * bufferStep / bufferLimit);
                                buffer.limit(bufferStep);
                                builder.add(buffer);
                                buffer.clear();
                            }
                            tagVisitor.visitLongArray(builder.build());
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
                        stack = Arrays.copyOf(stack, stack.length * 2 + 1);
                    }
                    stack[pointer++] = new ValueContext(tagVisitor.visitValue(), ((ListContext) context).tagType);
                }
            } else if (context instanceof CompoundContext) {
                TagCompoundVisitor tagVisitor = ((CompoundContext) context).tagVisitor;
                TagType tagType = nextType();
                if (tagType == TagType.END) {
                    tagVisitor.visitEnd();
                } else {
                    if (++pointer >= stack.length) {
                        stack = Arrays.copyOf(stack, stack.length * 2 + 1);
                    }
                    stack[pointer++] = new ValueContext(tagVisitor.visit(nextString()), tagType);
                }
            }
        }
    }

    private TagType nextType() throws IOException {
        try {
            return TagType.getById(this.data.readByte());
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }
    }

    private String nextString() throws IOException {
        int len = this.data.readShort() & 0xFFFF;
        byte[] bytes = new byte[len];
        this.data.readFully(bytes, 0, len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static final class ValueContext {
        private final TagType tagType;
        private final TagValueVisitor tagVisitor;

        private ValueContext(TagValueVisitor tagVisitor, TagType tagType) {
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
