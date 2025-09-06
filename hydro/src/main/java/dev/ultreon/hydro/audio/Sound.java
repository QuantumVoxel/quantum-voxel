package dev.ultreon.hydro.audio;

import org.joml.Vector3f;

public interface Sound {
    long play();

    long play(float volume);

    long play(float volume, float pitch, float pan);

    long play(float volume, float pitch, float pan, Vector3f position);

    long loop();

    long loop(float volume);

    long loop(float volume, float pitch, float pan);

    long loop(float volume, float pitch, float pan, Vector3f position);

    void pause();

    void resume();

    void stop();
    void destroy();

    void pause(long id);

    void resume(long id);

    void stop(long id);

    void setPitch(long id, float pitch);

    void setVolume(long id, float volume);

    void setPan(long id, float pan, float volume);

    void setPosition(long id, Vector3f position);

    void setLooping(long id, boolean looping);

    void getPosition(long id, Vector3f position);

    void setDirection(long id, Vector3f direction);

    void setRolloff(long id, float rolloff);
}
