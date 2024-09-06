package dev.ultreon.quantum.android;

import android.view.InputDevice;
import dev.ultreon.quantum.platform.MouseDevice;

public class AndroidMouseDevice implements MouseDevice {
    private final InputDevice.MotionRange xMotion;
    private final InputDevice.MotionRange yMotion;
    private final int deviceId;

    public AndroidMouseDevice(InputDevice.MotionRange xMotion, InputDevice.MotionRange yMotion, int deviceId) {
        this.xMotion = xMotion;
        this.yMotion = yMotion;
        this.deviceId = deviceId;
    }

    @Override
    public float getX() {
        return xMotion.getFlat();
    }

    @Override
    public float getY() {
        return yMotion.getFlat();
    }

    public int getDeviceId() {
        return deviceId;
    }
}
