package dev.ultreon.quantum.android;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.*;
import android.hardware.input.InputManager;
import android.net.Uri;
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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.GameLibGDXWrapper;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.screens.ModImportFailedScreen;
import dev.ultreon.quantum.client.gui.screens.RestartConfirmScreen;
import dev.ultreon.xeox.loader.XeoxLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.UUID;

public class AndroidLauncher extends AndroidApplication implements SensorEventListener, InputManager.InputDeviceListener {
    private AndroidPlatform androidPlatform;
    private SensorManager sensorManager;
    private int orientation;
    private NotificationManager notificationManager;
    private InputManager inputManager;
    private GLSurfaceView view;
    private boolean isMouseCaptured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useGL30 = true;
        config.maxSimultaneousSounds = 256;
        config.useGyroscope = true;
        config.useCompass = true;

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        androidPlatform = new AndroidPlatform(this);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel("quantum", "Quantum", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notificationChannel);

        inputManager = (InputManager) getSystemService(INPUT_SERVICE);
        inputManager.registerInputDeviceListener(this, Handler.createAsync(Looper.getMainLooper()));

        initialize(GameLibGDXWrapper.createInstance(new String[]{"--android"}), config);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AndroidPlatform.IMPORT_MOD_CODE && resultCode == Activity.RESULT_OK) {
            try {
                Uri contentDescriber = data.getData();
                File source = new File(contentDescriber.getPath());
                CommonConstants.LOGGER.debug("src is %s", source);

                FileHandle external = Gdx.files.external("temp");
                File tempFile = external.child(source.getName() + "_" + UUID.randomUUID() + ".tmp").file();
                tempFile.deleteOnExit();
                copy(source, tempFile);
                XeoxLoader.get().importMod(tempFile);
                tempFile.delete();
                QuantumClient.get().showScreen(new RestartConfirmScreen());
            } catch (Exception e) {
                Log.e("Quantum", "Failed to import mod file", e);
                QuantumClient.get().showScreen(new ModImportFailedScreen());
            }
        }
    }

    private void copy(File source, File destination) throws IOException {
        try (FileChannel in = new FileInputStream(source).getChannel();
             FileChannel out = new FileOutputStream(destination).getChannel()) {

            try {
                in.transferTo(0, in.size(), out);
            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            }
        }
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
            AndroidMouseDevice mouseDevice = androidPlatform.getMouseDevice();
            if (mouseDevice != null) {
                // TODO: mouseDevice.setCursorPosition(x, y);
            }
        });
    }
}
