package io.izzel.nbt;

import io.izzel.nbt.util.ImmutableBytes;
import io.izzel.nbt.util.ImmutableInts;
import io.izzel.nbt.util.ImmutableLongs;
import io.izzel.nbt.util.NbtReader;
import io.izzel.nbt.util.NbtWriter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;

import static org.junit.Assert.*;

public class NbtTest {

    private static final int DUMMY_INT = 42;
    private static final long DUMMY_LONG = 42L;
    private static final float DUMMY_FLOAT = 42.0F;
    private static final double DUMMY_DOUBLE = 42.0;
    private static final byte DUMMY_BYTE = (byte) DUMMY_INT;
    private static final short DUMMY_SHORT = (short) DUMMY_INT;

    private static final String DUMMY_STRING = "42";
    private static final int[] DUMMY_INTS = {1, 2, 3};
    private static final byte[] DUMMY_BYTES = {1, 2, 3};
    private static final long[] DUMMY_LONGS = {1, 2, 3};
    private static final IntBuffer DUMMY_BUFFERED_INTS = IntBuffer.wrap(new int[]{4, 5, 6});
    private static final ByteBuffer DUMMY_BUFFERED_BYTES = ByteBuffer.wrap(new byte[]{4, 5, 6});
    private static final LongBuffer DUMMY_BUFFERED_LONGS = LongBuffer.wrap(new long[]{4, 5, 6});
    private static final ImmutableInts DUMMY_IMMUTABLE_INTS = ImmutableInts.builder().add(new int[]{7, 8, 9}).build();
    private static final ImmutableBytes DUMMY_IMMUTABLE_BYTES = ImmutableBytes.builder().add(new byte[]{7, 8, 9}).build();
    private static final ImmutableLongs DUMMY_IMMUTABLE_LONGS = ImmutableLongs.builder().add(new long[]{7, 8, 9}).build();

    private static final EndTag DUMMY_END_TAG = EndTag.of();
    private static final ByteTag DUMMY_TRUE_TAG = ByteTag.of(true);
    private static final ByteTag DUMMY_FALSE_TAG = ByteTag.of(false);
    private static final IntTag DUMMY_INT_TAG = IntTag.of(DUMMY_INT);
    private static final LongTag DUMMY_LONG_TAG = LongTag.of(DUMMY_LONG);
    private static final ByteTag DUMMY_BYTE_TAG = ByteTag.of(DUMMY_BYTE);
    private static final ShortTag DUMMY_SHORT_TAG = ShortTag.of(DUMMY_SHORT);
    private static final FloatTag DUMMY_FLOAT_TAG = FloatTag.of(DUMMY_FLOAT);
    private static final DoubleTag DUMMY_DOUBLE_TAG = DoubleTag.of(DUMMY_DOUBLE);
    private static final ListTag DUMMY_LIST_TAG = ListTag.builder().add(DUMMY_INT).build();
    private static final CompoundTag DUMMY_COMPOUND_TAG = CompoundTag.builder().add("Unknown", DUMMY_INT).build();

    public static final CompoundTag DUMMY_BIG_LIST_COMPOUND_TAG = CompoundTag.builder()
            .add("ListBoolean", ListTag.builder().add(true).build())
            .add("ListEnd", ListTag.builder().add(DUMMY_END_TAG).build())
            .add("ListInt", ListTag.builder().add(DUMMY_INT).build())
            .add("ListLong", ListTag.builder().add(DUMMY_LONG).build())
            .add("ListByte", ListTag.builder().add(DUMMY_BYTE).build())
            .add("ListShort", ListTag.builder().add(DUMMY_SHORT).build())
            .add("ListFloat", ListTag.builder().add(DUMMY_FLOAT).build())
            .add("ListDouble", ListTag.builder().add(DUMMY_DOUBLE).build())
            .add("ListString", ListTag.builder().add(DUMMY_STRING).build())
            .add("ListInts1", ListTag.builder().add(DUMMY_INTS).build())
            .add("ListInts2", ListTag.builder().add(DUMMY_BUFFERED_INTS).build())
            .add("ListInts3", ListTag.builder().add(DUMMY_IMMUTABLE_INTS).build())
            .add("ListBytes1", ListTag.builder().add(DUMMY_BYTES).build())
            .add("ListBytes2", ListTag.builder().add(DUMMY_BUFFERED_BYTES).build())
            .add("ListBytes3", ListTag.builder().add(DUMMY_IMMUTABLE_BYTES).build())
            .add("ListLongs1", ListTag.builder().add(DUMMY_LONGS).build())
            .add("ListLongs2", ListTag.builder().add(DUMMY_BUFFERED_LONGS).build())
            .add("ListLongs3", ListTag.builder().add(DUMMY_IMMUTABLE_LONGS).build())
            .add("ListList", ListTag.builder().add(DUMMY_LIST_TAG).build())
            .add("ListCompound", ListTag.builder().add(DUMMY_COMPOUND_TAG).build())
            .build();

