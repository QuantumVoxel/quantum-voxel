package dev.ultreon.quantum.text;

public interface Translatable {
    String getTranslationId();

    default MutableText getTranslation() {
        return TextObject.translation(this.getTranslationId());
    }

    default String getTranslationText() {
        return this.getTranslation().getText();
    }
}
