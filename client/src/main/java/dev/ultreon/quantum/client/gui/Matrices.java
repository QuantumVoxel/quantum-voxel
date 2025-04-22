package dev.ultreon.quantum.client.gui;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FlushablePool;
import dev.ultreon.quantum.client.util.Utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class Matrices {
    private final FlushablePool<Matrix4> pool = new FlushablePool<>() {
        @Override
        protected Matrix4 newObject() {
            return new Matrix4();
        }
    };

    private final Deque<Matrix4> stack;
    public Consumer<Matrix4> onPush = matrix -> {};
    public Consumer<Matrix4> onPop = matrix -> {};
    public Consumer<Matrix4> onEdit = matrix -> {};
    private Vector3 tmp;

    public Matrices() {
        this.stack = Utils.make(new ArrayDeque<>(), matrixDeque -> matrixDeque.add(this.pool.obtain().idt()));
    }

    public Matrices(Matrix4 origin) {
        this.stack = Utils.make(new ArrayDeque<>(), matrixDeque -> matrixDeque.add(origin));
    }

    public void push() {
        this.stack.add(this.stack.getLast().cpy());
        this.onEdit.accept(this.stack.getLast());
    }

    public void pop() {
        this.stack.removeLast().idt();
        this.onEdit.accept(this.stack.getLast());
    }

    public void translate(double x, double y) {
        this.translate((float) x, (float) y);
    }

    public void translate(float x, float y) {
        Matrix4 matrix = this.stack.getLast();
        matrix.translate(x, y, 0);
        this.onEdit.accept(matrix);
    }

    public void translate(float x, float y, float z) {
        Matrix4 matrix = this.stack.getLast();
        matrix.translate(x, y, z);
        this.onEdit.accept(matrix);
    }

    public void scale(float x, float y) {
        Matrix4 matrix = this.stack.getLast();
        matrix.scale(x, y, 0);
        this.onEdit.accept(matrix);
    }
    
    public void rotate(Quaternion quaternion) {
        Matrix4 matrix = this.stack.getLast();
        matrix.rotate(quaternion);
        this.onEdit.accept(matrix);
    }

    public Matrix4 last() {
        return this.stack.getLast();
    }

    public boolean isClear() {
        return this.stack.size() == 1;
    }

    @Override
    public String toString() {
        return this.stack.toString();
    }

    public void reset() {
        pool.flush();
    }

    public Vector2 getScale(Vector2 scale) {
        Matrix4 matrix = this.stack.getLast();
        scale.set(matrix.getScaleX(), matrix.getScaleY());
        return scale;
    }

    public Vector3 getScale(Vector3 scale) {
        Matrix4 matrix = this.stack.getLast();
        scale.set(matrix.getScaleX(), matrix.getScaleY(), matrix.getScaleZ());
        return scale;
    }

    public Vector2 getTranslation(Vector2 translation) {
        Matrix4 matrix = this.stack.getLast();
        matrix.getTranslation(tmp);
        translation.set(tmp.x, tmp.y);
        return translation;
    }

    public Vector3 getTranslation(Vector3 translation) {
        Matrix4 matrix = this.stack.getLast();
        matrix.getTranslation(translation);
        return translation;
    }

    public Quaternion getRotation(Quaternion rotation) {
        Matrix4 matrix = this.stack.getLast();
        matrix.getRotation(rotation);
        return rotation;
    }
}