    public static final CompoundTag DUMMY_BIT_COMPOUND_TAG = CompoundTag.builder()
            .add("Boolean", true)
            .add("End", DUMMY_END_TAG)
            .add("Int", DUMMY_INT)
            .add("Long", DUMMY_LONG)
            .add("Byte", DUMMY_BYTE)
            .add("Short", DUMMY_SHORT)
            .add("Float", DUMMY_FLOAT)
            .add("Double", DUMMY_DOUBLE)
            .add("String", DUMMY_STRING)
            .add("Ints1", DUMMY_INTS)
            .add("Ints2", DUMMY_BUFFERED_INTS)
            .add("Ints3", DUMMY_IMMUTABLE_INTS)
            .add("Bytes1", DUMMY_BYTES)
            .add("Bytes2", DUMMY_BUFFERED_BYTES)
            .add("Bytes3", DUMMY_IMMUTABLE_BYTES)
            .add("Longs1", DUMMY_LONGS)
            .add("Longs2", DUMMY_BUFFERED_LONGS)
            .add("Longs3", DUMMY_IMMUTABLE_LONGS)
            .add("List", DUMMY_LIST_TAG)
            .add("Compound", DUMMY_COMPOUND_TAG)
            .build();

    @Test
    public void testNumber() {
        assertEquals(DUMMY_INT_TAG.getInt(), DUMMY_INT);
        assertEquals(DUMMY_LONG_TAG.getLong(), DUMMY_LONG);
        assertEquals(DUMMY_BYTE_TAG.getByte(), DUMMY_BYTE);
        assertEquals(DUMMY_SHORT_TAG.getShort(), DUMMY_SHORT);
        assertEquals(DUMMY_FLOAT_TAG.getFloat(), DUMMY_FLOAT, 0.0F);
        assertEquals(DUMMY_DOUBLE_TAG.getDouble(), DUMMY_DOUBLE, 0.0);
    }

    @Test
    public void testBoolean() {
        assertTrue(DUMMY_TRUE_TAG.getBoolean());
        assertFalse(DUMMY_FALSE_TAG.getBoolean());
        assertEquals(DUMMY_TRUE_TAG.getByte(), (byte) 1);
        assertEquals(DUMMY_FALSE_TAG.getByte(), (byte) 0);
        assertTrue(ByteTag.of((byte) 1).getBoolean());
        assertFalse(ByteTag.of((byte) 0).getBoolean());
    }

    @Test
    public void testFloatingComparison() {
        assertNotEquals(FloatTag.of(0.0F), FloatTag.of(-0.0F));
        assertNotEquals(DoubleTag.of(0.0D), DoubleTag.of(-0.0D));
        assertEquals(FloatTag.of(Float.NaN), FloatTag.of(Float.NaN));
        assertEquals(DoubleTag.of(Double.NaN), DoubleTag.of(Double.NaN));
        assertEquals(DUMMY_FLOAT_TAG, FloatTag.of(FloatTag.of(21.0F).getFloat() * 2.0F));
        assertEquals(DUMMY_DOUBLE_TAG, DoubleTag.of(DoubleTag.of(21.0).getDouble() * 2.0));
    }

