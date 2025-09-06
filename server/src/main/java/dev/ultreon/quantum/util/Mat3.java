package dev.ultreon.quantum.util;

public interface Mat3 {
    float[] getValues();

    Mat3 translate(float x, float y, float z);
    Mat3 scale(float x, float y, float z);
    Mat3 rotate(float angle, float x, float y, float z);
    Mat3 multiply(Mat3 other);
    Mat3 invert();

    void set(float[] values);
    void setIdentity();

    boolean isIdentity();
}
