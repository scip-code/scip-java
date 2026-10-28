package snapshots

class Overrides : AutoCloseable {
    override fun close() {
        TODO("Not yet implemented")
    }
}

interface Animal {
    val favoriteNumber: Int
    fun sound(): String
}
open class Bird : Animal {
    override val favoriteNumber: Int
        get() = 42

    override fun sound(): String {
        return "tweet"
    }
}
class Seagull : Bird() {
    override val favoriteNumber: Int
        get() = 1337
    override fun sound(): String {
        return "squawk"
    }
}

interface Named
interface Speakable
class Person : Named, Speakable
fun usePerson(p: Person): Named = p
