package dev.ultreon.quantum.cs;

public interface Component {
    void onTick();

    void onCreate();

    void onDestroy();
}
