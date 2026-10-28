package snapshots

import java.lang.RuntimeException

object ObjectKt {
  fun fail(message: String?): Nothing {
    throw RuntimeException(message)
  }
}

// Named objects define an INSTANCE field for Java callers; object expressions do not.
object MySingleton {
    fun hello(): String = "hi"
}
fun useSingleton() = MySingleton.hello()
