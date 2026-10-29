package snapshots

data class Point(val x: Int, val y: Int)

fun destructure(): Int {
    val (x, y) = Point(1, 2)
    val labeled = listOf(Point(3, 4) to "label")
    val total = labeled.sumOf { (point, label) -> point.x + label.length }
    return x + y + total
}
