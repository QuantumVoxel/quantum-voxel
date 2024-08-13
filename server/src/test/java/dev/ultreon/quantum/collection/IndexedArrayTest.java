package dev.ultreon.quantum.collection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class IndexedArrayTest {
//
//    @Test
//    @DisplayName("Basic Read/Write Test")
//    void readWriteTest() {
//        IndexedArray<Integer> test = new IndexedArray<>(2, 0);
//        assertEquals(0, test.get(0));
//        assertEquals(0, test.get(1));
//
//        test.set(0, 1);
//
//        assertEquals(1, test.get(0));
//        assertEquals(0, test.get(1));
//
//        test.set(1, 2);
//        assertEquals(1, test.get(0));
//        assertEquals(2, test.get(1));
//
//        test.set(0, 3);
//
//        assertEquals(3, test.get(0));
//        assertEquals(2, test.get(1));
//
//        test.set(1, 4);
//        assertEquals(3, test.get(0));
//        assertEquals(4, test.get(1));
//
//        test.set(0, 5);
//
//        assertEquals(5, test.get(0));
//        assertEquals(4, test.get(1));
//
//        test.set(1, 6);
//        assertEquals(5, test.get(0));
//        assertEquals(6, test.get(1));
//    }
//
//    @Test
//    @DisplayName("Mass Read/Write Test")
//    void massReadWriteTest() {
//        IndexedArray<Integer> test = new IndexedArray<>(100, 0);
//        for (int i = 0; i < 100; i++) {
//            test.set(i, i);
//        }
//
//        for (int i = 0; i < 100; i++) {
//            assertEquals(i, test.get(i));
//        }
//    }
//
//    @Test
//    @DisplayName("Mass Random Read/Write Test")
//    void massRandomReadWriteTest() {
//        IndexedArray<Integer> test = new IndexedArray<>(100, 0);
//        Random random = new Random();
//
//        for (int i = 0; i < 100; i++) {
//            test.set(random.nextInt(100), i);
//        }
//
//        Integer[] test2 = new Integer[100];
//        for (int i = 0; i < 100; i++) {
//            test2[i] = test.get(i);
//        }
//
//        // Dump to file
//        try (OutputStream out = Files.newOutputStream(Path.of("massRandomReadWriteTest.txt"))) {
//            for (Integer i : test2) {
//                out.write(i.toString().getBytes());
//                out.write("\n".getBytes());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        for (int i = 0; i < 100; i++) {
//            assertEquals(i, test.get(i));
//        }
//    }
//
//    @Test
//    @DisplayName("Gigantic Random Read/Write Test")
//    void giganticRandomReadWriteTest() {
//        IndexedArray<Integer> test = new IndexedArray<>(100000, 0);
//        Random random = new Random();
//
//        for (int i = 0; i < 100000; i++) {
//            test.set(random.nextInt(100000), i);
//        }
//
//        Integer[] test2 = new Integer[100000];
//        for (int i = 0; i < 100000; i++) {
//            test2[i] = test.get(i);
//        }
//
//        // Dump to file
//        try (OutputStream out = Files.newOutputStream(Path.of("giganticRandomReadWriteTest.txt"))) {
//            for (Integer i : test2) {
//                out.write(i.toString().getBytes());
//                out.write("\n".getBytes());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        for (int i = 0; i < 100000; i++) {
//            assertEquals(i, test.get(i));
//        }
//    }
//
//    @Test
//    @DisplayName("Gigantic Array Compared Read/Write Test")
//    void enormousArrayReadWriteTest() {
//        Integer[] test = new Integer[100000];
//        IndexedArray<Integer> test2 = new IndexedArray<>(100000, 0);
//
//        for (int i = 0; i < 100000; i++) {
//            test[i] = i;
//            test2.set(i, i);
//        }
//
//        // Dump to file
//        try (OutputStream out = Files.newOutputStream(Path.of("enourmousArrayReadWriteTest.against.txt"))) {
//            for (Integer i : test) {
//                out.write(i.toString().getBytes());
//                out.write("\n".getBytes());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        Integer[] test3 = new Integer[100000];
//        for (int i = 0; i < 100000; i++) {
//            test3[i] = test2.get(i);
//        }
//
//        // Dump to file
//        try (OutputStream out = Files.newOutputStream(Path.of("enourmousArrayReadWriteTest.compared.txt"))) {
//            for (Integer i : test3) {
//                out.write(i.toString().getBytes());
//                out.write("\n".getBytes());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        for (int i = 0; i < 100000; i++) {
//            int finalI = i;
//            assertEquals(test[i], test2.get(i), () -> "Failed at index " + finalI + " with value " + test[finalI] + " and " + test2.get(finalI));
//        }
//    }
//
//    @Test
//    @DisplayName("Gigantic Array Compared Random Read/Write Test")
//    void enormousArrayRandomReadWriteTest() {
//        Integer[] test = new Integer[100000];
//        IndexedArray<Integer> test2 = new IndexedArray<>(100000, 0);
//        Random random = new Random();
//
//        for (int i = 0; i < 100000; i++) {
//            int rand = random.nextInt(100000);
//            test[i] = rand;
//            test2.set(i, rand);
//        }
//
//        Integer[] test3 = new Integer[100000];
//        for (int i = 0; i < 100000; i++) {
//            test3[i] = test2.get(i);
//        }
//
//        // Dump to file
//        try (OutputStream out = Files.newOutputStream(Path.of("enourmousArrayRandomReadWriteTest.against.txt"))) {
//            for (Integer i : test) {
//                out.write(i.toString().getBytes());
//                out.write("\n".getBytes());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        // Dump to file
//        try (OutputStream out = Files.newOutputStream(Path.of("enourmousArrayRandomReadWriteTest.compared.txt"))) {
//            for (Integer i : test3) {
//                out.write(i.toString().getBytes());
//                out.write("\n".getBytes());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        for (int i = 0; i < 100000; i++) {
//            assertEquals(test[i], test3[i]);
//        }
//    }
}