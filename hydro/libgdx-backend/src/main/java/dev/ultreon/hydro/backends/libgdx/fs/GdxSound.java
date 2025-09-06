package dev.ultreon.hydro.backends.libgdx.fs;

import dev.ultreon.hydro.audio.Sound;
import org.joml.Vector3f;

public class GdxSound implements Sound {
    private final com.badlogic.gdx.audio.Sound sound;

    public GdxSound(com.badlogic.gdx.audio.Sound sound) {
        this.sound = sound;
    }

    @Override
    public long play() {
        return sound.play();
    }

    @Override
    public long play(float volume) {
        return sound.play(volume);
    }

    @Override
    public long play(float volume, float pitch, float pan) {
        return sound.play(volume, pitch, pan);
    }

    @Override
    public long play(float volume, float pitch, float pan, Vector3f position) {
        return sound.play(volume, pitch, pan);
    }

    @Override
    public long loop() {
        return sound.loop();
    }

    @Override
    public long loop(float volume) {
        return sound.loop(volume);
    }

    @Override
    public long loop(float volume, float pitch, float pan) {
        return sound.loop(volume, pitch, pan);
    }

    @Override
    public long loop(float volume, float pitch, float pan, Vector3f position) {
        return sound.loop(volume, pitch, pan);
    }

    @Override
    public void pause() {
        sound.pause();
    }

    @Override
    public void resume() {
        sound.resume();
    }

    @Override
    public void stop() {
        sound.stop();
    }

    @Override
    public void destroy() {
        sound.dispose();
    }

    public com.badlogic.gdx.audio.Sound getSound() {
        return sound;
    }

    @Override
    public void pause(long id) {
        sound.pause(id);
    }

    @Override
    public void resume(long id) {
        sound.resume(id);
    }

    @Override
    public void stop(long id) {
        sound.stop(id);
    }

    @Override
    public void setPitch(long id, float pitch) {
        sound.setPitch(id, pitch);
    }

    @Override
    public void setVolume(long id, float volume) {
        sound.setVolume(id, volume);
    }

    @Override
    public void setPan(long id, float pan, float volume) {
        sound.setPan(id, pan, volume);
    }

    @Override
    public void setPosition(long id, Vector3f position) {
        // LibGDX doesn't support this
    }

    @Override
    public void setLooping(long id, boolean looping) {
        sound.setLooping(id, looping);
    }

    @Override
    public void getPosition(long id, Vector3f position) {
        position.set(0, 0, 0);
    }

    @Override
    public void setDirection(long id, Vector3f direction) {
        // LibGDX doesn't support this
    }

    @Override
    public void setRolloff(long id, float rolloff) {
        // LibGDX doesn't support this
    }
}
