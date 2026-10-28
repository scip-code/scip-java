package snapshots

val x = arrayListOf<String>().forEachIndexed { i, s -> println("$i $s") }

val y = "fdsa".run { this.toByteArray() }

val z = y.let { it.size }

fun useLambda() {
    val f = { n: Int -> n * 2 }
}
