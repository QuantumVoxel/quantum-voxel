package dev.ultreon.quantum.util;

public interface Mat4 {
    float[] getValues();

    Mat4 translate(float x, float y, float z);
    Mat4 scale(float x, float y, float z);
    Mat4 rotate(float angle, float x, float y, float z);
    Mat4 multiply(Mat4 other);
    Mat4 invert();

    void getTranslation(Vec3 translation);
    void getScale(Vec3 scale);
    void getRotation(Quat rotation);
    void getRotationEuler(Vec3 rotation);

    void setTranslation(float x, float y, float z);
    void setScale(float x, float y, float z);
    void setRotation(Quat rotation);
    void setRotationEuler(float x, float y, float z);

    void set(float[] values);
    void setIdentity();

    boolean isIdentity();
}
