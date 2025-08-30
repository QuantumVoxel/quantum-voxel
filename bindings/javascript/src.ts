import ApplicationListener from "@ultreon/jv-gdx/ApplicationListener";
import Texture from "@ultreon/jv-gdx/graphics/Texture";
import Batch from "@ultreon/jv-gdx/graphics/g2d/Batch";
import SpriteBatch from "@ultreon/jv-gdx/graphics/g2d/SpriteBatch";
import Lwjgl3ApplicationConfiguration from "@ultreon/jv-gdx/backends/lwjgl3/Lwjgl3ApplicationConfiguration";
import Lwjgl3Application from "@ultreon/jv-gdx/backends/lwjgl3/Lwjgl3Application";
import System from "@ultreon/jv-jvm-lang/System";

((_args: any[]) => {
  class MyListener implements ApplicationListener {
    private texture: Texture;
    private batch: Batch;

    create() {
      console.log("Hello World!");
      this.texture = new Texture("assets/images/test.png");
      this.batch = new SpriteBatch();
    }

    render() {
      this.batch.begin();
      this.batch.draw(this.texture, 0, 0);
      this.batch.end();
    }

    resize(width: number, height: number) {
      this.batch.setProjectionMatrix(this.batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height));
    }

    pause() {

    }

    resume() {

    }

    dispose() {
      this.texture.dispose();
      this.batch.dispose();
    }
  }

  const config = new Lwjgl3ApplicationConfiguration();
  config.setTitle("My GDX Project");
  config.setWindowedMode(800, 600);
  config.setWindowIcon("assets/images/icon.png");
  config.useVsync(true);
  config.setBackBufferConfig(8, 8, 8, 8, 24, 0, 2);
  config.setForegroundFPS(60);
  config.setIdleFPS(60);
  new Lwjgl3Application(new MyListener(), config);
  System.exit(0);
})([]);
