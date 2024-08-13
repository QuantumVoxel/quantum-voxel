package dev.ultreon.quantum.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IndexedListTest {
//    @Test
//    @DisplayName("Basic test")
//    void test() {
//        var list = IndexedList.<String>create(10, 10);
//
//        assertEquals(0, list.size());
//
//        list.add("apple");
//        assertEquals(1, list.size());
//
//        list.add("banana");
//        assertEquals(2, list.size());
//
//        list.add("cherry");
//        assertEquals(3, list.size());
//
//        // ---
//
//        list.set(0, "orange");
//        assertEquals(3, list.size());
//        assertEquals("orange", list.get(0));
//        assertEquals("banana", list.get(1));
//        assertEquals("cherry", list.get(2));
//
//        list.set(1, "pear");
//        assertEquals(3, list.size());
//        assertEquals("orange", list.get(0));
//        assertEquals("pear", list.get(1));
//        assertEquals("cherry", list.get(2));
//
//        // ---
//
//        list.remove(0);
//        assertEquals(2, list.size());
//        assertEquals("pear", list.get(0));
//        assertEquals("cherry", list.get(1));
//    }
//
//    @Test
//    @DisplayName("List compared read/write test")
//    void listComparedReadWriteTest() {
//        var list = new PaletteStorage<>(10, Integer.class);
//        var array = new Integer[10];
//        Arrays.fill(array, -1);
//
//        for (int $ = 0; $ < 10; $++) {
//            for (int i = 0; i < 10; i++) {
//                int value = (int) (Math.random() * 10);
//                list.set(i, value);
//                array[i] = i;
//            }
//        }
//
//        List<Integer> list2 = new ArrayList<>(Arrays.asList(array).subList(0, 10));
//        List<Integer> list3 = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            list3.add(list.get(i));
//        }
//
//        assertEquals(list2, list3);
//
//        list = new PaletteStorage<>(100, Integer.class);
//        array = new Integer[100];
//        Arrays.fill(array, -1);
//
//        for (int $ = 0; $ < 10; $++) {
//            for (int i = 0; i < 100; i++) {
//                int value = (int) (Math.random() * 100);
//                list.set(i, value);
//                array[i] = i;
//            }
//        }
//
//        for (int i = 0; i < 100; i++) {
//            assertEquals(array[i], list.get(i));
//        }
//
//        list = new PaletteStorage<>(1000, Integer.class);
//        array = new Integer[1000];
//        Arrays.fill(array, -1);
//
//        for (int $ = 0; $ < 10; $++) {
//            for (int i = 0; i < 1000; i++) {
//                int value = (int) (Math.random() * 1000);
//                list.set(i, value);
//                array[i] = i;
//            }
//        }
//
//        for (int i = 0; i < 1000; i++) {
//            assertEquals(array[i], list.get(i));
//        }
//
//        list = new PaletteStorage<>(10000, Integer.class);
//        array = new Integer[10000];
//        Arrays.fill(array, -1);
//
//        for (int $ = 0; $ < 10; $++) {
//            for (int i = 0; i < 10000; i++) {
//                int value = (int) (Math.random() * 10000);
//                list.set(i, value);
//                array[i] = i;
//            }
//        }
//
//        for (int i = 0; i < 10000; i++) {
//            assertEquals(array[i], list.get(i));
//        }
//
//        list = new PaletteStorage<>(100000, Integer.class);
//        array = new Integer[100000];
//        Arrays.fill(array, -1);
//
//        for (int $ = 0; $ < 10; $++) {
//            for (int i = 0; i < 100000; i++) {
//                int value = (int) (Math.random() * 100000);
//                list.set(i, value);
//                array[i] = i;
//            }
//        }
//
//        for (int i = 0; i < 100000; i++) {
//            assertEquals(array[i], list.get(i));
//        }
//
//        list = new PaletteStorage<>(1000000, Integer.class);
//        array = new Integer[1000000];
//        Arrays.fill(array, -1);
//
//        for (int $ = 0; $ < 10; $++) {
//            for (int i = 0; i < 1000000; i++) {
//                int value = (int) (Math.random() * 1000000);
//                list.set(i, value);
//                array[i] = i;
//            }
//        }
//
//        for (int i = 0; i < 1000000; i++) {
//            assertEquals(array[i], list.get(i));
//        }
//    }
//
//    @Test
//    @DisplayName("Random read/write test")
//    @SuppressWarnings("RedundantTypeArguments")
//    void randomReadWriteTest() {
//        // Read-write at random positions 10 times - 10 entries
//        var list = IndexedList.<Integer>create(10);
//
//        for (int i = 0; i < 10; i++) {
//            int index = (int) (Math.random() * 10);
//            int value = (int) (Math.random() * 10);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read write at random positions 10 times - 100 entries
//        list = IndexedList.<Integer>create(100);
//
//        for (int i = 0; i < 10; i++) {
//            int index = (int) (Math.random() * 100);
//            int value = (int) (Math.random() * 100);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read write at random positions 10 times - 1000 entries
//        list = IndexedList.<Integer>create(1000);
//
//        for (int i = 0; i < 10; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 1000);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read write at random positions 100 times - 10 entries
//        list = IndexedList.<Integer>create(10);
//
//        for (int i = 0; i < 100; i++) {
//            int index = (int) (Math.random() * 10);
//            int value = (int) (Math.random() * 10);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read write at random positions 100 times - 100 entries
//        list = IndexedList.<Integer>create(100);
//
//        for (int i = 0; i < 100; i++) {
//            int index = (int) (Math.random() * 100);
//            int value = (int) (Math.random() * 100);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read write at random positions 100 times - 1000 entries
//        list = IndexedList.<Integer>create(1000);
//
//        for (int i = 0; i < 100; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 1000);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read write at random positions 1000 times - 10 entries
//        list = IndexedList.<Integer>create(10);
//
//        for (int i = 0; i < 1000; i++) {
//            int index = (int) (Math.random() * 10);
//            int value = (int) (Math.random() * 10);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read write at random positions 1000 times - 100 entries
//        list = IndexedList.<Integer>create(100);
//
//        for (int i = 0; i < 1000; i++) {
//            int index = (int) (Math.random() * 100);
//            int value = (int) (Math.random() * 100);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read write at random positions 1000 times - 1000 entries
//        list = IndexedList.<Integer>create(1000);
//
//        for (int i = 0; i < 1000; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 1000);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//    }
//
//    @Test
//    @DisplayName("Massive random read/write test")
//    @SuppressWarnings("RedundantTypeArguments")
//    @DisabledIfEnvironmentVariable(named = "RUNNING", matches = "true")
//    void massiveRandomReadWriteTest() {
//        // Read-write at random positions 100000 times - 10 entries
//        IndexedList<Integer> list = IndexedList.<Integer>create(10);
//
//        for (int i = 0; i < 100000; i++) {
//            int index = (int) (Math.random() * 10);
//            int value = (int) (Math.random() * 10);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 100000 times - 100 entries
//        list = IndexedList.<Integer>create(100);
//
//        for (int i = 0; i < 100000; i++) {
//            int index = (int) (Math.random() * 100);
//            int value = (int) (Math.random() * 100);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 100000 times - 1000 entries
//        list = IndexedList.<Integer>create(1000);
//
//        for (int i = 0; i < 100000; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 1000);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 1000000 times - 10 entries
//        list = IndexedList.<Integer>create(10);
//
//        for (int i = 0; i < 1000000; i++) {
//            int index = (int) (Math.random() * 10);
//            int value = (int) (Math.random() * 10);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 1000000 times - 100 entries
//        list = IndexedList.<Integer>create(100);
//
//        for (int i = 0; i < 1000000; i++) {
//            int index = (int) (Math.random() * 100);
//            int value = (int) (Math.random() * 100);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 1000000 times - 1000 entries
//        list = IndexedList.<Integer>create(1000);
//
//        for (int i = 0; i < 1000000; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 1000);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 10000000 times - 10 entries
//        list = IndexedList.<Integer>create(10);
//
//        for (int i = 0; i < 10000000; i++) {
//            int index = (int) (Math.random() * 10);
//            int value = (int) (Math.random() * 10);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 10000000 times - 100 entries
//        list = IndexedList.<Integer>create(100);
//
//        for (int i = 0; i < 10000000; i++) {
//            int index = (int) (Math.random() * 100);
//            int value = (int) (Math.random() * 100);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 10000000 times - 1000 entries
//        list = IndexedList.<Integer>create(1000);
//
//        for (int i = 0; i < 10000000; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 1000);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//    }
//
//    @Test
//    @DisplayName("Random read/write common values test")
//    void randomReadWriteCommonValuesTest() {
//        IndexedList<Integer> list = IndexedList.<Integer>create(1000);
//
//        // Read-write at random positions 100000 times - 1000 entries
//        for (int i = 0; i < 100000; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 10);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 100000 times - 1000 entries
//        for (int i = 0; i < 100000; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 100);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 100000 times - 1000 entries
//        for (int i = 0; i < 100000; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 1000);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 1000000 times - 10 entries
//        for (int i = 0; i < 1000000; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 10);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 1000000 times - 100 entries
//        for (int i = 0; i < 1000000; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 100);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//
//        // Read-write at random positions 1000000 times - 1000 entries
//        for (int i = 0; i < 1000000; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 1000);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//    }
//
//    @Test
//    @DisplayName("Random read/write 100mil test")
//    @DisabledIfEnvironmentVariable(named = "RUNNING", matches = "true")
//    void randomReadWrite100MilTest() {
//        IndexedList<Integer> list = IndexedList.<Integer>create(1000);
//
//        for (int i = 0; i < 100_000_000; i++) {
//            int index = (int) (Math.random() * 1000);
//            int value = (int) (Math.random() * 1000);
//
//            list.set(index, value);
//            assertEquals(value, list.get(index));
//        }
//    }
}