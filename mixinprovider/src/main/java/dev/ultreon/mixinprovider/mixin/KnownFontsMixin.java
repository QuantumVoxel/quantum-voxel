package dev.ultreon.mixinprovider.mixin;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.github.tommyettinger.textra.KnownFonts")
public abstract class KnownFontsMixin {
    @Shadow
    public static TextureAtlas loadUnicodeAtlas(FileHandle packFile, FileHandle imagesDir, boolean flip) {
        return null;
    }

    @Redirect(
            method = "addEmoji(Lcom/github/tommyettinger/textra/Font;Ljava/lang/String;Ljava/lang/String;FFF)Lcom/github/tommyettinger/textra/Font;",
            at = @At(value = "INVOKE", target = "Lcom/github/tommyettinger/textra/KnownFonts;loadUnicodeAtlas(Lcom/badlogic/gdx/files/FileHandle;Lcom/badlogic/gdx/files/FileHandle;Z)Lcom/badlogic/gdx/graphics/g2d/TextureAtlas;")
    )
    private static TextureAtlas meow(FileHandle packFile, FileHandle imagesDir, boolean flip) {
        return loadUnicodeAtlas(packFile, imagesDir, true);
    }
}