    @Test
    public void testCache() {
        assertSame(DUMMY_END_TAG, DUMMY_END_TAG);
        assertSame(DUMMY_INT_TAG, DUMMY_INT_TAG);
        assertSame(DUMMY_LONG_TAG, DUMMY_LONG_TAG);
        assertSame(DUMMY_BYTE_TAG, DUMMY_BYTE_TAG);
        assertSame(DUMMY_SHORT_TAG, DUMMY_SHORT_TAG);

        assertNotSame(IntTag.of(DUMMY_INT * DUMMY_INT), IntTag.of(DUMMY_INT * DUMMY_INT));
        assertNotSame(IntTag.of(DUMMY_INT * -DUMMY_INT), IntTag.of(DUMMY_INT * -DUMMY_INT));
        assertNotSame(LongTag.of(DUMMY_LONG * DUMMY_LONG), LongTag.of(DUMMY_LONG * DUMMY_LONG));
        assertNotSame(LongTag.of(DUMMY_LONG * -DUMMY_LONG), LongTag.of(DUMMY_LONG * -DUMMY_LONG));
        assertNotSame(ShortTag.of((short) (DUMMY_INT * -DUMMY_INT)), ShortTag.of((short) (DUMMY_INT * -DUMMY_INT)));
        assertNotSame(ShortTag.of((short) (DUMMY_INT * -DUMMY_INT)), ShortTag.of((short) (DUMMY_INT * -DUMMY_INT)));
    }

    @Test
    public void testEscapeString() {
        assertEquals(StringTag.of("").toString(), "\"\"");
        assertEquals(StringTag.of("'").toString(), "\"'\"");
        assertEquals(StringTag.of("\"").toString(), "'\"'");
        assertEquals(StringTag.of("\\").toString(), "\"\\\\\"");
        assertEquals(StringTag.of("\"'").toString(), "'\"\\''");
        assertEquals(StringTag.of("'\"").toString(), "\"'\\\"\"");
        assertEquals(StringTag.of("\\\\").toString(), "\"\\\\\\\\\"");
    }

    @Test
    public void testToString() {
        assertEquals(DUMMY_END_TAG.toString(), "");
        assertEquals(DUMMY_INT_TAG.toString(), "42");
        assertEquals(DUMMY_LONG_TAG.toString(), "42l");
        assertEquals(DUMMY_BYTE_TAG.toString(), "42b");
        assertEquals(DUMMY_SHORT_TAG.toString(), "42s");
        assertEquals(DUMMY_FLOAT_TAG.toString(), "42.0f");
        assertEquals(DUMMY_DOUBLE_TAG.toString(), "42.0d");
        assertEquals(DUMMY_LIST_TAG.toString(), "[42]");
        assertEquals(DUMMY_COMPOUND_TAG.toString(), "{Unknown:42}");
    }

