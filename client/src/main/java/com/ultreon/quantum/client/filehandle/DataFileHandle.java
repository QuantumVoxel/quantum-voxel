package com.ultreon.quantum.client.filehandle;

import com.badlogic.gdx.files.FileHandle;
import com.ultreon.quantum.client.QuantumClient;

public class DataFileHandle extends FileHandle {
    public DataFileHandle(String path) {
        super(QuantumClient.data(path).file());
    }
}
