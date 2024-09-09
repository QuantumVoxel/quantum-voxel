package dev.ultreon.langgen;

import java.nio.file.Path;

public class LangGenConfig {
    public static boolean generateStub = false;
    public static LangGenListener progressListener = new LangGenListener() {
        @Override
        public void onProgress(int progress, int total) {

        }

        @Override
        public void onPreprocessProgress(int progress, int total) {

        }

        @Override
        public void onDone() {

        }
    };
    public static Path stubPath = Path.of(".lang-stub");
}
