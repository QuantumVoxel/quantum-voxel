package dev.ultreon.quantum.client.gui.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class SystemInfoDebugPage implements DebugPage {

    @Override
    public void render(DebugPageContext context) {
        // Get current process's PID
        context.left("System");

        context.left();
        context.left("Graphics");
        context.left("Display", Gdx.graphics.getDisplayMode().toString());
        context.left("GL Renderer", Gdx.gl.glGetString(GL20.GL_RENDERER));
        context.left("GL Version", Gdx.gl.glGetString(GL20.GL_VERSION));
        context.left("GL Vendor", Gdx.gl.glGetString(GL20.GL_VENDOR));
        context.left("GL Extensions", Gdx.gl.glGetString(GL20.GL_EXTENSIONS));

        context.right();
        context.right("OS");
        context.right("Name", System.getProperty("os.name"));
        context.right("Version", System.getProperty("os.version"));
        context.right("Architecture", System.getProperty("os.arch"));

        context.right();
        context.right("Java");
        context.right("Version", System.getProperty("java.version"));
        context.right("VM Name", System.getProperty("java.vm.name"));
        context.right("VM Vendor", System.getProperty("java.vm.vendor"));
        context.right("VM Version", System.getProperty("java.vm.version"));

        if (System.getProperty("java.vm.vendor").equals("GraalVM")) {
            context.right();
            context.right("GraalVM");
            context.right("Version", System.getProperty("graalvm.version"));
            context.right("VM Name", System.getProperty("graalvm.vm.name"));
            context.right("VM Vendor", System.getProperty("graalvm.vm.vendor"));
            context.right("VM Version", System.getProperty("graalvm.vm.version"));
        }
    }
}
