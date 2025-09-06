package dev.ultreon.hydro.backends.libgdxwg.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import dev.ultreon.hydro.graphics.Stencils;

public class WgGdxStencils implements Stencils {
    @Override
    public void set(int value) {
        Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, value, 0xFF);
    }

    @Override
    public void clear() {
        Gdx.gl.glClear(GL20.GL_STENCIL_BUFFER_BIT);
    }

    @Override
    public void setEnabled(boolean value) {
        if(value) {
            Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
        } else {
            Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
        }
    }

    @Override
    public boolean isEnabled() {
        return Gdx.gl.glIsEnabled(GL20.GL_STENCIL_TEST);
    }
}
