package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import dev.ultreon.quantum.client.config.ClientConfiguration;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.sound.event.SoundEvents;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class Screenshots {
    private boolean skipScreenshot = false;
    private boolean screenshotWorldOnly = false;
    private boolean triggerScreenshot = false;
    private boolean captureScreenshot = false;
    private int screenshotScale = 4;
    private long screenshotFlashTime = 0;
    private Consumer<Screenshot> onScreenshot;
    private final Color tmpColor = new Color();
    private final QuantumClient client;

    public Screenshots(QuantumClient client) {
        this.client = client;
    }

    public boolean isSkipScreenshot() {
        return skipScreenshot;
    }

    public void setSkipScreenshot(boolean skipScreenshot) {
        this.skipScreenshot = skipScreenshot;
    }

    public boolean isScreenshotWorldOnly() {
        return screenshotWorldOnly;
    }

    public void setScreenshotWorldOnly(boolean screenshotWorldOnly) {
        this.screenshotWorldOnly = screenshotWorldOnly;
    }

    public boolean isTriggerScreenshot() {
        return triggerScreenshot;
    }

    public void setTriggerScreenshot(boolean triggerScreenshot) {
        this.triggerScreenshot = triggerScreenshot;
    }

    public boolean isCaptureScreenshot() {
        return captureScreenshot;
    }

    public void setCaptureScreenshot(boolean captureScreenshot) {
        this.captureScreenshot = captureScreenshot;
    }

    public int getScreenshotScale() {
        return screenshotScale;
    }

    public void setScreenshotScale(int screenshotScale) {
        this.screenshotScale = screenshotScale;
    }

    public long getScreenshotFlashTime() {
        return screenshotFlashTime;
    }

    public void setScreenshotFlashTime(long screenshotFlashTime) {
        this.screenshotFlashTime = screenshotFlashTime;
    }

    /**
     * Makes a screenshot of the game.
     */
    public void screenshot(Consumer<Screenshot> screenshot) {
        this.triggerScreenshot = true;
        this.onScreenshot = screenshot;
    }

    /**
     * Makes a screenshot of the game.
     *
     * @param worldOnly whether to only screenshot the world.
     */
    public void screenshot(boolean worldOnly, Consumer<Screenshot> screenshot) {
        this.screenshotWorldOnly = worldOnly;
        this.triggerScreenshot = true;
        this.onScreenshot = screenshot;
    }


    void prepareScreenshot(float deltaTime) {
        // If the screenshot world only flag is true, clear the screen, render the world, and set the capture screenshot and trigger screenshot flags to false.
        // Then, grab a screenshot, complete the screenshot future, clear the screen, set the screenshot world only flag to false, and set the capture screenshot and trigger screenshot flags to false.
        if (this.screenshotWorldOnly) {
            ScreenUtils.clear(0, 0, 0, 0, true);
            client.gameRenderer.renderWorld(deltaTime);
            this.captureScreenshot = false;
            this.triggerScreenshot = false;

            Screenshot grabbed = Screenshot.grab(client.width, client.height);
            onScreenshot.accept(grabbed);
            ScreenUtils.clear(0, 0, 0, 0, true);
            this.screenshotWorldOnly = false;
        }

        // If the trigger screenshot flag is true, prepare the screenshot.
        if (this.triggerScreenshot) this.prepareScreenshot();
    }

    /**
     * Prepares the application for capturing a screenshot. This method adjusts
     * the screenshot scale, viewport dimensions, and necessary flags to ensure
     * a screenshot is properly captured at the desired resolution.
     */
    private void prepareScreenshot() {
        // Default to 1x scale
        this.screenshotScale = 1;

        // Check if 4x screenshot is enabled and shift is being held
        if (ClientConfiguration.enable4xScreenshot.getValue() && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
            this.screenshotScale = 4;
        }

        // Scale the viewport dimensions
        client.width = client.width * this.screenshotScale;
        client.height = client.height * this.screenshotScale;

        // Set screenshot flags
        this.captureScreenshot = true;
        this.triggerScreenshot = false;

        // Clear the screen to black
        ScreenUtils.clear(0, 0, 0, 1, true);
    }

    public void renderFlash(Renderer renderer, int width, int height) {
        // If the capture screenshot flag is true and the screenshot world only flag is false, handle the screenshot.
        if (this.captureScreenshot && !this.screenshotWorldOnly) this.handleScreenshot();

        // If the screenshot world only flag is false and the screenshot flash time is greater than the current time minus 200, draw the screenshot flash.
        if (!this.screenshotWorldOnly && this.screenshotFlashTime > System.currentTimeMillis() - 200) {
            renderer.begin();
            renderer.fill(0, 0, width, height, this.tmpColor.set(1, 1, 1, 1 - (System.currentTimeMillis() - this.screenshotFlashTime) / 200f));
            renderer.end();
        }
    }

    /**
     * Handles the screenshot capture and saves it to disk.
     * After saving, resets the viewport and resizes the game window.
     */
    private void handleScreenshot() {
        // Reset screenshot capture flag
        this.captureScreenshot = false;

        // Save screenshot to disk
        this.saveScreenshot();

        // Reset viewport to original dimensions
        Gdx.gl.glViewport(0, 0, client.getWidth(), client.getHeight());

        // Reset window size
        client.resize(QuantumClient.get().getWidth(), QuantumClient.get().getHeight());
    }


    /**
     * Saves a screenshot.
     */
    private void saveScreenshot() {
        // Beginning of the method
        if (client.spriteBatch != null && client.spriteBatch.isDrawing()) client.spriteBatch.flush();

        Screenshot grabbed = Screenshot.grab(client.width, client.height);
        FileHandle save = grabbed.save(Gdx.files.local("screenshots").child(String.format("%s.png", DateTimeFormatter.ofPattern("MM.dd.yyyy-HH.mm.ss").format(LocalDateTime.now()))));

        // Saving screenshot to a file
        this.screenshotFlashTime = System.currentTimeMillis();

        // Playing sound effect for taking a screenshot
        client.playSound(SoundEvents.SCREENSHOT, 0.5f);

        // Adding notification message with the saved file name and path
        client.notifications.add("Screenshot taken.", save.name(), "screenshots");

        onScreenshot.accept(grabbed);
        grabbed.dispose();
    }

}
