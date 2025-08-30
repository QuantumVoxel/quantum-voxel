package dev.ultreon.quantum.util;

import java.util.*;

public class ArgParser {
    private final List<String> args;
    private final Set<String> flags;
    private final Map<String, String> keywordArgs;
    private final List<String> argv;

    public ArgParser(String... argv) {
        this.argv = Arrays.asList(argv);

        ArrayList<String> args =new ArrayList<String>();
        HashSet<String> flags = new HashSet<String>();
        HashMap<String, String> keywordArgs = new HashMap<String, String>();

        for (String s : argv) {
            if (s.startsWith("--")) {
                String name = s.substring(2);
                String[] split = name.split("=", 2);
                if (split.length == 1) {
                    flags.add(name);
                } else {
                    String key = split[0];
                    String value = split[1];
                    keywordArgs.put(key, value);
                }
            } else if (s.startsWith("-")) {
                String name = s.substring(1);
                if (name.length() != 1) {
                    args.add(name);
                } else {
                    flags.add(name);
                }
            }
        }
        this.args = Collections.unmodifiableList(args);
        this.flags = Collections.unmodifiableSet(flags);
        this.keywordArgs = Collections.unmodifiableMap(keywordArgs);
    }

    public List<String> getArgs() {
        return this.args;
    }

    public Set<String> getFlags() {
        return this.flags;
    }

    public Map<String, String> getKeywordArgs() {
        return this.keywordArgs;
    }

    public List<String> getArgv() {
        return this.argv;
    }
}
