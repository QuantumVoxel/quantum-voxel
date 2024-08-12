package dev.ultreon.quantum.client.gui;

import dev.ultreon.libs.datetime.v0.Duration;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.icon.Icon;
import dev.ultreon.quantum.client.util.Renderable;
import dev.ultreon.quantum.text.MutableText;
import dev.ultreon.quantum.util.RgbColor;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Notifications implements Renderable {
    private static final int HEIGHT = 41;
    private static final int WIDTH = 150;
    private static final int OFFSET = 5;
    private static final int GAP = 5;

    private final Lock lock = new ReentrantLock(true);
    private final QuantumClient client;
    private final Deque<Notification> notifications = new ArrayDeque<>();
    private final Set<UUID> usedNotifications = new HashSet<>();
    private float motionY;

    public Notifications(QuantumClient client) {
        this.client = client;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        if (this.client.isLoading()) return;

        int y = (int) (Notifications.OFFSET + this.motionY);

        this.lock.lock();
        this.notifications.removeIf(notification1 -> {
            if (notification1.isFinished()) {
                this.motionY += Notifications.HEIGHT + Notifications.GAP;
                return true;
            }
            return false;
        });

        for (Notification notification : this.notifications) {
            MutableText title = notification.getTitle().setBold(true);
            MutableText summary = notification.getSummary();
            MutableText subText = notification.getSubText();
            Icon icon = notification.getIcon();

            int width = NumberUtils.max(this.client.font.width(title), this.client.font.width(summary), this.client.font.width(subText), Notifications.WIDTH) + 10;
            if (icon != null) width += 37;

            int x = this.client.getScaledWidth() - Notifications.OFFSET - width;

            float motionRatio = notification.getMotion();
            int motion = (int) ((width + Notifications.OFFSET) * motionRatio);

            renderer.fill(x + motion + 1, y, width - 2, Notifications.HEIGHT, RgbColor.rgb(0x101010))
                    .fill(x + motion, y + 1, width, Notifications.HEIGHT - 2, RgbColor.rgb(0x101010))
                    .box(x + motion + 1, y + 1, width - 2, Notifications.HEIGHT - 2, RgbColor.rgb(0x505050));

            if (icon != null) {
                icon.render(renderer, x + motion + 4, y + 4, 32, 32, deltaTime);
            }

            int textX = icon == null ? 0 : 37;

            renderer.textLeft(title, x + motion + 5 + textX, y + 5, RgbColor.rgb(0xd0d0d0))
                    .textLeft(summary, x + motion + 5 + textX, y + 17, RgbColor.rgb(0xb0b0b0))
                    .textLeft(subText, x + motion + 5 + textX, y + 29, RgbColor.rgb(0x707070));

            y += Notifications.HEIGHT + Notifications.GAP;
        }

        this.motionY = Math.max(this.motionY - ((deltaTime * Notifications.HEIGHT) * 4 + (deltaTime * this.motionY) * 4), 0);

        this.lock.unlock();
    }

    public void add(Notification notification) {
        if (!QuantumClient.isOnRenderThread()) {
            if (QuantumClient.get().isShutdown()) return;
            QuantumClient.invoke(() -> this.add(notification));
            return;
        }

        this.notifications.addLast(notification);
    }

    public void add(String title, String description) {
        this.add(Notification.builder(title, description).build());
    }

    public void add(String title, String description, String subText) {
        this.add(Notification.builder(title, description).subText(subText).build());
    }

    public void add(MutableText title, MutableText description) {
        this.add(Notification.builder(title, description).build());
    }

    public void add(MutableText title, MutableText description, MutableText subText) {
        this.add(Notification.builder(title, description).subText(subText).build());
    }

    public void addOnce(UUID uuid, Notification message) {
        if (!this.usedNotifications.contains(uuid)) {
            this.usedNotifications.add(uuid);
            this.add(message);
        }
    }

    public void unavailable(String feature) {
        this.add(Notification.builder("Unavailable Feature", String.format("'%s' isn't available yet.", feature))
                .subText("Feature Locker")
                .duration(Duration.ofSeconds(5))
                .build()
        );
    }
}
