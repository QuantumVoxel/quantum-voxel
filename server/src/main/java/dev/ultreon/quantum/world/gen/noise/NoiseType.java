package dev.ultreon.quantum.world.gen.noise;

import dev.ultreon.quantum.server.ServerDisposable;

public interface NoiseType extends ServerDisposable {

    double eval(double x, double y);

    double eval(double x, double y, double z);
}
