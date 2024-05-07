package dev.ultreon.quantum.client.model;

import com.badlogic.gdx.graphics.g3d.Model;

public interface ModelImporter {
    Model getModel();

    Model createModel();
}
