package dev.ultreon.quantum.client;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class GLSLVersion implements Comparable<GLSLVersion> {
    private final int major;

    private final int minor;

    public GLSLVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public static GLSLVersion parse(String version) {
        if (!version.matches("\\d+\\.\\d{2}")) {
            throw new IllegalArgumentException("Invalid GLSL version string: " + version);
        }

        String[] split = version.split("\\.", 2);
        if (split.length != 2) {
            throw new IllegalArgumentException("Invalid GLSL version string: " + version);
        }
        return new GLSLVersion(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    @Override
    public int compareTo(@NotNull GLSLVersion o) {
        int compare = Integer.compare(this.major, o.major);
        if (compare == 0) {
            compare = Integer.compare(this.minor, o.minor);
        }
        return compare;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%d.%02d", this.major, this.minor);
    }

    public int major() {
        return this.major;
    }

    public int minor() {
        return this.minor;
    }

    public boolean isAtLeast(int major, int minor) {
        if (this.major > major) {
            return true;
        } else if (this.major < major) {
            return false;
        } else {
            return this.minor >= minor;
        }
    }

    public boolean isAtLeast(GLSLVersion version) {
        if (this.major > version.major) {
            return true;
        } else if (this.major < version.major) {
            return false;
        } else {
            return this.minor >= version.minor;
        }
    }
}
