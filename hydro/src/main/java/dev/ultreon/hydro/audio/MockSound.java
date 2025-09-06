package dev.ultreon.hydro.audio;

import org.joml.Vector3f;

public class MockSound implements Sound {
    @Override
    public long play() {
        return 0;
    }

    @Override
    public long play(float volume) {
        return 0;
    }

    @Override
    public long play(float volume, float pitch, float pan) {
        return 0;
    }

    @Override
    public long play(float volume, float pitch, float pan, Vector3f position) {
        return 0;
    }

    @Override
    public long loop() {
        return 0;
    }

    @Override
    public long loop(float volume) {
        return 0;
    }

    @Override
    public long loop(float volume, float pitch, float pan) {
        return 0;
    }

    @Override
    public long loop(float volume, float pitch, float pan, Vector3f position) {
        return 0;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void pause(long id) {

    }

    @Override
    public void resume(long id) {

    }

    @Override
    public void stop(long id) {

    }

    @Override
    public void setPitch(long id, float pitch) {

    }

    @Override
    public void setVolume(long id, float volume) {

    }

    @Override
    public void setPan(long id, float pan, float volume) {

    }

    @Override
    public void setPosition(long id, Vector3f position) {

    }

    @Override
    public void setLooping(long id, boolean looping) {

    }

    @Override
    public void getPosition(long id, Vector3f position) {

    }

    @Override
    public void setDirection(long id, Vector3f direction) {

    }

    @Override
    public void setRolloff(long id, float rolloff) {

    }
}
