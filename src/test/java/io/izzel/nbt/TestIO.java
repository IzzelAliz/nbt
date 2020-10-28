package io.izzel.nbt;

import io.izzel.nbt.util.CompressedNbtReader;
import io.izzel.nbt.util.NbtReader;
import io.izzel.nbt.util.TagReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestIO {

    public static final CompoundTag DUMMY_TAG_DATA = CompoundTag.builder()
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
            .add("ListList", ListTag.builder().add(TestChildren.DUMMY_LIST_TAG).build())
            .add("ListCompound", ListTag.builder().add(TestChildren.DUMMY_COMPOUND_TAG).build())
            .build();

    public static final byte[] DUMMY_DATA = Base64.getUrlDecoder().decode("CgAAAQAHQm9vbGVhbg" +
            "EDAANJbnQAAAAqBAAETG9uZwAAAAAAAAAqAQAEQnl0ZSoCAAVTaG9ydAAqBQAFRmxvYXRCKAAABgAGRG" +
            "91YmxlQEUAAAAAAAAIAAZTdHJpbmcAAjQyCwAFSW50czEAAAADAAAAAQAAAAIAAAADCwAFSW50czIAAA" +
            "ADAAAABAAAAAUAAAAGCwAFSW50czMAAAADAAAABwAAAAgAAAAJBwAGQnl0ZXMxAAAAAwECAwcABkJ5dG" +
            "VzMgAAAAMEBQYHAAZCeXRlczMAAAADBwgJDAAGTG9uZ3MxAAAAAwAAAAAAAAABAAAAAAAAAAIAAAAAAA" +
            "AAAwwABkxvbmdzMgAAAAMAAAAAAAAABAAAAAAAAAAFAAAAAAAAAAYMAAZMb25nczMAAAADAAAAAAAAAA" +
            "cAAAAAAAAACAAAAAAAAAAJCQAETGlzdAMAAAABAAAAKgoACENvbXBvdW5kAwAHVW5rbm93bgAAACoACQ" +
            "ALTGlzdEJvb2xlYW4BAAAAAQEJAAdMaXN0SW50AwAAAAEAAAAqCQAITGlzdExvbmcEAAAAAQAAAAAAAA" +
            "AqCQAITGlzdEJ5dGUBAAAAASoJAAlMaXN0U2hvcnQCAAAAAQAqCQAJTGlzdEZsb2F0BQAAAAFCKAAACQ" +
            "AKTGlzdERvdWJsZQYAAAABQEUAAAAAAAAJAApMaXN0U3RyaW5nCAAAAAEAAjQyCQAJTGlzdEludHMxCw" +
            "AAAAEAAAADAAAAAQAAAAIAAAADCQAJTGlzdEludHMyCwAAAAEAAAADAAAABAAAAAUAAAAGCQAJTGlzdE" +
            "ludHMzCwAAAAEAAAADAAAABwAAAAgAAAAJCQAKTGlzdEJ5dGVzMQcAAAABAAAAAwECAwkACkxpc3RCeX" +
            "RlczIHAAAAAQAAAAMEBQYJAApMaXN0Qnl0ZXMzBwAAAAEAAAADBwgJCQAKTGlzdExvbmdzMQwAAAABAA" +
            "AAAwAAAAAAAAABAAAAAAAAAAIAAAAAAAAAAwkACkxpc3RMb25nczIMAAAAAQAAAAMAAAAAAAAABAAAAA" +
            "AAAAAFAAAAAAAAAAYJAApMaXN0TG9uZ3MzDAAAAAEAAAADAAAAAAAAAAcAAAAAAAAACAAAAAAAAAAJCQ" +
            "AITGlzdExpc3QJAAAAAQMAAAABAAAAKgkADExpc3RDb21wb3VuZAoAAAABAwAHVW5rbm93bgAAACoAAA");

    public static final byte[] DUMMY_COMPRESSED_DATA = Base64.getUrlDecoder().decode("H4sIAAA" +
            "AAAAAAG2SW07DMBBFbxI_4iQLQfkjYQGoPCQk_ioWUEQEFcVGbSrE7pnxI3JLLVmJ71zPeHzcAAX0yrn" +
            "dtLFFherJzgB6AfHs7DvC6AuI1e889SXk-sPtZ_QS8nHnNvPqClBQ9-74uptuH4K_hlrP-y3tL2-GFpK" +
            "SHq5Jr8DlgJL_oz5EXdCUNFXUx6hrnw8wGorPEBIVZZXWPoGQKq39Rl2bDopbWAojFkc8AI8qmYbMJOJ" +
            "Xxq9KpjEzaaRWwzCGbmx7mFOLfYP6zn19u6N9q6Bf7Kd1P5YDMGjZmS6dNxQGmjVqfElgULPElUV29KR" +
            "zr34vCYYFD6b0ziR5QtxGwZgMGhYDKsVq4hUjgVntUxC4mMTDa-MJTghmhiE3LCgzw5gbFqaxcOCqk4P" +
            "g5oFhCRDlPDAuAcIdAwF5lxW7yD13D-fufw8gd4_n7gsvIYCjadibEe1YTM-iCcGTt4E_poTgFZEDAAA");

    public static final String DUMMY_STRING_FORMAT_DATA_WITHOUT_SPACE_CHARACTER = ("{Boolean:" +
            "1b,Int:42,Long:42l,Byte:42b,Short:42s,Float:42.0f,Double:42.0d,String:\"42\",Int" +
            "s1:[I;1,2,3],Ints2:[I;4,5,6],Ints3:[I;7,8,9],Bytes1:[B;1,2,3],Bytes2:[B;4,5,6],B" +
            "ytes3:[B;7,8,9],Longs1:[L;1,2,3],Longs2:[L;4,5,6],Longs3:[L;7,8,9],List:[42],Com" +
            "pound:{Unknown:42},ListBoolean:[1b],ListInt:[42],ListLong:[42l],ListByte:[42b],L" +
            "istShort:[42s],ListFloat:[42.0f],ListDouble:[42.0d],ListString:[\"42\"],ListInts" +
            "1:[[I;1,2,3]],ListInts2:[[I;4,5,6]],ListInts3:[[I;7,8,9]],ListBytes1:[[B;1,2,3]]" +
            ",ListBytes2:[[B;4,5,6]],ListBytes3:[[B;7,8,9]],ListLongs1:[[L;1,2,3]],ListLongs2" +
            ":[[L;4,5,6]],ListLongs3:[[L;7,8,9]],ListList:[[42]],ListCompound:[{Unknown:42}]}");

    public static final String DUMMY_STRING_FORMAT_DATA_AFTER_PRETTIFYING = ("{\n    Boolean:" +
            " 1b,\n    Int: 42,\n    Long: 42l,\n    Byte: 42b,\n    Short: 42s,\n    Float: " +
            "42.0f,\n    Double: 42.0d,\n    String: \"42\",\n    Ints1: [I; 1, 2, 3],\n    I" +
            "nts2: [I; 4, 5, 6],\n    Ints3: [I; 7, 8, 9],\n    Bytes1: [B; 1, 2, 3],\n    By" +
            "tes2: [B; 4, 5, 6],\n    Bytes3: [B; 7, 8, 9],\n    Longs1: [L; 1, 2, 3],\n    L" +
            "ongs2: [L; 4, 5, 6],\n    Longs3: [L; 7, 8, 9],\n    List: [42],\n    Compound: " +
            "{Unknown: 42},\n    ListBoolean: [1b],\n    ListInt: [42],\n    ListLong: [42l]," +
            "\n    ListByte: [42b],\n    ListShort: [42s],\n    ListFloat: [42.0f],\n    List" +
            "Double: [42.0d],\n    ListString: [\"42\"],\n    ListInts1: [[I; 1, 2, 3]],\n   " +
            " ListInts2: [[I; 4, 5, 6]],\n    ListInts3: [[I; 7, 8, 9]],\n    ListBytes1: [[B" +
            "; 1, 2, 3]],\n    ListBytes2: [[B; 4, 5, 6]],\n    ListBytes3: [[B; 7, 8, 9]],\n" +
            "    ListLongs1: [[L; 1, 2, 3]],\n    ListLongs2: [[L; 4, 5, 6]],\n    ListLongs3" +
            ": [[L; 7, 8, 9]],\n    ListList: [[42]],\n    ListCompound: [{Unknown: 42}]\n}\n");

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
        new TagReader(DUMMY_TAG_DATA).toBinaryFile(this.tmpFile);
        assertArrayEquals(Files.readAllBytes(this.tmpFile), DUMMY_DATA);
        assertEquals(new NbtReader(DUMMY_DATA).toCompoundTag(), DUMMY_TAG_DATA);
        assertEquals(new NbtReader(this.tmpFile).toCompoundTag(), DUMMY_TAG_DATA);
        assertArrayEquals(new TagReader(DUMMY_TAG_DATA).toBinaryNbt(), DUMMY_DATA);
        assertEquals(new NbtReader(this.tmpFile).toStringNbt(), DUMMY_STRING_FORMAT_DATA_WITHOUT_SPACE_CHARACTER);
    }

    @Test
    public void testGzipped() throws IOException {
        new TagReader(DUMMY_TAG_DATA).toCompressedBinaryFile(this.tmpFile);
        assertArrayEquals(Files.readAllBytes(this.tmpFile), DUMMY_COMPRESSED_DATA);
        assertEquals(new CompressedNbtReader(this.tmpFile).toCompoundTag(), DUMMY_TAG_DATA);
        assertEquals(new CompressedNbtReader(DUMMY_COMPRESSED_DATA).toCompoundTag(), DUMMY_TAG_DATA);
        assertArrayEquals(new TagReader(DUMMY_TAG_DATA).toCompressedBinaryNbt(), DUMMY_COMPRESSED_DATA);
        assertEquals(new CompressedNbtReader(this.tmpFile).toStringNbt(), DUMMY_STRING_FORMAT_DATA_WITHOUT_SPACE_CHARACTER);
    }

    @Test
    public void testLargeArrays() throws IOException {
        byte[] bytes = new byte[0x7FF7];
        new Random(TestNumber.DUMMY_LONG).nextBytes(bytes);
        CompoundTag compoundTag = CompoundTag.builder()
                .add("Bytes", ByteArrayTag.of(ByteBuffer.wrap(bytes)))
                .add("Ints", IntArrayTag.of(ByteBuffer.wrap(bytes).asIntBuffer()))
                .add("Longs", LongArrayTag.of(ByteBuffer.wrap(bytes).asLongBuffer())).build();

        new TagReader(compoundTag).toBinaryFile(this.tmpFile);
        assertEquals(new NbtReader(this.tmpFile).toCompoundTag(), compoundTag);
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

        new TagReader(compoundTag).toBinaryFile(this.tmpFile);
        return MessageDigest.getInstance("SHA-256").digest(bytes);
    }

    private byte[] newBytesSHA256() throws IOException, GeneralSecurityException {
        CompoundTag compoundTag = new NbtReader(this.tmpFile).toCompoundTag();
        byte[] bytes = compoundTag.getBytesOrDefault("Bytes").toByteArray();

        return MessageDigest.getInstance("SHA-256").digest(bytes);
    }
}
