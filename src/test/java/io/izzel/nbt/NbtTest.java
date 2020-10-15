package io.izzel.nbt;

import io.izzel.nbt.util.NbtReader;
import io.izzel.nbt.util.NbtWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;

public class NbtTest {
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

        CompoundTag tag;

        try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            try (NbtReader reader = new NbtReader(stream, true)) {
                tag = reader.readAsTag();
            }
        }

        byte[] newBytes;

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (NbtWriter writer = new NbtWriter(stream, true)) {
                tag.accept(writer);
            }
            newBytes = stream.toByteArray();
        }

        assertArrayEquals(bytes, newBytes);
    }

    @Test
    public void testHugeByteArray() throws IOException {
        Random random = new Random(42L);

        byte[] bytes = new byte[0x7FFFFFF7];
        random.nextBytes(bytes);

        ByteArrayTag byteArrayTag = new ByteArrayTag(bytes);
        CompoundTag compoundTag = new CompoundTag("");
        compoundTag.put("Bytes", byteArrayTag);

        Path tmp = Files.createTempFile("io.izzel.nbt.", ".tmp");

        try (OutputStream stream = Files.newOutputStream(tmp); NbtWriter writer = new NbtWriter(stream)) {
            compoundTag.accept(writer);
        }

        try (InputStream stream = Files.newInputStream(tmp); NbtReader reader = new NbtReader(stream)) {
            CompoundTag newCompoundTag = reader.readAsTag();
            Assert.assertEquals(newCompoundTag.getName(), "");
            assertArrayEquals(((ByteArrayTag) newCompoundTag.get("Bytes")).getValue(), bytes);
        }
    }

    @Test
    public void testDeepRecursive() throws IOException {
        ListTag tag = new ListTag(TagType.END);

        for (int i = 0; i < 0x7FF7; ++i) {
            ListTag outerTag = new ListTag(TagType.LIST);
            outerTag.add(tag);
            tag = outerTag;
        }

        CompoundTag compoundTag = new CompoundTag("");

        compoundTag.put("List", tag);

        byte[] bytes;

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (NbtWriter writer = new NbtWriter(stream, true)) {
                compoundTag.accept(writer);
            }
            bytes = stream.toByteArray();
        }

        try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            try (NbtReader reader = new NbtReader(stream, true)) {
                CompoundTag newCompoundTag = reader.readAsTag();
                ListTag newTag = (ListTag) newCompoundTag.get("List");
                Assert.assertEquals(newTag, tag);
            }
        }
    }
}