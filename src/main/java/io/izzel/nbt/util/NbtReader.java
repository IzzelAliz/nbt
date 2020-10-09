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
        byte type = byteBuffer.get();
        String name = nextString();
        this.read(type, visitor, name);
    }

    public CompoundTag readAsTag() {
        TagReader reader = new TagReader();
        this.accept(reader);
        return (CompoundTag) reader.getTag();
    }

    private void read(byte type, TagValueVisitor visitor, String name) {
        switch (type) {
            case 0:
                visitor.visitEnd();
                return;
            case 1:
                visitor.visitByte(byteBuffer.get());
                return;
            case 2:
                visitor.visitShort(byteBuffer.getShort());
                return;
            case 3:
                visitor.visitInt(byteBuffer.getInt());
                return;
            case 4:
                visitor.visitLong(byteBuffer.getLong());
                return;
            case 5:
                visitor.visitFloat(byteBuffer.getFloat());
                return;
            case 6:
                visitor.visitDouble(byteBuffer.getDouble());
                return;
            case 7: {
                int len = byteBuffer.getInt();
                byte[] bytes = new byte[len];
                byteBuffer.get(bytes, 0, len);
                visitor.visitByteArray(bytes);
                return;
            }
            case 8:
                visitor.visitString(this.nextString());
                return;
            case 9: {
                TagListVisitor listVisitor = visitor.visitList();
                byte b = byteBuffer.get();
                listVisitor.visitType(TagType.getById(b));
                int len = byteBuffer.getInt();
                listVisitor.visitLength(len);
                for (int i = 0; i < len; i++) {
                    this.read(b, listVisitor.visitValue(i), null);
                }
                return;
            }
            case 10: {
                TagCompoundVisitor compoundVisitor;
                if (name != null) {
                    compoundVisitor = visitor.visitNamedCompound(name);
                } else {
                    compoundVisitor = visitor.visitCompound();
                }
                while (true) {
                    byte b = byteBuffer.get();
                    if (b == 0) break;
                    String key = nextString();
                    read(b, compoundVisitor.visit(key), null);
                }
                compoundVisitor.visitEnd();
                return;
            }
            case 11: {
                int len = byteBuffer.getInt();
                int[] ints = new int[len];
                for (int i = 0; i < len; i++) {
                    ints[i] = byteBuffer.getInt();
                }
                visitor.visitIntArray(ints);
                return;
            }
            case 12: {
                int len = byteBuffer.getInt();
                long[] longs = new long[len];
                for (int i = 0; i < len; i++) {
                    longs[i] = byteBuffer.getLong();
                }
                visitor.visitLongArray(longs);
                return;
            }
            default: {}
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
}
