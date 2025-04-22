package dev.ultreon.quantum.android;

import android.app.*;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.*;
import android.hardware.input.InputManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import dev.ultreon.quantum.DeviceType;
import dev.ultreon.quantum.client.Main;

public class AndroidLauncher extends AndroidApplication implements SensorEventListener, InputManager.InputDeviceListener {
    private AndroidPlatform androidPlatform;
    private SensorManager sensorManager;
    private int orientation;
    private NotificationManager notificationManager;
    private InputManager inputManager;
    private GLSurfaceView view;
    private boolean isMouseCaptured;
    private UiModeManager uiModeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.maxSimultaneousSounds = 256;
        config.useGyroscope = true;
        config.useCompass = true;
        config.r = 8;
        config.g = 8;
        config.b = 8;
        config.a = 8;
        config.depth = 16;
        config.stencil = 8;
        config.useImmersiveMode = true;
        config.renderUnderCutout = true;
        config.maxNetThreads = 4;

        // Make sure the shaders use GLES 2
        ShaderProgram.prependVertexCode = "#version 100\n";
        ShaderProgram.prependFragmentCode = "#version 100\n";

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        androidPlatform = new AndroidPlatform(this);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel("quantum", "Quantum", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notificationChannel);

        inputManager = (InputManager) getSystemService(INPUT_SERVICE);
        inputManager.registerInputDeviceListener(this, Handler.createAsync(Looper.getMainLooper()));

        this.uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);

        initialize(Main.createInstance(new String[]{"--android"}), config);
    }

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        androidPlatform.handleSensorChange(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setRotationLock(boolean lock) {
        setRequestedOrientation(lock ? orientation : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);

        orientation = config.orientation;
    }

    public void sendNotification(String tag, String title, String message) {
        // Send notification
        Notification notification = new Notification.Builder(this, "quantum")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setChannelId("quantum")
                .setOngoing(true)
                .build();

        notificationManager.notify(tag, 0, notification);
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    @Override
    public void onInputDeviceAdded(int deviceId) {
        InputDevice inputDevice = inputManager.getInputDevice(deviceId);
        if (inputDevice.supportsSource(InputDevice.SOURCE_MOUSE)) {
            androidPlatform.addMouseDevice(deviceId, inputDevice);
        } else if (inputDevice.supportsSource(InputDevice.SOURCE_KEYBOARD)) {
            androidPlatform.addKeyboardDevice(deviceId, inputDevice);
        }
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        androidPlatform.removeDevice(deviceId);
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        View view = super.onCreateView(name, context, attrs);
        if (view instanceof GLSurfaceView && this.view == null) {
            this.view = (GLSurfaceView) view;
        }
        return view;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (view == null) {
            Log.w("QuantumAndroid", "GLSurfaceView is null");
            return;
        }

        view = null;
    }

    public void setMouseCaptured(boolean captured) {
        if (view == null) {
            Log.w("QuantumAndroid", "GLSurfaceView is null");
            return;
        }

        if (captured) {
            view.requestPointerCapture();
            this.isMouseCaptured = true;
        } else {
            view.releasePointerCapture();
            this.isMouseCaptured = false;
        }
    }

    public boolean isMouseCaptured() {
        if (view == null) {
            Log.w("QuantumAndroid", "GLSurfaceView is null");
            return false;
        }

        return isMouseCaptured;
    }

    public void setCursorPosition(int x, int y) {
        if (view == null) {
            Log.w("QuantumAndroid", "GLSurfaceView is null");
            return;
        }

        view.queueEvent(() -> {
            // TODO: mouseDevice.setCursorPosition(x, y);
        });
    }

    public DeviceType getDeviceType() {
        switch (uiModeManager.getCurrentModeType()) {
            case Configuration.UI_MODE_TYPE_TELEVISION:
                return DeviceType.TV;
            case Configuration.UI_MODE_TYPE_CAR:
                return DeviceType.AUTOMOBILE;
            case Configuration.UI_MODE_TYPE_DESK:
                return DeviceType.DESKTOP;
            case Configuration.UI_MODE_TYPE_WATCH:
                return DeviceType.WATCH;
            case Configuration.UI_MODE_TYPE_NORMAL:
                return DeviceType.MOBILE;
            case Configuration.UI_MODE_TYPE_APPLIANCE:
                return DeviceType.APPLIANCE;
            case Configuration.UI_MODE_TYPE_VR_HEADSET:
                return DeviceType.VR_HEADSET;
            default:
                return DeviceType.OTHER;
        }
    }
}
