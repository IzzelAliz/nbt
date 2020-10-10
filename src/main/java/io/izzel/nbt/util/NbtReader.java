package io.izzel.nbt.util;

import io.izzel.nbt.CompoundTag;
import io.izzel.nbt.TagType;
import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagListVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

public class NbtReader {

    public static void main(String[] args) throws Throwable {
        CompoundTag read = fromGzip(Paths.get("G:\\Minecraft\\.minecraft\\saves\\新的世界\\level.dat")).readAsTag();
        System.out.println(read);
        NbtWriter writer = new NbtWriter(true);
        read.accept(writer);
        Files.write(Paths.get("G:\\Minecraft\\.minecraft\\saves\\新的世界\\level1.dat"), writer.toByteArray());
    }

    private final ByteBuffer byteBuffer;

    public NbtReader(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public NbtReader(byte[] bytes) {
        this.byteBuffer = ByteBuffer.wrap(bytes);
    }

    public void accept(TagValueVisitor visitor) {
        this.read(new ValueContext(visitor, TagType.getById(byteBuffer.get()), nextString()));
    }

    public CompoundTag readAsTag() {
        TagReader reader = new TagReader();
        this.accept(reader);
        return (CompoundTag) reader.getTag();
    }

    private void read(ValueContext initContext) {
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
                        tagVisitor.visitByte(byteBuffer.get());
                        break;
                    }
                    case SHORT: {
                        tagVisitor.visitShort(byteBuffer.getShort());
                        break;
                    }
                    case INT: {
                        tagVisitor.visitInt(byteBuffer.getInt());
                        break;
                    }
                    case LONG: {
                        tagVisitor.visitLong(byteBuffer.getLong());
                        break;
                    }
                    case FLOAT: {
                        tagVisitor.visitFloat(byteBuffer.getFloat());
                        break;
                    }
                    case DOUBLE: {
                        tagVisitor.visitDouble(byteBuffer.getDouble());
                        break;
                    }
                    case BYTE_ARRAY: {
                        int len = byteBuffer.getInt();
                        byte[] bytes = new byte[len];
                        byteBuffer.get(bytes, 0, len);
                        tagVisitor.visitByteArray(bytes);
                        break;
                    }
                    case STRING: {
                        tagVisitor.visitString(this.nextString());
                        break;
                    }
                    case LIST: {
                        TagType tagType = TagType.getById(byteBuffer.get());
                        int len = byteBuffer.getInt();
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
                        int len = byteBuffer.getInt();
                        int[] ints = new int[len];
                        byteBuffer.asIntBuffer().get(ints, 0, len);
                        tagVisitor.visitIntArray(ints);
                        break;
                    }
                    case LONG_ARRAY: {
                        int len = byteBuffer.getInt();
                        long[] longs = new long[len];
                        byteBuffer.asLongBuffer().get(longs, 0, len);
                        tagVisitor.visitLongArray(longs);
                        break;
                    }
                }
            } else if (context instanceof ListContext) {
                TagListVisitor tagVisitor = ((ListContext) context).tagVisitor;
                if (((ListContext) context).leftTagCount.getAndDecrement() > 0) {
                    stack[pointer++] = context;
                    if (pointer >= stack.length) {
                        stack = Arrays.copyOf(stack, stack.length * 2);
                    }
                    stack[pointer++] = new ValueContext(tagVisitor.visitValue(), ((ListContext) context).tagType, null);
                }
            } else if (context instanceof CompoundContext) {
                TagCompoundVisitor tagVisitor = ((CompoundContext) context).tagVisitor;
                TagType tagType = TagType.getById(byteBuffer.get());
                if (tagType == TagType.END) {
                    tagVisitor.visitEnd();
                } else {
                    stack[pointer++] = context;
                    if (pointer >= stack.length) {
                        stack = Arrays.copyOf(stack, stack.length * 2);
                    }
                    stack[pointer++] = new ValueContext(tagVisitor.visit(nextString()), tagType, null);
                }
            }
        }
    }

    private String nextString() {
        int len = byteBuffer.getShort() & 0xFFFF;
        byte[] bytes = new byte[len];
        byteBuffer.get(bytes, 0, len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static NbtReader from(Path path) throws IOException {
        return new NbtReader(Files.readAllBytes(path));
    }

    public static NbtReader fromGzip(Path path) throws IOException {
        try (InputStream in = new GZIPInputStream(Files.newInputStream(path));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            return new NbtReader(out.toByteArray());
        }
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
