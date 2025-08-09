package dev.ultreon.quantum.data;

import com.badlogic.gdx.utils.JsonValue;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JsonOpsTest {
    private static class TestClass {
        public String test;
        public int test2;

        public TestClass() {
        }

        public TestClass(String test, int test2) {
            this.test = test;
            this.test2 = test2;
        }

        public String getTest() {
            return test;
        }

        public int getTest2() {
            return test2;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestClass testClass = (TestClass) o;
            return test2 == testClass.test2 && 
                   (test == null ? testClass.test == null : test.equals(testClass.test));
        }
    }

    @Test
    void testJsonOpsWithCompoundCodec() {
        // Get JsonOps instance
        JsonOps jsonOps = JsonOps.INSTANCE;
        
        // Create a codec for TestClass
        CompoundCodec<TestClass> codec = CompoundCodec.builder(args -> {
                    if (args.size() != 2) {
                        throw new CodecException("Invalid number of arguments!");
                    }
                    return new TestClass((String) args.get("test"), (int) args.get("test2"));
                })
                .add(Codec.STRING.fieldOf("test")).getBy(TestClass::getTest).build()
                .add(Codec.INT.fieldOf("test2")).getBy(TestClass::getTest2).build()
                .build();
        
        // Create test data
        TestClass testObject = new TestClass("Hello, World!", 42);
        
        // Test serialization
        DataResult<JsonValue> writeResult = codec.write(jsonOps, testObject);
        assertTrue(writeResult.isSuccessful());
        
        JsonValue jsonValue = writeResult.getValue();
        assertNotNull(jsonValue);
        assertEquals("Hello, World!", jsonValue.getString("test"));
        assertEquals(42, jsonValue.getInt("test2"));
        
        // Test deserialization
        DataResult<TestClass> readResult = codec.read(jsonOps, jsonValue);
        assertTrue(readResult.isSuccessful());
        
        TestClass deserializedObject = readResult.getValue();
        assertNotNull(deserializedObject);
        assertEquals(testObject, deserializedObject);
        assertEquals("Hello, World!", deserializedObject.getTest());
        assertEquals(42, deserializedObject.getTest2());
    }
    
    @Test
    void testJsonOpsBasicTypes() {
        JsonOps jsonOps = JsonOps.INSTANCE;
        
        // Test string
        JsonValue stringValue = jsonOps.writeString("test");
        assertEquals("test", jsonOps.readString(stringValue));
        
        // Test int
        JsonValue intValue = jsonOps.writeInt(123);
        assertEquals(123, jsonOps.readInt(intValue));
        
        // Test boolean
        JsonValue boolValue = jsonOps.writeBoolean(true);
        assertTrue(jsonOps.readBoolean(boolValue));
        
        // Test float
        JsonValue floatValue = jsonOps.writeFloat(3.14f);
        assertEquals(3.14f, jsonOps.readFloat(floatValue), 0.001);
        
        // Test double
        JsonValue doubleValue = jsonOps.writeDouble(2.71828);
        assertEquals(2.71828, jsonOps.readDouble(doubleValue), 0.00001);
    }
}