    @Test
    public void testList() {
        CompoundTag tag = DUMMY_BIG_LIST_COMPOUND_TAG;
        assertTrue(tag.getListOrDefault("ListBoolean").getBoolean(1, true));
        assertTrue(tag.getListOrDefault("ListBoolean").getBooleanOrDefault(0));
        assertEquals(tag.getListOrDefault("ListEnd").get(1, DUMMY_END_TAG), DUMMY_END_TAG);
        assertEquals(tag.getListOrDefault("ListEnd").getOrDefault(0), DUMMY_END_TAG);
        assertEquals(tag.getListOrDefault("ListInt").getInt(1, DUMMY_INT), DUMMY_INT);
        assertEquals(tag.getListOrDefault("ListInt").getIntOrDefault(0), DUMMY_INT);
        assertEquals(tag.getListOrDefault("ListLong").getLong(1, DUMMY_LONG), DUMMY_LONG);
        assertEquals(tag.getListOrDefault("ListLong").getLongOrDefault(0), DUMMY_LONG);
        assertEquals(tag.getListOrDefault("ListByte").getByte(1, DUMMY_BYTE), DUMMY_BYTE);
        assertEquals(tag.getListOrDefault("ListByte").getByteOrDefault(0), DUMMY_BYTE);
        assertEquals(tag.getListOrDefault("ListShort").getShort(1, DUMMY_SHORT), DUMMY_SHORT);
        assertEquals(tag.getListOrDefault("ListShort").getShortOrDefault(0), DUMMY_SHORT);
        assertEquals(tag.getListOrDefault("ListFloat").getFloat(1, DUMMY_FLOAT), DUMMY_FLOAT, 0.0F);
        assertEquals(tag.getListOrDefault("ListFloat").getFloatOrDefault(0), DUMMY_FLOAT, 0.0F);
        assertEquals(tag.getListOrDefault("ListDouble").getDouble(1, DUMMY_DOUBLE), DUMMY_DOUBLE, 0.0D);
        assertEquals(tag.getListOrDefault("ListDouble").getDoubleOrDefault(0), DUMMY_DOUBLE, 0.0D);
        assertEquals(tag.getListOrDefault("ListString").getString(1, DUMMY_STRING), DUMMY_STRING);
        assertEquals(tag.getListOrDefault("ListString").getStringOrDefault(0), DUMMY_STRING);
        assertEquals(tag.getListOrDefault("ListInts3").getInts(1, DUMMY_IMMUTABLE_INTS), DUMMY_IMMUTABLE_INTS);
        assertEquals(tag.getListOrDefault("ListInts3").getIntsOrDefault(0), DUMMY_IMMUTABLE_INTS);
        assertEquals(tag.getListOrDefault("ListBytes3").getBytes(1, DUMMY_IMMUTABLE_BYTES), DUMMY_IMMUTABLE_BYTES);
        assertEquals(tag.getListOrDefault("ListBytes3").getBytesOrDefault(0), DUMMY_IMMUTABLE_BYTES);
        assertEquals(tag.getListOrDefault("ListLongs3").getLongs(1, DUMMY_IMMUTABLE_LONGS), DUMMY_IMMUTABLE_LONGS);
        assertEquals(tag.getListOrDefault("ListLongs3").getLongsOrDefault(0), DUMMY_IMMUTABLE_LONGS);
        assertEquals(tag.getListOrDefault("ListList").getList(1, DUMMY_LIST_TAG), DUMMY_LIST_TAG);
        assertEquals(tag.getListOrDefault("ListList").getListOrDefault(0), DUMMY_LIST_TAG);
        assertEquals(tag.getListOrDefault("ListList").getList(1, TagType.INT, DUMMY_LIST_TAG), DUMMY_LIST_TAG);
        assertEquals(tag.getListOrDefault("ListList").getListOrDefault(0, TagType.INT), DUMMY_LIST_TAG);
        assertEquals(tag.getListOrDefault("ListCompound").getCompound(1, DUMMY_COMPOUND_TAG), DUMMY_COMPOUND_TAG);
        assertEquals(tag.getListOrDefault("ListCompound").getCompoundOrDefault(0), DUMMY_COMPOUND_TAG);
    }

