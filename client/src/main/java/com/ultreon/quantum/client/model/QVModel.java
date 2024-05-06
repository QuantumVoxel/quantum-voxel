package com.ultreon.quantum.client.model;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.ultreon.quantum.client.render.shader.Shaders;
import com.ultreon.quantum.client.render.shader.OpenShaderProvider;

import static com.badlogic.gdx.graphics.g3d.utils.AnimationController.*;

public class QVModel {
    private final ModelInstance instance;
    private final AnimationController animationController;
    private final OpenShaderProvider shaderProvider = Shaders.MODEL_VIEW.get();

    public QVModel(ModelInstance instance) {
        this.instance = instance;
        this.animationController = new AnimationController(instance);
    }

    public ModelInstance getInstance() {
        return instance;
    }

    public AnimationController getAnimationController() {
        return animationController;
    }

    public void update(float delta) {
        animationController.update(delta);
    }

    public void dispose() {
        //? Is this necessary?
    }

    public AnimationDesc setAnimation(String animation) {
        return animationController.setAnimation(animation);
    }

    public AnimationDesc setAnimation(String animation, int loopCount) {
        return animationController.setAnimation(animation, loopCount);
    }

    public AnimationDesc setAnimation(String animation, AnimationListener listener) {
        return animationController.setAnimation(animation, listener);
    }

    public AnimationDesc setAnimation(String animation, int loopCount, AnimationListener listener) {
        return animationController.setAnimation(animation, loopCount, listener);
    }

    public AnimationDesc setAnimation(String animation, int loopCount, float speed, AnimationListener listener) {
        return animationController.setAnimation(animation, loopCount, speed, listener);
    }

    public AnimationDesc setAnimation(String animation, float offset, float duration, int loopCount, float speed, AnimationListener listener) {
        return animationController.setAnimation(animation, offset, duration, loopCount, speed, listener);
    }

    public AnimationDesc animate(String animation, float transitionTime) {
        return animationController.animate(animation, transitionTime);
    }

    public AnimationDesc animate(String animation, AnimationListener listener, float transitionTime) {
        return animationController.animate(animation, listener, transitionTime);
    }

    public AnimationDesc animate(String animation, int loopCount, AnimationListener listener, float transitionTime) {
        return animationController.animate(animation, loopCount, listener, transitionTime);
    }

    public AnimationDesc animate(String animation, int loopCount, float speed, AnimationListener listener, float transitionTime) {
        return animationController.animate(animation, loopCount, speed, listener, transitionTime);
    }

    public AnimationDesc animate(String animation, float offset, float duration, int loopCount, float speed, AnimationListener listener, float transitionTime) {
        return animationController.animate(animation, offset, duration, loopCount, speed, listener, transitionTime);
    }

    public AnimationDesc action(String animation, int loopCount, float speed, AnimationListener listener, float transitionTime) {
        return animationController.action(animation, loopCount, speed, listener, transitionTime);
    }

    public AnimationDesc action(String animation, float offset, float duration, int loopCount, float speed, AnimationListener listener, float transitionTime) {
        return animationController.action(animation, offset, duration, loopCount, speed, listener, transitionTime);
    }

    public AnimationDesc queue(String animation, int loopCount, float speed, AnimationListener listener, float transitionTime) {
        return animationController.queue(animation, loopCount, speed, listener, transitionTime);
    }

    public AnimationDesc queue(String animation, float offset, float duration, int loopCount, float speed, AnimationListener listener, float transitionTime) {
        return animationController.queue(animation, offset, duration, loopCount, speed, listener, transitionTime);
    }

    public AnimationDesc getPreviousAnimation() {
        return animationController.previous;
    }

    public void setPreviousAnimation(AnimationDesc previousAnimation) {
        animationController.previous = previousAnimation;
    }

    public AnimationDesc getCurrentAnimation() {
        return animationController.current;
    }

    public void setCurrentAnimation(AnimationDesc currentAnimation) {
        animationController.current = currentAnimation;
    }

    public AnimationDesc getNextAnimation() {
        return animationController.queued;
    }

    public void setNextAnimation(AnimationDesc nextAnimation) {
        animationController.queued = nextAnimation;
    }

    public boolean isAnimationFinished() {
        return !animationController.inAction && !animationController.paused;
    }

    public boolean isAnimationPlaying() {
        return animationController.inAction && !animationController.paused;
    }

    public boolean isAnimationPaused() {
        return animationController.paused;
    }

    public boolean isAnimationStopped() {
        return !animationController.inAction;
    }

    public void pauseAnimation() {
        animationController.paused = true;
    }

    public void resumeAnimation() {
        animationController.paused = false;
    }

    public void stopAnimation() {
        animationController.inAction = false;
    }

    public float getTransitionTargetTime() {
        return animationController.transitionTargetTime;
    }

    public void setTransitionTargetTime(float transitionTargetTime) {
        animationController.transitionTargetTime = transitionTargetTime;
    }

    public float getTransitionCurrentTime() {
        return animationController.transitionCurrentTime;
    }

    public void setTransitionCurrentTime(float transitionCurrentTime) {
        animationController.transitionCurrentTime = transitionCurrentTime;
    }

    public float getQueuedTransitionTime() {
        return animationController.queuedTransitionTime;
    }

    public void setQueuedTransitionTime(float queuedTransitionTime) {
        animationController.queuedTransitionTime = queuedTransitionTime;
    }

    public boolean isSameAnimationAllowed() {
        return animationController.allowSameAnimation;
    }

    public void setAllowSameAnimation(boolean allowSameAnimation) {
        animationController.allowSameAnimation = allowSameAnimation;
    }

    public OpenShaderProvider getShaderProvider() {
        return shaderProvider;
    }
}
