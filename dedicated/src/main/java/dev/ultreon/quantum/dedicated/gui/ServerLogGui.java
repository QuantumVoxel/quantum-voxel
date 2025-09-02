package dev.ultreon.quantum.dedicated.gui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.TimerTask;

public class ServerLogGui extends JScrollPane {
    private final JTextArea logArea;

    public ServerLogGui(DedicatedServerGui gui) {
        super();

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        logArea = new JTextArea();
        this.setViewportView(logArea);
        logArea.setEditable(false);
        logArea.setOpaque(false);
        logArea.setBackground(new Color(0, 0, 0, 0));
        logArea.setCaretPosition(0);
        logArea.setMargin(new Insets(0, 0, 0, 0));
        logArea.setLineWrap(false);
        logArea.setWrapStyleWord(false);

        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        gui.timer.schedule(new LogUpdateTask(), 0, 100);
    }

    private void close() {

    }

    public void clear() {
        JTextArea logArea = (JTextArea) this.getViewport().getView();
        logArea.setText("");
    }

    public void scrollToBottom() {
        this.getVerticalScrollBar().setValue(this.getVerticalScrollBar().getMaximum());
    }

    public void scrollToTop() {
        this.getVerticalScrollBar().setValue(0);
    }

    private class LogUpdateTask extends TimerTask {
        @Override
        public void run() {
            try (Scanner scanner = new Scanner(new File("logs/latest.log"))) {
                @NotNull String log = read(scanner);

                SwingUtilities.invokeLater(() -> updateLog(log));
            } catch (FileNotFoundException e) {
                end(e);
            }
        }

        private @NotNull String read(Scanner scanner) {
            StringBuilder log = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                log.append(line).append("\n");
            }
            return log.toString();
        }

        private void end(FileNotFoundException e) {
            try {
                e.wait(10000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            cancel();
        }

        private void updateLog(@NotNull String log) {
            int selectionStart = logArea.getSelectionStart();
            int selectionEnd = logArea.getSelectionEnd();
            if (selectionEnd == logArea.getText().length()) {
                logArea.setText(log);
                logArea.select(Math.min(selectionStart, logArea.getText().length()), logArea.getText().length());
                getVerticalScrollBar().setValue(getVerticalScrollBar().getMaximum());
            }
        }
    }
}
