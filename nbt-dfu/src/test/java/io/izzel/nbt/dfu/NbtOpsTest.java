package io.izzel.nbt.dfu;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.izzel.nbt.ByteTag;
import io.izzel.nbt.CompoundTag;
import io.izzel.nbt.IntTag;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NbtOpsTest {
    public static final CompoundTag TAG = CompoundTag.builder().add("value", 42D).build();

    @Test
    public void convertPrimitive() {
        NbtOps ops = NbtOps.INSTANCE;
        assertEquals(Codec.BYTE.decode(ops, ByteTag.of((byte) 42)).map(Pair::getFirst), DataResult.success((byte) 42));
        assertEquals(ops.createInt(42), IntTag.of(42));
        assertEquals(ops.createMap(ImmutableMap.of(ops.createString("value"), ops.createDouble(42D))), TAG);
    }

    static class TestObject {
        private final double value;

        TestObject(double value) {
            this.value = value;
        }

        static Codec<TestObject> codec() {
            return RecordCodecBuilder.create(it ->
                    it.group(Codec.DOUBLE.fieldOf("value").forGetter(x -> x.value))
                            .apply(it, TestObject::new));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestObject that = (TestObject) o;

            return Double.compare(that.value, value) == 0;
        }

        @Override
        public int hashCode() {
            long temp = Double.doubleToLongBits(value);
            return (int) (temp ^ (temp >>> 32));
        }
    }

    @Test
    public void convertJavaObject() {
        TestObject object = new TestObject(42D);
        assertEquals(TestObject.codec().encodeStart(NbtOps.INSTANCE, object), DataResult.success(TAG));
        assertEquals(TestObject.codec().decode(NbtOps.INSTANCE, TAG).map(Pair::getFirst), DataResult.success(object));
    }
}