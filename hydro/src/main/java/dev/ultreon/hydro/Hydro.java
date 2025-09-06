package dev.ultreon.hydro;

import dev.ultreon.hydro.engine.*;
import org.jetbrains.annotations.ApiStatus;

public final class Hydro {
    public static GraphicsEngine graphics;

    public static InputEngine input;

    public static AudioEngine audio;

    @ApiStatus.Experimental
    public static ComputeEngine compute;

    public static IOEngine io;
    public static Engine core;
}
