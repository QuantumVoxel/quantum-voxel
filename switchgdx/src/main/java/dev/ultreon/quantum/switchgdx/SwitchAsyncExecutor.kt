package dev.ultreon.quantum.switchgdx

import dev.ultreon.quantum.async.AsyncExecutor
import dev.ultreon.quantum.async.AsyncResult
import dev.ultreon.quantum.async.Future

class SwitchAsyncExecutor(maxConcurrent: Int, name: String) : AsyncExecutor() {
  private val gdx = com.badlogic.gdx.utils.async.AsyncExecutor(maxConcurrent, name)

  override fun <T> submit(task: () -> T): Future<T> {
    val future = Future<T>()

    gdx.submit {
      try {
        future.complete(task())
      } catch (e: Throwable) {
        future.completeExceptionally(e)
      }
    }

    return future
  }

  override fun dispose() {
    gdx.dispose()
  }
}