    @Test
    public void testCompound() {
        CompoundTag tag = DUMMY_BIT_COMPOUND_TAG;
        assertTrue(tag.getBoolean("Unknown", true));
        assertTrue(tag.getBooleanOrDefault("Boolean"));
        assertEquals(tag.get("Unknown", DUMMY_END_TAG), DUMMY_END_TAG);
        assertEquals(tag.getOrDefault("End"), DUMMY_END_TAG);
        assertEquals(tag.getInt("Unknown", DUMMY_INT), DUMMY_INT);
        assertEquals(tag.getIntOrDefault("Int"), DUMMY_INT);
        assertEquals(tag.getLong("Unknown", DUMMY_LONG), DUMMY_LONG);
        assertEquals(tag.getLongOrDefault("Long"), DUMMY_LONG);
        assertEquals(tag.getByte("Unknown", DUMMY_BYTE), DUMMY_BYTE);
        assertEquals(tag.getByteOrDefault("Byte"), DUMMY_BYTE);
        assertEquals(tag.getShort("Unknown", DUMMY_SHORT), DUMMY_SHORT);
        assertEquals(tag.getShortOrDefault("Short"), DUMMY_SHORT);
        assertEquals(tag.getFloat("Unknown", DUMMY_FLOAT), DUMMY_FLOAT, 0.0F);
        assertEquals(tag.getFloatOrDefault("Float"), DUMMY_FLOAT, 0.0F);
        assertEquals(tag.getDouble("Unknown", DUMMY_DOUBLE), DUMMY_DOUBLE, 0.0D);
        assertEquals(tag.getDoubleOrDefault("Double"), DUMMY_DOUBLE, 0.0D);
        assertEquals(tag.getString("Unknown", DUMMY_STRING), DUMMY_STRING);
        assertEquals(tag.getStringOrDefault("String"), DUMMY_STRING);
        assertEquals(tag.getBytes("Unknown", DUMMY_IMMUTABLE_BYTES), DUMMY_IMMUTABLE_BYTES);
        assertEquals(tag.getBytesOrDefault("Bytes3"), DUMMY_IMMUTABLE_BYTES);
        assertEquals(tag.getInts("Unknown", DUMMY_IMMUTABLE_INTS), DUMMY_IMMUTABLE_INTS);
        assertEquals(tag.getIntsOrDefault("Ints3"), DUMMY_IMMUTABLE_INTS);
        assertEquals(tag.getLongs("Unknown", DUMMY_IMMUTABLE_LONGS), DUMMY_IMMUTABLE_LONGS);
        assertEquals(tag.getLongsOrDefault("Longs3"), DUMMY_IMMUTABLE_LONGS);
        assertEquals(tag.getList("Unknown", DUMMY_LIST_TAG), DUMMY_LIST_TAG);
        assertEquals(tag.getListOrDefault("List"), DUMMY_LIST_TAG);
        assertEquals(tag.getList("Unknown", TagType.INT, DUMMY_LIST_TAG), DUMMY_LIST_TAG);
        assertEquals(tag.getListOrDefault("List", TagType.INT), DUMMY_LIST_TAG);
        assertEquals(tag.getCompound("Unknown", DUMMY_COMPOUND_TAG), DUMMY_COMPOUND_TAG);
        assertEquals(tag.getCompoundOrDefault("Compound"), DUMMY_COMPOUND_TAG);
    }

