package dev.ultreon.quantum.desktop;

import oshi.SystemInfo;

public class LaunchConfig {
    public long maxMemoryMB = 4096;

    public void fix(SystemInfo systemInfo) {
        if (maxMemoryMB <= 1024) maxMemoryMB = 4096;

        if (maxMemoryMB >= systemInfo.getHardware().getMemory().getTotal() / 1572864) {
            maxMemoryMB = systemInfo.getHardware().getMemory().getTotal() / 1572864;
        }
    }
}
