package dev.ultreon.quantum.api.neocommand;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import dev.ultreon.libs.commons.v0.exceptions.SyntaxException;
import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.util.NamespaceID;

import java.io.IOException;
import java.io.StringReader;

public class CommandReader {
    private final String cmd;
    private final String[] args;
    private int cur = 0;
    private final QuantumServer server;
    private CommandSender sender;

    public CommandReader(String cmd, String[] args, QuantumServer server, CommandSender sender) {
        this.cmd = cmd;
        this.args = args;
        this.server = server;
        this.sender = sender;
    }

    public static CommandReader fromString(String input, QuantumServer server, CommandSender sender) {
        String command = input.split(" ")[0];
        String[] args = new String[input.split(" ").length - 1];
        System.arraycopy(input.split(" "), 1, args, 0, args.length);
        return new CommandReader(command, args, server, sender);
    }

    public String getCommand() {
        return this.cmd;
    }

    public String[] getArgs() {
        return this.args;
    }

    public int tell() {
        return this.cur;
    }

    public int nextInt() throws CommandParseException {
        try {
            return Integer.parseInt(this.args[this.cur++]);
        } catch (NumberFormatException e) {
            throw new CommandParseException("Invalid number", this.cur);
        }
    }

    public String nextWord() {
        return this.args[this.cur++];
    }

    public String nextString() throws CommandParseException {
        String arg = this.args[this.cur++];
        if (arg.startsWith("\"") && arg.endsWith("\"")) {
            return arg.substring(1, arg.length() - 1);
        } else if (arg.startsWith("\"")) {
            String substring = arg.substring(1);
            StringBuilder builder = new StringBuilder();
            while (!substring.isEmpty()) {
                int index = substring.indexOf("\"");
                if (index != -1) {
                    if (index != substring.length() - 1) throw new CommandParseException("Invalid string", this.cur);
                    builder.append(substring.substring(0, index));
                    builder.append("\"");
                    return builder.toString();
                }
                builder.append(substring);
            }

            throw new CommandParseException("Unterminated string", this.cur);
        } else {
            return arg;
        }
    }

    public boolean nextBoolean() {
        return Boolean.parseBoolean(this.args[this.cur++]);
    }

    public double nextDouble() {
        return Double.parseDouble(this.args[this.cur++]);
    }

    public long nextLong() {
        return Long.parseLong(this.args[this.cur++]);
    }

    public float nextFloat() {
        return Float.parseFloat(this.args[this.cur++]);
    }

    public char nextChar() {
        String arg = this.args[this.cur++];
        if (arg.length() != 1) {
            throw new IllegalArgumentException("Argument must be a single character");
        }
        return arg.charAt(0);
    }

    public byte nextByte() {
        return Byte.parseByte(this.args[this.cur++]);
    }

    public short nextShort() {
        return Short.parseShort(this.args[this.cur++]);
    }

    public String remainder() {
        String[] args = new String[this.args.length - this.cur];
        System.arraycopy(this.args, this.cur, args, 0, args.length);
        return String.join(" ", args);
    }

    public Selector<?> nextSelector() throws CommandParseException {
        String arg = this.args[this.cur++];
        Selector.Type<?> type = Selector.Type.fromChar(arg.charAt(0));
        if (type == null) {
            throw new IllegalArgumentException("Invalid selector type");
        }
        try {
            return Selector.Type.parse(new StringReader(arg.substring(1)));
        } catch (IOException e) {
            throw new CommandParseException(e.getMessage(), this.cur);
        }
    }

    public Selective nextSelective() throws CommandParseException {
        StringReader reader = new StringReader(this.args[this.cur++]);
        try {
            char c = (char) reader.read();
            Selector.Type<?> type = Selector.Type.fromChar(c);
            StringBuilder name = new StringBuilder();
            if (type == null) {
                while (type == null) {
                    type = Selector.Type.fromChar(c);
                    if (type == null) {
                        name.append(c);
                        c = (char) reader.read();
                    }
                }
            }

            Array<Selector<?>> selectors = new Array<>();
            while (true) {
                Selector<?> selector = Selector.Type.parse(reader);
                if (selector == null) {
                    return new Selective(name.toString(), selectors.toArray(Selector.class));
                }
                selectors.add(selector);
            }
        } catch (IOException e) {
            throw new CommandParseException(e.getMessage(), this.cur);
        }
    }

    public String current() {
        return this.args[this.cur];
    }

    public void skip() {
        this.cur++;
    }

    public boolean isLast() {
        return this.cur == this.args.length;
    }

    public int length() {
        return this.args.length;
    }

    public String getCommandlineArgs() {
        return String.join(" ", this.args);
    }

    public boolean hasNext() {
        return this.cur < this.args.length;
    }

    public NamespaceID nextID() throws CommandParseException {
        String arg = this.args[this.cur++];
        try {
            return NamespaceID.parse(arg);
        } catch (SyntaxException e) {
            throw new CommandParseException(e.getMessage(), this.cur);
        }
    }

    public QuantumServer getServer() {
        return server;
    }

    public CommandSender getSender() {
        return sender;
    }

    public boolean isEOF() {
        return this.cur >= this.args.length;
    }

    public void seek(int i) {
        this.cur = i;
    }
}
