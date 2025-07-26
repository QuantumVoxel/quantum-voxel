package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Box {
    public final Vector3 pos000 = new Vector3();
    public final Vector3 pos001 = new Vector3();
    public final Vector3 pos010 = new Vector3();
    public final Vector3 pos011 = new Vector3();
    public final Vector3 pos100 = new Vector3();
    public final Vector3 pos101 = new Vector3();
    public final Vector3 pos110 = new Vector3();
    public final Vector3 pos111 = new Vector3();
    
    private final Matrix4 tmp = new Matrix4();

    public Box() {
        
    }
    
    public Box(Vector3 pos000,
               Vector3 pos001,
               Vector3 pos010,
               Vector3 pos011,
               Vector3 pos100,
               Vector3 pos101,
               Vector3 pos110,
               Vector3 pos111) {
        this.pos000.set(pos000);
        this.pos001.set(pos001);
        this.pos010.set(pos010);
        this.pos011.set(pos011);
        this.pos100.set(pos100);
        this.pos101.set(pos101);
        this.pos110.set(pos110);
        this.pos111.set(pos111);
    }
    
    public Box set(Vector3 pos000,
                    Vector3 pos001,
                    Vector3 pos010,
                    Vector3 pos011,
                    Vector3 pos100,
                    Vector3 pos101,
                    Vector3 pos110,
                    Vector3 pos111) {
        this.pos000.set(pos000);
        this.pos001.set(pos001);
        this.pos010.set(pos010);
        this.pos011.set(pos011);
        this.pos100.set(pos100);
        this.pos101.set(pos101);
        this.pos110.set(pos110);
        this.pos111.set(pos111);
        return this;
    }
    
    public Box unproject(Matrix4 projection, Matrix4 view, Matrix4 model) {
        pos000.mul(tmp.set(projection).mul(view).mul(model).inv());
        pos001.mul(tmp.set(projection).mul(view).mul(model).inv());
        pos010.mul(tmp.set(projection).mul(view).mul(model).inv());
        pos011.mul(tmp.set(projection).mul(view).mul(model).inv());
        pos100.mul(tmp.set(projection).mul(view).mul(model).inv());
        pos101.mul(tmp.set(projection).mul(view).mul(model).inv());
        pos110.mul(tmp.set(projection).mul(view).mul(model).inv());
        pos111.mul(tmp.set(projection).mul(view).mul(model).inv());
        return this;
    }
    
    public Box unproject(Matrix4 projection, Matrix4 modelView) {
        pos000.mul(tmp.set(projection).mul(modelView).inv());
        pos001.mul(tmp.set(projection).mul(modelView).inv());
        pos010.mul(tmp.set(projection).mul(modelView).inv());
        pos011.mul(tmp.set(projection).mul(modelView).inv());
        pos100.mul(tmp.set(projection).mul(modelView).inv());
        pos101.mul(tmp.set(projection).mul(modelView).inv());
        pos110.mul(tmp.set(projection).mul(modelView).inv());
        pos111.mul(tmp.set(projection).mul(modelView).inv());
        return this;
    }
    
    public Box unproject(Matrix4 combined) {
        pos000.mul(tmp.set(combined).inv());
        pos001.mul(tmp.set(combined).inv());
        pos010.mul(tmp.set(combined).inv());
        pos011.mul(tmp.set(combined).inv());
        pos100.mul(tmp.set(combined).inv());
        pos101.mul(tmp.set(combined).inv());
        pos110.mul(tmp.set(combined).inv());
        pos111.mul(tmp.set(combined).inv());
        return this;
    }
    
    public Box project(Matrix4 projection, Matrix4 view, Matrix4 model) {
        pos000.mul(tmp.set(projection).mul(view).mul(model));
        pos001.mul(tmp.set(projection).mul(view).mul(model));
        pos010.mul(tmp.set(projection).mul(view).mul(model));
        pos011.mul(tmp.set(projection).mul(view).mul(model));
        pos100.mul(tmp.set(projection).mul(view).mul(model));
        pos101.mul(tmp.set(projection).mul(view).mul(model));
        pos110.mul(tmp.set(projection).mul(view).mul(model));
        pos111.mul(tmp.set(projection).mul(view).mul(model));
        return this;
    }
    
    public Box project(Matrix4 projection, Matrix4 modelView) {
        pos000.mul(tmp.set(projection).mul(modelView));
        pos001.mul(tmp.set(projection).mul(modelView));
        pos010.mul(tmp.set(projection).mul(modelView));
        pos011.mul(tmp.set(projection).mul(modelView));
        pos100.mul(tmp.set(projection).mul(modelView));
        pos101.mul(tmp.set(projection).mul(modelView));
        pos110.mul(tmp.set(projection).mul(modelView));
        pos111.mul(tmp.set(projection).mul(modelView));
        return this;
    }
    
    public Box project(Matrix4 combined) {
        pos000.mul(tmp.set(combined));
        pos001.mul(tmp.set(combined));
        pos010.mul(tmp.set(combined));
        pos011.mul(tmp.set(combined));
        pos100.mul(tmp.set(combined));
        pos101.mul(tmp.set(combined));
        pos110.mul(tmp.set(combined));
        pos111.mul(tmp.set(combined));
        return this;
    }

    public Box set(BoundingBox tightBounds) {
        tightBounds.getCorner000(pos000);
        tightBounds.getCorner001(pos001);
        tightBounds.getCorner010(pos010);
        tightBounds.getCorner011(pos011);
        tightBounds.getCorner100(pos100);
        tightBounds.getCorner101(pos101);
        tightBounds.getCorner110(pos110);
        tightBounds.getCorner111(pos111);
        return this;
    }

    public Rectangle rect(Rectangle rectangle) {
        rectangle.set(pos000.x, pos000.y, 0, 0);
        rectangle.merge(pos001.x, pos001.y);
        rectangle.merge(pos010.x, pos010.y);
        rectangle.merge(pos011.x, pos011.y);
        rectangle.merge(pos100.x, pos100.y);
        rectangle.merge(pos101.x, pos101.y);
        rectangle.merge(pos110.x, pos110.y);
        rectangle.merge(pos111.x, pos111.y);
        return rectangle;
    }
}
