package dev.ultreon.langgen;

public interface LangGenListener {
    void onProgress(int progress, int total);

    void onPreprocessProgress(int progress, int total);

    void onDone();
}
