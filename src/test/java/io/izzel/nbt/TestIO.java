package io.izzel.nbt;

import io.izzel.nbt.util.ImmutableBytes;
import io.izzel.nbt.util.NbtReader;
import io.izzel.nbt.util.NbtWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestIO {

    public static final CompoundTag DUMMY_LARGE_TAG = CompoundTag.builder()
            .add("Boolean", true)
            .add("Int", TestNumber.DUMMY_INT)
            .add("Long", TestNumber.DUMMY_LONG)
            .add("Byte", TestNumber.DUMMY_BYTE)
            .add("Short", TestNumber.DUMMY_SHORT)
            .add("Float", TestNumber.DUMMY_FLOAT)
            .add("Double", TestNumber.DUMMY_DOUBLE)
            .add("String", TestString.DUMMY_STRING)
            .add("Ints1", TestArray.DUMMY_INTS)
            .add("Ints2", TestArray.DUMMY_BUFFERED_INTS)
            .add("Ints3", TestArray.DUMMY_IMMUTABLE_INTS)
            .add("Bytes1", TestArray.DUMMY_MUTABLE_BYTES)
            .add("Bytes2", TestArray.DUMMY_BUFFERED_BYTES)
            .add("Bytes3", TestArray.DUMMY_IMMUTABLE_BYTES)
            .add("Longs1", TestArray.DUMMY_MUTABLE_LONGS)
            .add("Longs2", TestArray.DUMMY_BUFFERED_LONGS)
            .add("Longs3", TestArray.DUMMY_IMMUTABLE_LONGS)
            .add("List", TestChildren.DUMMY_LIST_TAG)
            .add("Compound", TestChildren.DUMMY_COMPOUND_TAG)
            .add("ListBoolean", ListTag.builder().add(true).build())
            .add("ListInt", ListTag.builder().add(TestNumber.DUMMY_INT).build())
            .add("ListLong", ListTag.builder().add(TestNumber.DUMMY_LONG).build())
            .add("ListByte", ListTag.builder().add(TestNumber.DUMMY_BYTE).build())
            .add("ListShort", ListTag.builder().add(TestNumber.DUMMY_SHORT).build())
            .add("ListFloat", ListTag.builder().add(TestNumber.DUMMY_FLOAT).build())
            .add("ListDouble", ListTag.builder().add(TestNumber.DUMMY_DOUBLE).build())
            .add("ListString", ListTag.builder().add(TestString.DUMMY_STRING).build())
            .add("ListInts1", ListTag.builder().add(TestArray.DUMMY_INTS).build())
            .add("ListInts2", ListTag.builder().add(TestArray.DUMMY_BUFFERED_INTS).build())
            .add("ListInts3", ListTag.builder().add(TestArray.DUMMY_IMMUTABLE_INTS).build())
            .add("ListBytes1", ListTag.builder().add(TestArray.DUMMY_MUTABLE_BYTES).build())
            .add("ListBytes2", ListTag.builder().add(TestArray.DUMMY_BUFFERED_BYTES).build())
            .add("ListBytes3", ListTag.builder().add(TestArray.DUMMY_IMMUTABLE_BYTES).build())
            .add("ListLongs1", ListTag.builder().add(TestArray.DUMMY_MUTABLE_LONGS).build())
            .add("ListLongs2", ListTag.builder().add(TestArray.DUMMY_BUFFERED_LONGS).build())
            .add("ListLongs3", ListTag.builder().add(TestArray.DUMMY_IMMUTABLE_LONGS).build())
            .add("ListEnd", ListTag.builder().add(TestEnd.DUMMY_END_TAG).build())
            .add("ListList", ListTag.builder().add(TestChildren.DUMMY_LIST_TAG).build())
            .add("ListCompound", ListTag.builder().add(TestChildren.DUMMY_COMPOUND_TAG).build())
            .build();

    public static final byte[] DUMMY_INITIAL = Base64.getDecoder().decode("CgAAAQAHQm9vbGVhbgEDAANJbnQAAAA" +
            "qBAAETG9uZwAAAAAAAAAqAQAEQnl0ZSoCAAVTaG9ydAAqBQAFRmxvYXRCKAAABgAGRG91YmxlQEUAAAAAAAAIAAZTdHJp" +
            "bmcAAjQyCwAFSW50czEAAAADAAAAAQAAAAIAAAADCwAFSW50czIAAAADAAAABAAAAAUAAAAGCwAFSW50czMAAAADAAAAB" +
            "wAAAAgAAAAJBwAGQnl0ZXMxAAAAAwECAwcABkJ5dGVzMgAAAAMEBQYHAAZCeXRlczMAAAADBwgJDAAGTG9uZ3MxAAAAAw" +
            "AAAAAAAAABAAAAAAAAAAIAAAAAAAAAAwwABkxvbmdzMgAAAAMAAAAAAAAABAAAAAAAAAAFAAAAAAAAAAYMAAZMb25nczM" +
            "AAAADAAAAAAAAAAcAAAAAAAAACAAAAAAAAAAJCQAETGlzdAMAAAABAAAAKgoACENvbXBvdW5kAwAHVW5rbm93bgAAACoA" +
            "CQALTGlzdEJvb2xlYW4BAAAAAQEJAAdMaXN0SW50AwAAAAEAAAAqCQAITGlzdExvbmcEAAAAAQAAAAAAAAAqCQAITGlzd" +
            "EJ5dGUBAAAAASoJAAlMaXN0U2hvcnQCAAAAAQAqCQAJTGlzdEZsb2F0BQAAAAFCKAAACQAKTGlzdERvdWJsZQYAAAABQE" +
            "UAAAAAAAAJAApMaXN0U3RyaW5nCAAAAAEAAjQyCQAJTGlzdEludHMxCwAAAAEAAAADAAAAAQAAAAIAAAADCQAJTGlzdEl" +
            "udHMyCwAAAAEAAAADAAAABAAAAAUAAAAGCQAJTGlzdEludHMzCwAAAAEAAAADAAAABwAAAAgAAAAJCQAKTGlzdEJ5dGVz" +
            "MQcAAAABAAAAAwECAwkACkxpc3RCeXRlczIHAAAAAQAAAAMEBQYJAApMaXN0Qnl0ZXMzBwAAAAEAAAADBwgJCQAKTGlzd" +
            "ExvbmdzMQwAAAABAAAAAwAAAAAAAAABAAAAAAAAAAIAAAAAAAAAAwkACkxpc3RMb25nczIMAAAAAQAAAAMAAAAAAAAABA" +
            "AAAAAAAAAFAAAAAAAAAAYJAApMaXN0TG9uZ3MzDAAAAAEAAAADAAAAAAAAAAcAAAAAAAAACAAAAAAAAAAJCQAHTGlzdEV" +
            "uZAAAAAABCQAITGlzdExpc3QJAAAAAQMAAAABAAAAKgkADExpc3RDb21wb3VuZAoAAAABAwAHVW5rbm93bgAAACoAAA==");

    public static final byte[] DUMMY_GZIPPED = Base64.getDecoder().decode("H4sIAAAAAAAAAG2SXU7DMBCEJ4l/4qQ" +
            "HQXkj4QAoUCQk3ioOUNQKKoqN2lSI2+O115FbaslKPDve9fpzAxTQo3P77doWFapnOwHoBMSLs++Ioysgxt9p25WQqw93" +
            "mNBJyKe9W0/jDaCgHt3pbb+9X0Z/DbWaDju/v7zrW0if9Hjr9QpUDijpn/WedeGn9FOxPrCuQz7AaCg6Q0xUlFVahwRCq" +
            "rQOG3VtFlDUwlwYXBx8ABpVMvWZSfBX8lcl05CZNFKrcRjjb2x3nFKLXYP6wX19u5PdVNCv9tO6H0sBGLTkTJdOGwoDTZ" +
            "pvfE5gUJNElUV29KRTr2GvFwwJAUwZnEkKhKiNgjAZNCRGVIrUxIsjkVkdUnhwnCTAa/kEZwQzQ58bZpSZYcgNM1MuHLn" +
            "q5PBw80A/BzzlPDDMAY+bAxH5Iit2lXvu7i/d/x5A7h4u3VdeQmC5tJtQOnH009A6A7wgMb2SJgbPngr+AOUw2MagAwAA");

    public static final List<String> DUMMY_STRING_LIST = Arrays.asList("{Boolean:1b,Int:42,Long:42l,Byte:4" +
            "2b,Short:42s,Float:42.0f,Double:42.0d,String:\"42\",Ints1:[I;1,2,3],Ints2:[I;4,5,6],Ints3:[I;" +
            "7,8,9],Bytes1:[B;1,2,3],Bytes2:[B;4,5,6],Bytes3:[B;7,8,9],Longs1:[L;1,2,3],Longs2:[L;4,5,6],L" +
            "ongs3:[L;7,8,9],List:[42],Compound:{Unknown:42},ListBoolean:[1b],ListInt:[42],ListLong:[42l]," +
            "ListByte:[42b],ListShort:[42s],ListFloat:[42.0f],ListDouble:[42.0d],ListString:[\"42\"],ListI" +
            "nts1:[[I;1,2,3]],ListInts2:[[I;4,5,6]],ListInts3:[[I;7,8,9]],ListBytes1:[[B;1,2,3]],ListBytes" +
            "2:[[B;4,5,6]],ListBytes3:[[B;7,8,9]],ListLongs1:[[L;1,2,3]],ListLongs2:[[L;4,5,6]],ListLongs3" +
            ":[[L;7,8,9]],ListEnd:[],ListList:[[42]],ListCompound:[{Unknown:42}]}", "{\n  Boolean: 1b, \n " +
            " Int: 42, \n  Long: 42l, \n  Byte: 42b, \n  Short: 42s, \n  Float: 42.0f, \n  Double: 42.0d, " +
            "\n  String: \"42\", \n  Ints1: [I; 1, 2, 3], \n  Ints2: [I; 4, 5, 6], \n  Ints3: [I; 7, 8, 9]" +
            ", \n  Bytes1: [B; 1, 2, 3], \n  Bytes2: [B; 4, 5, 6], \n  Bytes3: [B; 7, 8, 9], \n  Longs1: [" +
            "L; 1, 2, 3], \n  Longs2: [L; 4, 5, 6], \n  Longs3: [L; 7, 8, 9], \n  List: [42], \n  Compound" +
            ": {Unknown: 42}, \n  ListBoolean: [1b], \n  ListInt: [42], \n  ListLong: [42l], \n  ListByte:" +
            " [42b], \n  ListShort: [42s], \n  ListFloat: [42.0f], \n  ListDouble: [42.0d], \n  ListString" +
            ": [\"42\"], \n  ListInts1: [[I; 1, 2, 3]], \n  ListInts2: [[I; 4, 5, 6]], \n  ListInts3: [[I;" +
            " 7, 8, 9]], \n  ListBytes1: [[B; 1, 2, 3]], \n  ListBytes2: [[B; 4, 5, 6]], \n  ListBytes3: [" +
            "[B; 7, 8, 9]], \n  ListLongs1: [[L; 1, 2, 3]], \n  ListLongs2: [[L; 4, 5, 6]], \n  ListLongs3" +
            ": [[L; 7, 8, 9]], \n  ListEnd: [], \n  ListList: [[42]], \n  ListCompound: [{Unknown: 42}]\n}");

    private Path tmpFile;

    @Before
    public void beforeTest() throws IOException {
        this.tmpFile = Files.createTempFile("io.izzel.nbt.", ".tmp");
    }

    @After
    public void afterTest() throws IOException {
        Files.delete(this.tmpFile);
    }

    @Test
    public void testInitial() throws IOException {
        try (NbtWriter writer = new NbtWriter(Files.newOutputStream(this.tmpFile))) {
            DUMMY_LARGE_TAG.accept(writer);
            writer.flush();
        }

        assertEquals(ImmutableBytes.builder().add(Files.readAllBytes(this.tmpFile)).build(), ImmutableBytes.builder().add(DUMMY_INITIAL).build());

        try (NbtReader reader = new NbtReader(Files.newInputStream(this.tmpFile))) {
            CompoundTag generated = reader.toCompoundTag();
            assertEquals(DUMMY_LARGE_TAG, generated);
        }
    }

    @Test
    public void testGzipped() throws IOException {
        try (NbtWriter writer = new NbtWriter(Files.newOutputStream(this.tmpFile), true)) {
            DUMMY_LARGE_TAG.accept(writer);
            writer.flush();
        }

        assertArrayEquals(Files.readAllBytes(this.tmpFile), DUMMY_GZIPPED);

        try (NbtReader reader = new NbtReader(Files.newInputStream(this.tmpFile), true)) {
            CompoundTag generated = reader.toCompoundTag();
            assertEquals(DUMMY_LARGE_TAG, generated);
        }
    }

    @Test
    public void testLargeArrays() throws IOException {
        byte[] bytes = new byte[0x7FF7];
        new Random(TestNumber.DUMMY_LONG).nextBytes(bytes);
        CompoundTag compoundTag = CompoundTag.builder()
                .add("Bytes", ByteArrayTag.of(ByteBuffer.wrap(bytes)))
                .add("Ints", IntArrayTag.of(ByteBuffer.wrap(bytes).asIntBuffer()))
                .add("Longs", LongArrayTag.of(ByteBuffer.wrap(bytes).asLongBuffer())).build();

        try (NbtWriter writer = new NbtWriter(Files.newOutputStream(this.tmpFile))) {
            compoundTag.accept(writer);
            writer.flush();
        }

        try (NbtReader reader = new NbtReader(Files.newInputStream(this.tmpFile))) {
            CompoundTag newCompoundTag = reader.toCompoundTag();
            assertEquals(compoundTag, newCompoundTag);
        }
    }

    @Test
    public void testHugeByteArray() throws IOException, GeneralSecurityException {
        byte[] oldSHA256 = oldBytesSHA256(), newSHA256 = newBytesSHA256();
        assertArrayEquals(oldSHA256, newSHA256);
    }

    private byte[] oldBytesSHA256() throws IOException, GeneralSecurityException {
        byte[] bytes = new byte[0x7FFFFFF7];
        new Random(TestNumber.DUMMY_LONG).nextBytes(bytes);
        CompoundTag compoundTag = CompoundTag.builder().add("Bytes", bytes).build();
        try (NbtWriter writer = new NbtWriter(Files.newOutputStream(this.tmpFile))) {
            compoundTag.accept(writer);
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        }
    }

    private byte[] newBytesSHA256() throws IOException, GeneralSecurityException {
        try (NbtReader reader = new NbtReader(Files.newInputStream(this.tmpFile))) {
            byte[] bytes = reader.toCompoundTag().getBytesOrDefault("Bytes").toByteArray();
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        }
    }
}
