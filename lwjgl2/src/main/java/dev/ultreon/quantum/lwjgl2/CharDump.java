package dev.ultreon.quantum.lwjgl2;

import java.util.Scanner;

public class CharDump {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                System.out.println("\\n: " + (int) '\n');
                System.out.println("\\r: " + (int) '\r');
                continue;
            }
            char c = line.charAt(0);
            System.out.println(c + ": " + (int) c);
        }
    }
}
