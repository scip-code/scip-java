package snapshots

fun sampleText(x: String = "") {
  println(x)
}

class MyClass

fun bar(): MyClass = MyClass()

fun greet(name: String) = "Hello, $name!"

fun add(x: Int, y: Int): Int = x + y
fun add(x: Double, y: Double): Double = x + y
fun add(x: String, y: String): String = x + y

fun useOverloads() {
    add(1, 2)
    add(1.0, 2.0)
    add("a", "b")
}