    @Test
    public void testGzip() throws IOException {
        byte[] bytes = Base64.getDecoder().decode("H4sIAAAAAAAAAO1XzWtk" +
                "xxGv0Xxo3kgjjVba4N01JE5CIGDW3njXhISw8yXJskeW0Gi9a2NY98yrmWnU7/Vzdz/JEwIm93zc" +
                "Ym8SyJJ/ID4YbB/MggPGgd3/ILecAjk44EPIJanu9958aKW9+uKBhnndVdX18avqqgpABQptZlge" +
                "Lt9moY+Kh8NDxehPN2InYWvEwj4CwKUCVA6IQAZdRH/3TwX48qv3ny1DdYghKmakepUFCIs+Dlgs" +
                "TAmqTalISgtDg+oNSH85qLT5YMD7RDNeKMCFhKrLf44dVNEhD3BKuqgYD0kf0rHappNQcxlaZSuQ" +
                "u1aBpbZiQxlu8eHIeFDeZgZP2FjniXeD1hKtC6msdVplWgtgDQbwaBVordAq0VqmVaS1SmuR1hqt" +
                "qtMBwMqr5WA5ue0VLgT6uRzU9hUecxlrMU73nMzC1ITVW3mrVYCH48huEdPSLou2kJlYoc6d8tGd" +
                "zPASbCT7bRawIe6jagrZP7r5t3v29/sSrCfHt5my3nGHur5xinniUqaGaBq/+uOPwRm/+Boq68cc" +
                "lLshi/RIGjJxYccnyz8uQ8GFsXTt6rXrV6+TPYttNp41iWygmBjOBMm3fqgyIeRJSwYBoUPnCCb2" +
                "5r2wzfVRZlEeLp0FrjYKNgY4fKsCtVasjQyaUuvNY3KHJk0967uDWKAuw5IvD7nAtpKRhoJRMZah" +
                "4sstrvCQ94+yrYsBeydVhZDLww6GQzOC4os3brzwYhlqCv24j34be/FwJxxIKA6Y0MS4HtqgMHGA" +
                "KZ7JQ5nQyz7XrCdwU4yNYrvyGAmKpjVCujfj93y5K3sdSc5MuTZYGMqYkqfhH9scCpxV6WH1CDHa" +
                "Ca2lUo0nUqq+3AwNN+M5O9d82eEBN+i3FBsYmxAZw1Ige9uK48BupuSryqWpdUs3olyF3AtluNBP" +
                "vOLAshebKJ4ouqRtLA6Yz2MNC9eed2qQMS5EM3LXyLeJdqRGENiThR9dJ6cKOWz49J1hIGN4SkfY" +
                "N7Y06O3EqdgaxeHRhGA5dewB48SV2bTiy9uUIiPKi3FfYEa8rpGCmlyxRWb12DTsq74kmApbCOZ4" +
                "1gjdJ20rbBe1plxKryZwlfYJe6gqUGzaKlOBcoCBJFdqC9fVl2Jlmg742rAgAlc7Kl1BYbObypaQ" +
                "SsMYxXuxQW1rSrkEhSbTWL+egD7LpTWHKN6/Sv57CZkgPKaUWXpklFcyyqOQomQNPEDNSQFbglOe" +
                "m5/cu3d/hufiRHqKyyTm59xQzaiZImPPo/rWHNWhjIejkPw3UeHLefKNCbkxpHNStTLiem2eeH2e" +
                "+InaLme0IqZYUwmmjImFRRKBxj4lW1R7tsTYQjEPK/tSGSZaUgpfnoTgSnqt0dNSRTadGwGlo0mK" +
                "oMd6XFAVoyoMy3xGKhW0UsDGAzGmfxUekvN7MRd+rgjeCROJvj97+PARlU+ia7oz4hkkWhShTP8S" +
                "os7DR/S9bHVsZ0F0l1cU9nmETSmPPHrj3Ie2r9NFuExphH2b5T85GVHG36V3QbG+JF/BlemZpBQf" +
                "zh3OMPYUWT97dmnmTMRzbD+YHrnsuTtUbHx3KBjV9lm6b59JN0Pw9JTARj807BzthgpxTrtnTpv8" +
                "xNupgJ9B8NQ8wdm2n1Z55qg/ZuE5rh6jfd7O4Yt4eHReFAShe/bsu4+F73E7Ls26OcBz3JQE+HHu" +
                "Ga2jWEUCnxjBU1DIwQbXW1wY90i7h8ZlmQdVI5tICI5swfS/wek3OP16cVojnMYqZH3cjvlehCG9" +
                "BB7X048rE4Iz8EzPuOc6gqSpzUPpTmQnGn3r7T/kYfFOdCjpEUma1hUHeXWqhdcEjBaFj1M750Fp" +
                "V9rHpZQOCu734MMLxc9+++YPs+8CeLdu7bQ7yLT55PLGux/98r//LEIpaQYa9+07tT6Q0u+6HtSK" +
                "6+Axivp9q22+wVXuWXpu9sJtRS+Yn8uTBdk0BG6YKR+Q0pbPzjALf1188J/Gbx7lnDlOUmJOsduX" +
                "ChOFylahXanNL+7+pf39D9q/8yC/L7Uzo/7Td298/o/Go/prdad+/Zk/f+/v935dX4CC7bf/968i" +
                "5O9E+8nVlU3b1+8YDHTa6i/ZCS0dMuxMQepa4zJFNlJjN98ZMer5M2OTDqts+64sMrUuCmog0bfS" +
                "u0LatzvngTftm5MLq1acbXfTzsxtllz7+jr9v0lTmG3xErFfvUcaGmpDSWu3s/DeXkZtp9Qr5OkR" +
                "U75zFYFtOq12qCejXiUjtuPac6QktZtMpQ1rNihR65BekbQmtcmQPPEL2PAcZ18vf1qClXRuYwN8" +
                "Q4aYDXQ0UHUIN65dpRkt9/YXDz79dwnW5mZAe3H9asLgwWq3P6JBh2bSdJZKmirPOdp1Vt6rePKd" +
                "21IJvwSV6byYTYmPB5FaJruzT8VCU9OyGdpkco8Bjek039AEzAiH7aSh9zPk/x+AMy38YBAAAA==");

        String name;
        CompoundTag tag;

        try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            try (NbtReader reader = new NbtReader(stream, true)) {
                tag = reader.toCompoundTag();
                name = reader.getName();
            }
        }

