package dev.ultreon.quantum.client.audio.music;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * Manages background music playback with transitions and scheduling.
 * Supports different music categories and fade effects.
 */
@SuppressWarnings("GDXJavaUnsafeIterator")
public class MusicManager implements Music.OnCompletionListener {
    private static final MusicManager INSTANCE = new MusicManager();
    private final ObjectMap<NamespaceID, MusicEntry> musicRegistry = new ObjectMap<>();
    private final RandomXS128 rng = new RandomXS128();
    private MusicEntry currentMusic;
    private long nextMusic;
    private float fadeTime = 2f; // Fade duration in seconds
    private float currentVolume = 1f;
    private float fadePosition = 0f;
    private State state = State.STOPPED;

    public float getVolume() {
        return currentVolume;
    }

    public double getTimeUntilNextTrack() {
        return (nextMusic - System.currentTimeMillis()) / 1000.0;
    }

    @Override
    public void onCompletion(Music music) {
        nextMusic = System.currentTimeMillis() + rng.nextInt(300000 - 60000) + 60000;
        startFadeOut();
    }

    private enum State {
        PLAYING, PAUSED, STOPPED, FADING_IN, FADING_OUT
    }

    public enum Category {
        AMBIENT, MENU, ITEM
    }

    private static class MusicEntry {
        final Music music;
        final String category;

        MusicEntry(Music music, String category) {
            this.music = music;
            this.category = category;
        }

        public void dispose() {
            music.dispose();
        }
    }

    public static MusicManager get() {
        return INSTANCE;
    }

    private MusicManager() {

    }

    public void reload() {
        for (MusicEntry music : musicRegistry.values()) {
            music.dispose();
        }
        musicRegistry.clear();
    }

    /**
     * Registers a music track with the specified ID and category
     * @param id The unique identifier for the music track
     * @param category The category this music belongs to
     */
    public void registerMusic(@NotNull NamespaceID id, String category) {
        QuantumClient.invokeAndWait(() -> {
            Music music = Gdx.audio.newMusic(QuantumClient.resource(id.mapPath(path -> "music/" + path + ".ogg")));
            this.musicRegistry.put(id, new MusicEntry(music, category));
            music.setOnCompletionListener(this);
        });
    }

    public void update() {
        if (musicRegistry.size == 0) return;

        float delta = Gdx.graphics.getDeltaTime();
        if (nextMusic <= 0) nextMusic = System.currentTimeMillis() + rng.nextInt(30000) + 30000;

        switch (state) {
            case FADING_OUT:
                fadePosition += delta;
                if (fadePosition >= fadeTime) {
                    stopImmediate();
                } else {
                    currentMusic.music.setVolume(currentVolume * (1 - fadePosition / fadeTime));
                }
                break;

            case FADING_IN:
                fadePosition += delta;
                if (fadePosition >= fadeTime) {
                    state = State.PLAYING;
                    currentMusic.music.setVolume(currentVolume);
                } else {
                    currentMusic.music.setVolume(currentVolume * (fadePosition / fadeTime));
                }
                break;

            case PLAYING:
                break;

            case STOPPED:
                long curTime = System.currentTimeMillis();
                if (curTime >= nextMusic) {
                    pickNextTrack();
                }
                break;
        }
    }

    private void pickNextTrack() {
        if (musicRegistry.size == 0) return;

        // Get current category to avoid same type of music
        String currentCategory = currentMusic != null ? currentMusic.category : null;

        ObjectMap.Values<MusicEntry> candidates = musicRegistry.values();
        MusicEntry nextTrack;

        // Try to pick music from different category
        do {
            int index = rng.nextInt(musicRegistry.size);
            nextTrack = candidates.toArray().get(index);
        } while (musicRegistry.size > 1 && nextTrack.category.equals(currentCategory));

        currentMusic = nextTrack;
        fadePosition = 0f;
        state = State.FADING_IN;
        currentMusic.music.play();
    }

    /**
     * Sets the master volume for all music tracks
     * @param volume Volume level between 0.0 and 1.0
     */
    public void setVolume(float volume) {
        this.currentVolume = volume;
        if (currentMusic != null && state == State.PLAYING) {
            currentMusic.music.setVolume(volume);
        }
    }

    /**
     * Stops the current music track with fade out
     */
    public void stop() {
        if (currentMusic == null || state == State.STOPPED) return;
        startFadeOut();
    }

    private void startFadeOut() {
        state = State.FADING_OUT;
        fadePosition = 0f;
    }

    private void stopImmediate() {
        if (currentMusic != null) {
            currentMusic.music.stop();
            currentMusic = null;
        }
        nextMusic = System.currentTimeMillis() + rng.nextInt(200000 - 60000) + 60000;
        state = State.STOPPED;
    }

    public void dispose() {
        currentMusic = null;
        for (MusicEntry music : musicRegistry.values()) {
            if (music.music.isPlaying()) music.music.stop();
            music.dispose();
        }

        musicRegistry.clear();
    }

    private boolean isPlaying() {
        return currentMusic != null;
    }

    public void pause() {
        if (!isPlaying()) return;
        currentMusic.music.pause();
    }

    public void resume() {
        if (nextMusic == 0) nextMusic = System.currentTimeMillis() + rng.nextInt(30000) + 30000;
        if (!isPlaying()) return;
        currentMusic.music.play();
    }

    public boolean isPaused() {
        return isPlaying() && currentMusic.music.isPlaying();
    }
}
