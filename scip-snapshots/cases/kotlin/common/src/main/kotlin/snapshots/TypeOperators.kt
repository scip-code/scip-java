package snapshots

fun typeOperators(x: Any) {
    when (x) {
        is Int -> true
        else -> x as Float
    }
}

class Wrapper

fun classify(x: Any) {
    if (x is Wrapper) {}
    val s = x as? String
    val w = x as Wrapper
}
