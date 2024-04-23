package com.ultreon.quantum.api.commands.variables;

import com.ultreon.quantum.server.QuantumServer;

import java.util.Set;

public class RootVariables {
    public static final RootVariableSource<QuantumServer> SERVER = new RootVariableSource<>(ObjectType.SERVER, QuantumServer::get);

    public static RootVariableSource<?> get(String name) {
        return RootVariableSource.VARIABLES.get(name);
    }

    public static Set<String> names() {
        return RootVariableSource.VARIABLES.keySet();
    }
}
