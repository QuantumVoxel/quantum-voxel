package dev.ultreon.quantum.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class CompoundCodecTest {

    // Simple test class
    static class TestPerson {
        private final String name;
        private final int age;

        public TestPerson(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestPerson that = (TestPerson) o;
            return age == that.age && name.equals(that.name);
        }
    }

    // Simple implementation of DataOps for testing
    static class TestDataOps implements DataOps<Object> {
        private final Map<String, Object> data = new HashMap<>();
        
        @Override
        public Object write(Object data) {
            return data;
        }

        @Override
        public byte readByte(Object data) {
            return (byte) data;
        }

        @Override
        public short readShort(Object data) {
            return (short) data;
        }

        @Override
        public int readInt(Object data) {
            return (int) data;
        }

        @Override
        public long readLong(Object data) {
            return (long) data;
        }

        @Override
        public float readFloat(Object data) {
            return (float) data;
        }

        @Override
        public double readDouble(Object data) {
            return (double) data;
        }

        @Override
        public String readString(Object data) {
            return (String) data;
        }

        @Override
        public boolean readBoolean(Object data) {
            return (boolean) data;
        }

        @Override
        public char readChar(Object data) {
            return (char) data;
        }

        @Override
        public java.util.UUID readUuid(Object data) {
            return (java.util.UUID) data;
        }

        @Override
        public void readEnd(Object data) {
            // No-op for test
        }

        @Override
        public Object read(Object data) {
            return data;
        }

        @Override
        public DataResult<Object> readMapEntry(String key, Map<String, Object> map) {
            return new DataResult<>(map.get(key), true);
        }

        @Override
        public void iterate(Object data, java.util.function.Consumer<Object> consumer) {
            if (data instanceof Map) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) data).entrySet()) {
                    consumer.accept(entry.getValue());
                }
            }
        }

        @Override
        public boolean isMap(Object data) {
            return data instanceof Map;
        }

        @Override
        public boolean isList(Object data) {
            return data instanceof java.util.List;
        }

        @Override
        public boolean isNumber(Object data) {
            return data instanceof Number;
        }

        @Override
        public boolean isBoolean(Object data) {
            return data instanceof Boolean;
        }

        @Override
        public boolean isString(Object data) {
            return data instanceof String;
        }

        @Override
        public boolean isChar(Object data) {
            return data instanceof Character;
        }

        @Override
        public boolean isUuid(Object data) {
            return data instanceof java.util.UUID;
        }

        @Override
        public boolean isNull(Object data) {
            return data == null;
        }

        @Override
        public Class<?> getType(Object data) {
            return data != null ? data.getClass() : null;
        }

        @Override
        public Object createMap() {
            return new HashMap<String, Object>();
        }

        @Override
        public Object createList() {
            return new java.util.ArrayList<Object>();
        }

        @Override
        public Object unit() {
            return null;
        }

        @Override
        public Object writeByte(byte value) {
            return value;
        }

        @Override
        public Object writeShort(short value) {
            return value;
        }

        @Override
        public Object writeInt(int value) {
            return value;
        }

        @Override
        public Object writeLong(long value) {
            return value;
        }

        @Override
        public Object writeFloat(float value) {
            return value;
        }

        @Override
        public Object writeDouble(double value) {
            return value;
        }

        @Override
        public Object writeString(String value) {
            return value;
        }

        @Override
        public Object writeBoolean(boolean value) {
            return value;
        }

        @Override
        public Object writeChar(char value) {
            return value;
        }

        @Override
        public Object writeUuid(java.util.UUID value) {
            return value;
        }

        @Override
        public void writeMapEntry(Object map, String key, Object value) {
            if (map instanceof Map) {
                ((Map<String, Object>) map).put(key, value);
            }
        }

        @Override
        public void writeListItem(Object list, Object value) {
            if (list instanceof java.util.List) {
                ((java.util.List<Object>) list).add(value);
            }
        }
    }

    @Test
    void testWriteAndRead() {
        // Create a codec builder for TestPerson
        CompoundCodec.Builder<TestPerson> builder = CompoundCodec.builder(map -> 
            new TestPerson((String) map.get("name"), (int) map.get("age"))
        );
        
        // Add field codecs
        builder.add(Codec.STRING.fieldOf("name"))
            .getBy(TestPerson::getName)
            .build();
        builder.add(Codec.INT.fieldOf("age"))
            .getBy(TestPerson::getAge)
            .build();
            
        // Build the final codec
        CompoundCodec<TestPerson> codec = builder.build();
        
        // Create test data
        TestPerson person = new TestPerson("John Doe", 30);
        
        // Create test data ops
        TestDataOps dataOps = new TestDataOps();
        
        // Test write
        DataResult<Object> writeResult = codec.write(dataOps, person);
        assertTrue(writeResult.isSuccessful());
        
        Map<String, Object> writtenMap = (Map<String, Object>) writeResult.getValue();
        assertEquals("John Doe", writtenMap.get("name"));
        assertEquals(30, writtenMap.get("age"));
        
        // Test read
        DataResult<TestPerson> readResult = codec.read(dataOps, writtenMap);
        assertTrue(readResult.isSuccessful());
        
        TestPerson readPerson = readResult.getValue();
        assertEquals(person, readPerson);
        assertEquals("John Doe", readPerson.getName());
        assertEquals(30, readPerson.getAge());
    }
}