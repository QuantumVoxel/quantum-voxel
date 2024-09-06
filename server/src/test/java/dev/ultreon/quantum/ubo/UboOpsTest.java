package dev.ultreon.quantum.ubo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ultreon.ubo.types.DataType;
import dev.ultreon.ubo.types.MapType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UboOpsTest {
    record TestStruct(int a, int b) {}

    @Test
    @DisplayName("Codec test")
    void codecTest() {
        UboOps ops = new UboOps();
        Codec<TestStruct> codec = RecordCodecBuilder.create(instance -> instance
                .group(
                        Codec.INT.fieldOf("a").forGetter(TestStruct::a),
                        Codec.INT.fieldOf("b").forGetter(TestStruct::b)
                )
                .apply(instance, TestStruct::new)
        );

        DataResult<DataType<?>> dataTypeDataResult = codec.encodeStart(ops, new TestStruct(4, 6));
        assertTrue(dataTypeDataResult.isSuccess());

        DataType<?> orThrow = dataTypeDataResult.getOrThrow();

        assertInstanceOf(MapType.class, orThrow);

        MapType mapType = (MapType) orThrow;
        assertEquals(2, mapType.size());
        assertEquals(4, mapType.get("a").getValue());
        assertEquals(6, mapType.get("b").getValue());
    }
}