        byte[] newBytes;

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (NbtWriter writer = new NbtWriter(stream, true, name)) {
                tag.accept(writer);
            }
            newBytes = stream.toByteArray();
        }

        assertArrayEquals(bytes, newBytes);
    }

    @Test
    @Ignore("not correctly implemented yet")
    public void testDeepRecursive() throws IOException {
        ListTag tag = ListTag.empty();

        for (int i = 0; i < 0x7FF7; ++i) {
            tag = ListTag.builder(TagType.LIST).add(tag).build();
        }

        CompoundTag compoundTag = CompoundTag.builder().add("List", tag).build();

        byte[] bytes;

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (NbtWriter writer = new NbtWriter(stream, true)) {
                compoundTag.accept(writer);
            }
            bytes = stream.toByteArray();
        }

        try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            try (NbtReader reader = new NbtReader(stream, true)) {
                CompoundTag newCompoundTag = reader.toCompoundTag();
                ListTag newTag = newCompoundTag.getListOrDefault("List");
                assertEquals(newTag, tag);
            }
        }
    }

    @Test
    public void testLargeArrays() throws IOException {
        byte[] bytes = new byte[0x7FF7];
        new Random(DUMMY_LONG * DUMMY_LONG).nextBytes(bytes);
        CompoundTag compoundTag = CompoundTag.builder()
                .add("Bytes", ByteArrayTag.of(ByteBuffer.wrap(bytes)))
                .add("Ints", IntArrayTag.of(ByteBuffer.wrap(bytes).asIntBuffer()))
                .add("Longs", LongArrayTag.of(ByteBuffer.wrap(bytes).asLongBuffer())).build();

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (NbtWriter writer = new NbtWriter(stream, true)) {
                compoundTag.accept(writer);
            }
            bytes = stream.toByteArray();
        }

        try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            try (NbtReader reader = new NbtReader(stream, true)) {
                CompoundTag newCompoundTag = reader.toCompoundTag();
                assertEquals(newCompoundTag, compoundTag);
            }
        }
    }

    @Test
    public void testHugeByteArray() throws IOException, GeneralSecurityException {
        Path tmp = Files.createTempFile("io.izzel.nbt.", ".tmp");
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] digest = writeHugeByteArrayToFileAndDigest(tmp, sha256);
        assertArrayEquals(digest, readHugeByteArrayToFileAndDigest(tmp, sha256));
    }

    private byte[] writeHugeByteArrayToFileAndDigest(Path file, MessageDigest digest) throws IOException {
        byte[] bytes = new byte[0x7FFFFFF7];
        new Random(DUMMY_LONG * DUMMY_LONG).nextBytes(bytes);
        ByteArrayTag byteArrayTag = ByteArrayTag.of(bytes);
        CompoundTag compoundTag = CompoundTag.builder().add("Bytes", byteArrayTag).build();
        try (OutputStream stream = Files.newOutputStream(file); NbtWriter writer = new NbtWriter(stream)) {
            compoundTag.accept(writer);
            return digest.digest(bytes);
        }
    }

    private byte[] readHugeByteArrayToFileAndDigest(Path file, MessageDigest digest) throws IOException {
        try (InputStream stream = Files.newInputStream(file); NbtReader reader = new NbtReader(stream)) {
            return digest.digest(reader.toCompoundTag().getBytesOrDefault("Bytes").toByteArray());
        }
    }
}
