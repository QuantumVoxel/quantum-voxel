package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.gui.GuiBuilder;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.client.gui.Screen;
import dev.ultreon.quantum.client.gui.widget.SelectionList;
import dev.ultreon.quantum.client.util.Utils;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.text.TextObject;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
/**
 * Represents a screen that displays crash logs.
 *
 * @since 0.1.0
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public class CrashScreen extends Screen {
    // List of stack elements to be ignored in crash logs
    private static final List<String> UNUSABLE_STACK_ELEMENTS = List.of(
            "dev.ultreon.quantum.crash.", // Crash handling
            "java.", "javax.", "javafx.", // Java packages
            "kotlin.", "kotlinx.",        // Kotlin packages
            "sun.", "jdk.internal.",      // JDK internal packages
            "org.openjdk.", "org.junit.", // Misc. packages
            "com.badlogic.gdx."           // libGDX packages
    );

    // List of crash logs
    private final List<CrashLog> crashes;
    private SelectionList<CrashLog> list;

    /**
     * Constructor for CrashScreen class.
     * @param crashes A list of CrashLog objects representing the crash logs to be displayed.
     */
    public CrashScreen(List<CrashLog> crashes) {
        super(TextObject.translation("quantum.screen.crash"));

        this.crashes = crashes;
    }

    /**
     * Check if a stack trace id is a usable id.
     *
     * @param stackTraceElement The stack trace id to check.
     * @return true if the stack trace id is usable, false otherwise.
     */
    private static boolean isUsableStackElement(StackTraceElement stackTraceElement) {
        String className = stackTraceElement.getClassName();

        // Iterate over each unusable stack id and check if the class name starts with it
        for (String unusableStackElement : UNUSABLE_STACK_ELEMENTS) {
            if (className.startsWith(unusableStackElement)) {
                return false;
            }
        }

        return true;
    }

    /**
     * This method builds a GUI using the provided GuiBuilder.
     * It adds a SelectionList of CrashLog items to the GUI, renders the items, and sets the bounds.
     * It also writes the crash logs to a file and to the log.
     *
     * @param builder the GuiBuilder used to construct the GUI
     */
    @Override
    public void build(@NotNull GuiBuilder builder) {
        // Define the size of each item in the list
        final int itemSize = 75;

        // Add a SelectionList of CrashLog items to the GUI
        this.list = builder.add(
                new SelectionList<CrashLog>(itemSize)
                        .entries(crashes)
                        .itemRenderer(this::renderItem)
                        .bounds(() -> new Bounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight())
                        ));

        // Define the file destination for saving crash reports
        File destination = new File("crash-reports/" + CrashLog.getFileNameWithoutExt() + "-loading.txt");

        // Write each crash log to the specified file
        crashes.forEach(Utils.with(destination, CrashLog::writeToFile));

        // Write each crash log to the log
        crashes.forEach(CrashLog::writeToLog);
    }

    /**
     * Renders the widget with the given renderer, mouse coordinates, and delta time.
     * Overrides the superclass method and sets the width of the list to match the scaled width of the client.
     *
     * @param renderer the renderer to use for rendering
     * @param mouseX the setX coordinate of the mouse
     * @param mouseY the setY coordinate of the mouse
     * @param deltaTime the time that has passed since the last frame
     */
    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        this.list.width(QuantumClient.get().getScaledWidth());
    }

    /**
     * Renders the crash log details on the screen.
     *
     * @param renderer the renderer object
     * @param value the CrashLog object to render
     * @param y the setY-coordinate for rendering
     * @param mouseX the setX-coordinate of the mouse
     * @param mouseY the setY-coordinate of the mouse
     * @param selected whether the log is selected
     * @param deltaTime the time difference for rendering
     */
    private void renderItem(Renderer renderer, CrashLog value, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        // Get the throwable object from the CrashLog
        Throwable throwable = value.getThrowable();

        // Extract the simple name of the throwable class
        String throwableName = throwable.getClass().getSimpleName();

        // Extract the error message from the throwable
        String message = throwable.getMessage();
        String errorMessage = message == null ? "<No message>" : message.trim().stripIndent().replace("\n", " ").replace("\t", "").replace("java.lang.", "");

        // Get the first usable stack id from the throwable stack trace
        String usableStackTrace = Arrays.stream(throwable.getStackTrace())
                .filter(CrashScreen::isUsableStackElement)
                .map(StackTraceElement::toString)
                .findFirst()
                .orElse("<Unknown>")
                .trim();

        // Render the crash log details using the renderer
        renderer.textLeft(value.getDetails().trim(), 20, y + 20, ColorCode.RED);
        renderer.textLeft(throwableName + ": " + errorMessage, 20, y + 32, ColorCode.GRAY);
        renderer.textLeft("    " + usableStackTrace, 20, y + 44, ColorCode.DARK_GRAY);
    }

    @Override
    public boolean canCloseWithEsc() {
        return false;
    }

    @Override
    public boolean canClose() {
        return false;
    }
}
