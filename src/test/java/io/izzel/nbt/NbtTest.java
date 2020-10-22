package io.izzel.nbt;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;

import static org.junit.Assert.*;

public class NbtTest {
    @Test
    public void testInt() {
        IntTag intTag = IntTag.of(42);
        assertEquals(intTag.getInt(), 42);
    }

    @Test
    public void testLong() {
        LongTag longTag = LongTag.of(42L);
        assertEquals(longTag.getLong(), 42L);
    }

    @Test
    public void testByte() {
        ByteTag byteTag = ByteTag.of((byte) 42);
        assertEquals(byteTag.getByte(), (byte) 42);
    }

    @Test
    public void testBoolean() {
        assertTrue(ByteTag.of(true).getBoolean());
        assertFalse(ByteTag.of(false).getBoolean());
        assertTrue(ByteTag.of((byte) 1).getBoolean());
        assertFalse(ByteTag.of((byte) 0).getBoolean());
        assertEquals(ByteTag.of(true).getByte(), (byte) 1);
        assertEquals(ByteTag.of(false).getByte(), (byte) 0);
    }

    @Test
    public void testShort() {
        ShortTag shortTag = ShortTag.of((short) 42);
        assertEquals(shortTag.getShort(), (short) 42);
    }

    @Test
    public void testFloat() {
        FloatTag floatTag = FloatTag.of(42.0F);
        assertEquals(floatTag, FloatTag.of(42.0F));
        assertEquals(floatTag.getFloat(), 42.0F, 0.0F);
    }

    @Test
    public void testDouble() {
        DoubleTag doubleTag = DoubleTag.of(42.0);
        assertEquals(doubleTag, DoubleTag.of(42.0));
        assertEquals(doubleTag.getDouble(), 42.0, 0.0);
    }

    @Test
    public void testFloatingComparison() {
        assertNotEquals(FloatTag.of(0.0F), FloatTag.of(-0.0F));
        assertNotEquals(DoubleTag.of(0.0D), DoubleTag.of(-0.0D));
        assertEquals(FloatTag.of(Float.NaN), FloatTag.of(Float.NaN));
        assertEquals(DoubleTag.of(Double.NaN), DoubleTag.of(Double.NaN));
    }

    @Test
    public void testCache() {
        assertSame(EndTag.of(), EndTag.of());
        assertSame(IntTag.of(42), IntTag.of(42));
        assertSame(LongTag.of(42L), LongTag.of(42L));
        assertSame(ByteTag.of((byte) 42), ByteTag.of((byte) 42));
        assertSame(ShortTag.of((short) 42), ShortTag.of((short) 42));

        assertNotSame(IntTag.of(42 * 42), IntTag.of(42 * 42));
        assertNotSame(IntTag.of(42 * -42), IntTag.of(42 * -42));
        assertNotSame(LongTag.of(42L * 42L), LongTag.of(42L * 42L));
        assertNotSame(LongTag.of(42L * -42L), LongTag.of(42L * -42L));
        assertNotSame(ShortTag.of((short) (42 * -42)), ShortTag.of((short) (42 * -42)));
        assertNotSame(ShortTag.of((short) (42 * -42)), ShortTag.of((short) (42 * -42)));
    }

    @Test
    public void testToString() {
        assertEquals(EndTag.of().toString(), "");
        assertEquals(IntTag.of(42).toString(), "42");
        assertEquals(LongTag.of(42L).toString(), "42l");
        assertEquals(ByteTag.of((byte) 42).toString(), "42b");
        assertEquals(ShortTag.of((short) 42).toString(), "42s");
        assertEquals(FloatTag.of(42.0F).toString(), "42.0f");
        assertEquals(DoubleTag.of(42.0D).toString(), "42.0d");
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
                ListTag newTag = (ListTag) newCompoundTag.get("List");
                assertEquals(newTag, tag);
            }
        }
    }

    @Test
    public void testLargeArrays() throws IOException {
        byte[] bytes = new byte[0x7FF7];
        new Random(42L * 42L).nextBytes(bytes);
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
        new Random(42L * 42L).nextBytes(bytes);
        ByteArrayTag byteArrayTag = ByteArrayTag.of(bytes);
        CompoundTag compoundTag = CompoundTag.builder().add("Bytes", byteArrayTag).build();
        try (OutputStream stream = Files.newOutputStream(file); NbtWriter writer = new NbtWriter(stream)) {
            compoundTag.accept(writer);
            return digest.digest(bytes);
        }
    }

    private byte[] readHugeByteArrayToFileAndDigest(Path file, MessageDigest digest) throws IOException {
        try (InputStream stream = Files.newInputStream(file); NbtReader reader = new NbtReader(stream)) {
            return digest.digest(((ByteArrayTag) reader.toCompoundTag().get("Bytes")).getBytes().toByteArray());
        }
    }
}
