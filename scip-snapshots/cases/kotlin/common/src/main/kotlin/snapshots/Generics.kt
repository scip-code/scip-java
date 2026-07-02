package snapshots

class Container<T : Comparable<T>>(private val items: MutableList<T>) {
    fun add(item: T): Container<T> {
        items.add(item)
        return this
    }

    fun <R : Any> mapItems(transform: (T) -> R?): List<R> = items.mapNotNull(transform)
}

fun <T> firstOrSelf(items: List<T>, fallback: T): T = items.firstOrNull() ?: fallback

typealias StringContainer = Container<String>

fun useContainer(container: StringContainer): StringContainer = container.add("hello")
