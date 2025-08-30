from ultreonjv.gdx.ApplicationAdapter import ApplicationAdapter
from ultreonjv.gdx.backends.lwjgl3.Lwjgl3Application import Lwjgl3Application
from ultreonjv.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration import Lwjgl3ApplicationConfiguration
from ultreonjv.gdx.graphics.Texture import Texture
from ultreonjv.gdx.graphics.g2d.SpriteBatch import SpriteBatch
from ultreonjv.jvm.lang.System import System


class MyListener(ApplicationAdapter):
    def __init__(self):
        super().__init__()

        self.texture: Texture | None = None
        self.batch: SpriteBatch | None = None

    def create(self) -> None:
        print("Hello, World!")
        self.texture = Texture("assets/textures/test.png")
        self.batch = SpriteBatch()
        return

    def render(self) -> None:
        self.batch.draw(self.texture, 0, 0)

    def dispose(self) -> None:
        self.batch.dispose()


if __name__ == '__main__':
    config = Lwjgl3ApplicationConfiguration()
    config.setTitle("Hello, World!")

    Lwjgl3Application(MyListener(), config)
    System.exit(0)
