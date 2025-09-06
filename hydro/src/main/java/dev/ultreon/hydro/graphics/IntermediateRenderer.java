package dev.ultreon.hydro.graphics;

import dev.ultreon.hydro.Usable;
import dev.ultreon.hydro.util.Mat4;

public interface IntermediateRenderer extends AutoCloseable, Usable {
    void drawRectangle(float x, float y, float width, float height);
    void drawLine(float x1, float y1, float x2, float y2);
    void drawText(String text, float x, float y);

    void setTexture(Texture texture);
    void setColor(float r, float g, float b, float a);
    void setColor(float r, float g, float b);

    void setProjectionMatrix(Mat4 projection);

    void setShader(ShaderProgram shader);
}
