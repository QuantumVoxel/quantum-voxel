package dev.ultreon.quantum.client.input.controller;

public interface InterceptInvalidation {
    void onIntercept(ControllerInput.InterceptCallback callback);

    boolean isStillValid();
}
