package dev.ultreon.quantum.debug;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class HexDump {
    public static void dumpHexTable(int lines, SeekableByteChannel channel) throws IOException {
        System.out.println("___________|  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |");
        byte[] buffer = new byte[16];
        long currentPos = channel.position();

        for (int line = 0; line < lines; line++) {
            System.out.printf("0x%08x : ", currentPos + line * 16);
            int read = channel.read(ByteBuffer.wrap(buffer));
            if (read == -1) break;

            StringBuilder hex = new StringBuilder();
            StringBuilder ascii = new StringBuilder();

            for (int i = 0; i < 16; i++) {
                if (i < read) {
                    hex.append(String.format("%02x ", buffer[i]));
                    ascii.append(toAsciiChar(buffer[i]));
                } else {
                    hex.append("   ");
                    ascii.append(" ");
                }
            }

            System.out.println(hex + "| " + ascii);
        }
        channel.position(currentPos);
    }

    private static char toAsciiChar(byte b) {
        if (b >= 32 && b < 127) {
            return (char) b;
        }
        return '.';
    }
}
